package com.example.basefragment.ui.main.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.*
import com.example.basefragment.core.helper.RateHelper
import com.example.basefragment.databinding.FragmentSettingBinding
import com.example.basefragment.utils.music.MusicLocal
import com.example.basefragment.utils.state.RateState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding, SettingViewModel>(
    FragmentSettingBinding::inflate, SettingViewModel::class.java
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPressHandler()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    popBack()
                }
            }
        )
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentSettingBinding = FragmentSettingBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.apply {
            setupActionBar()
            setupSwitches()
            setupRateButton()
            setupScreenMode()
        }
    }

    private fun FragmentSettingBinding.setupActionBar() {
        actionBar.apply {
            tvCenter.select()
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
            setTextActionBar(tvCenter, getString(R.string.settings))
        }
    }

    private fun FragmentSettingBinding.setupSwitches() {
        // Lấy trạng thái từ SharedPreferences (default = true)
        val isMusicOn = sharedPreferences.isMusic()
        val isSoundOn = sharedPreferences.isSound()

        // Đồng bộ MusicLocal với SharedPreferences
        MusicLocal.toggle(requireContext(), isMusicOn)

        // Update UI
        updateSwitchIcon(switchMusic, isMusicOn)
        updateSwitchIcon(switchSound, isSoundOn)
    }

    private fun FragmentSettingBinding.setupRateButton() {
        if (sharedPreferences.isRateRequest()) {
            btnRate.gone()
        } else {
            btnRate.visible()
        }
    }

    private fun FragmentSettingBinding.setupScreenMode() {
        val isVertical = sharedPreferences.isRotate()
        updateScreenModeUI(isVertical)
    }

    override fun viewListener() {
        binding.apply {
            setupActionBarListeners()
            setupNavigationListeners()
            setupSwitchListeners()
            setupScreenModeListeners()
        }
    }

    private fun FragmentSettingBinding.setupActionBarListeners() {
        actionBar.btnActionBarLeft.onClick(requireContext()) {
            popBack()
        }
    }

    private fun FragmentSettingBinding.setupNavigationListeners() {
        btnLang.onClick(requireContext()) {
            toLangFromSetting()
        }

        btnPolicy.onClick(requireContext()) {
            policy()
        }

        btnRate.onClick(requireContext()) {
            RateHelper.showRateDialog(requireActivity(), sharedPreferences) { state ->
                if (state != RateState.CANCEL) {
                    btnRate.gone()
                    showToast(R.string.have_rated)
                }
            }
        }

        btnShare.onClick(requireContext()) {
            shareApp()
        }
    }

    private fun FragmentSettingBinding.setupSwitchListeners() {
        switchMusic.onClick(requireContext()) {
            toggleMusic()
        }

        switchSound.onClick(requireContext(), noplay = true) {
            toggleSound()
        }
    }

    private fun FragmentSettingBinding.setupScreenModeListeners() {
        horizontal.onClick(requireContext()) {
            setScreenMode(isVertical = false)
        }

        vertical.onClick(requireContext()) {
            setScreenMode(isVertical = true)
        }
    }

    private fun FragmentSettingBinding.toggleMusic() {
        val currentStatus = sharedPreferences.isMusic()
        val newStatus = !currentStatus

        // Lưu vào SharedPreferences
        sharedPreferences.setMusic(newStatus)

        // Toggle MusicLocal
        MusicLocal.toggle(requireContext(), newStatus)
        if (newStatus) {
            MusicLocal.play(requireContext())
        } else {
            MusicLocal.pause()
        }

        // Update UI
        updateSwitchIcon(switchMusic, newStatus)
    }

    private fun FragmentSettingBinding.toggleSound() {
        val currentSound = sharedPreferences.isSound()
        val newSound = !currentSound

        // Lưu vào SharedPreferences
        sharedPreferences.setSound(newSound)

        // Update UI
        updateSwitchIcon(switchSound, newSound)
    }

    private fun updateSwitchIcon(imageView: ImageView, isOn: Boolean) {
        val iconRes = if (isOn) R.drawable.ic_switch_on else R.drawable.ic_switch_off
        imageView.setImageResource(iconRes)
    }

    private fun FragmentSettingBinding.setScreenMode(isVertical: Boolean) {
        if (sharedPreferences.isRotate() == isVertical) return

        sharedPreferences.setRotate(isVertical)
        updateScreenModeUI(isVertical)
    }

    private fun FragmentSettingBinding.updateScreenModeUI(isVertical: Boolean) {
        // Reset tất cả
        listOf(strokerHorizontal, strokerVertical).forEach {
            it.applyStrokeSelected(false)
        }
        listOf(selectHorizontal, selectVertical).forEach {
            it.applySelectImage(false)
        }

        // Chọn mode hiện tại
        if (isVertical) {
            strokerVertical.applyStrokeSelected(true)
            selectVertical.applySelectImage(true)
        } else {
            strokerHorizontal.applyStrokeSelected(true)
            selectHorizontal.applySelectImage(true)
        }
    }

    override fun observeData() {}

    override fun bindViewModel() {}
}