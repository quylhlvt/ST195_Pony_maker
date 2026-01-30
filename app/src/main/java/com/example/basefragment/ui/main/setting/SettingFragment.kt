package com.example.basefragment.ui.main.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
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
        if (sharedPreferences.isRateRequest()) {
            binding.btnRate.gone()
        } else {
            binding.btnRate.visible()
        }
        binding.actionBar.tvCenter.select()
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
            setTextActionBar(tvCenter, getString(R.string.settings))
        }
    }

    override fun viewListener() {
        binding.apply {
            // Action bar left button
            actionBar.btnActionBarLeft.onClick {
                popBack()
            }

            // Navigate đến Language
            btnLang.onClick {
                toLangFromSetting()
            }

            // Các button setting khác
            btnPolicy.onClick {
                policy()
            }

            btnRate.onClick {
                RateHelper.showRateDialog(requireActivity(), sharedPreferences){ state ->
                    if (state != RateState.CANCEL){
                        btnRate.gone()
                        showToast(R.string.have_rated)
                    }
                }
            }

            btnShare.onClick {
                shareApp()
                // Handle share app
            }
        }
    }

    override fun observeData() {
        // Observe ViewModel data
    }

    override fun bindViewModel() {
        // Bind ViewModel
    }
}