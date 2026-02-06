package com.example.basefragment.ui.main.success

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.screenRotation
import com.example.basefragment.databinding.FragmentSuccessBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class SuccessFragment : BaseFragment<FragmentSuccessBinding, SuccessViewModel>(
    FragmentSuccessBinding::inflate, SuccessViewModel::class.java
) {
    private var isWin = false
    private var isAuto = false
    private var isplaymulti = false

    override fun viewListener() {
        binding.apply {
            btnHome.onClick(requireContext()) {
                // ✅ Navigate về Home và clear back stack
                findNavController().navigate(
                    R.id.action_success_to_home,
                    null,
                    androidx.navigation.NavOptions.Builder().setPopUpTo(R.id.homeFragment, true)
                        .build()
                )
            }

            btnRestart.onClick(requireContext()) {
                when {
                    isAuto -> {
                        val bundle = Bundle().apply {
                            putBoolean("isAutoMode", true)
                        }
                        val targetDestination = if (sharedPreferences.isRotate()) {
                            R.id.action_success_to_auto_Ver
                        } else {
                            R.id.action_success_to_auto
                        }

                        findNavController().navigate(
                            targetDestination, bundle
                        )
                    }

                    isplaymulti -> {
                        findNavController().navigate(
                            R.id.action_success_to_multiplayer
                        )
                    }

                    else -> {
                        val bundle = Bundle().apply {
                            putBoolean("isSuccess", true)

                        }
                        findNavController().navigate(R.id.action_success_to_manual, bundle)
                    }
                }
            }
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): FragmentSuccessBinding = FragmentSuccessBinding.inflate(inflater, container, false)

    override fun setupPreViews() {
        super.setupPreViews()
        isWin = arguments?.getBoolean("win", false) ?: false
        isAuto = arguments?.getBoolean("auto", false) ?: false
        isplaymulti = arguments?.getBoolean("playmulti", false) ?: false

        screenRotation()
    }

    override fun initView() {
        binding.apply {
            val (imageRes, playerText) = when {
                isWin -> R.drawable.img_success_play2 to getString(R.string.player_2)
                isplaymulti -> R.drawable.img_success_mutial to getString(R.string.best_player)
                else -> R.drawable.img_success_play1 to getString(R.string.player_1)
            }

            imgwin.setImageResource(imageRes)
            txtPlayer.text = playerText
        }

//        lifecycleScope.

//        binding.textView.text = "Home Fragment"
//        binding.btnTest.setOnClickListener {
//            showSnackbar("Xin chào từ Home!")
//        }
    }

    override fun observeData() {
//        viewModel.data.observe(viewLifecycleOwner) { text ->
//            binding.textView.text = text
//        }
    }

    override fun bindViewModel() {
        /// load data local và api
//        lifecycleScope.launch {
//            viewModel.loadLocalData()
//
//        }
    }

}