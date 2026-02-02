package com.example.basefragment.ui.main.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.applySelectImage
import com.example.basefragment.core.extention.applyStrokeSelected
import com.example.basefragment.core.extention.gone
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.policy
import com.example.basefragment.core.extention.popBack
import com.example.basefragment.core.extention.select
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.core.extention.setTextActionBar
import com.example.basefragment.core.extention.shareApp
import com.example.basefragment.core.extention.toLangFromSetting
import com.example.basefragment.core.extention.visible
import com.example.basefragment.core.helper.RateHelper
import com.example.basefragment.databinding.FragmentSettingBinding
import com.example.basefragment.utils.music.MusicLocal
import com.example.basefragment.utils.state.RateState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding, SettingViewModel>(
    FragmentSettingBinding::inflate, SettingViewModel::class.java
) {
    var checkMode = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPressHandler()
    }

    private fun setupBackPressHandler() {
        // Handle back button để quay về Home
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
            checkStatus(switchMusic,true)
            checkStatus(switchSound,false)
            if (sharedPreferences.isRateRequest()) {
                btnRate.gone()
            } else {
                btnRate.visible()
            }
             checkMode = sharedPreferences.isRotate()
            changemode(checkMode)

            actionBar.tvCenter.select()
            actionBar.apply {
                setImageActionBar(btnActionBarLeft, R.drawable.back_app)
                setTextActionBar(tvCenter, getString(R.string.settings))
            }
        }

    }
    private fun changemode(  check: Boolean){
        binding.apply {
        val (strokeView, selectView) = if (!check) {
            strokerHorizontal to selectHorizontal
        } else {
            strokerVertical to selectVertical
        }

        listOf(strokerHorizontal, strokerVertical)
            .forEach { it.applyStrokeSelected(false) }

        listOf(selectHorizontal, selectVertical)
            .forEach { it.applySelectImage(false) }

        strokeView.applyStrokeSelected(true)
        selectView.applySelectImage(true)
    }}
    override fun viewListener() {
        binding.apply {
            // Action bar left button
            actionBar.btnActionBarLeft.onClick(requireContext()) {
                popBack()
            }

            // Navigate đến Language
            btnLang.onClick(requireContext()) {
                toLangFromSetting()
            }

            // Các button setting khác
            btnPolicy.onClick(requireContext()) {
                policy()
            }

            btnRate.onClick(requireContext()) {
                RateHelper.showRateDialog(requireActivity(), sharedPreferences){ state ->
                    if (state != RateState.CANCEL){
                        btnRate.gone()
                        showToast(R.string.have_rated)
                    }
                }
            }
            btnShare.onClick(requireContext()) {
                shareApp()
                // Handle share app
            }
            switchMusic.onClick(requireContext()) {
                updateMusicIcon(switchMusic,true)
            }
            switchSound.onClick(requireContext(), noplay = true) {
                updateMusicIcon(switchSound,false)
            }
            horizontal.onClick(requireContext()) {
                screenMode(false)
            }
            vertical.onClick(requireContext()) {
                screenMode(true)
            }
        }
    }
    private fun updateMusicIcon( musicButtons: ImageView, isMusic: Boolean= false) {
        if (isMusic) {
            var playing = MusicLocal.status(requireContext())
            playing = ! playing
            MusicLocal.toggle(requireContext(),playing)
            if (!playing) MusicLocal.pause() else MusicLocal.play(requireContext())
            musicButtons.setImageResource(if (playing) R.drawable.ic_switch_on else R.drawable.ic_switch_off)
        } else {
            val sound = sharedPreferences.isSound().not()
            sharedPreferences.setSound(sound)
            musicButtons.setImageResource(if (sound) R.drawable.ic_switch_on else R.drawable.ic_switch_off)
        }
    }
    private fun checkStatus(musicButtons: ImageView, isMusic: Boolean= false) {
        if (isMusic) {
            val playing = MusicLocal.status(requireContext())
            musicButtons.setImageResource(if (playing) R.drawable.ic_switch_on else R.drawable.ic_switch_off)
        }
        else{
            val sound = sharedPreferences.isSound()
            musicButtons.setImageResource(if (sound) R.drawable.ic_switch_on else R.drawable.ic_switch_off)
        }
    }
    private fun screenMode(checkMode: Boolean=false) {
        if (sharedPreferences.isRotate() == checkMode) return
        changemode(checkMode)
        sharedPreferences.setRotate(checkMode)
    }
    override fun observeData() {
        // Observe ViewModel data
    }

    override fun bindViewModel() {
        // Bind ViewModel
    }
}