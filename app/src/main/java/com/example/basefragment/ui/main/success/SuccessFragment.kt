package com.example.basefragment.ui.main.success

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.popBack
import com.example.basefragment.core.extention.screenRotation
import com.example.basefragment.databinding.FragmentSuccessBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class SuccessFragment : BaseFragment<FragmentSuccessBinding, SuccessViewModel>(
    FragmentSuccessBinding::inflate,
    SuccessViewModel::class.java
) {
    private var isWin = false
    private var isAuto = false

    override fun viewListener() {
        binding.apply {

            btnHome.onClick(requireContext()) {
                popBack()
            }
            btnRestart.onClick(requireContext()) {
                if (!isAuto) {
                    findNavController().navigate(R.id.action_success_to_manual)
                } else {
                    val bundle = Bundle().apply {
                        putBoolean("isAutoMode", true)
                    }
                    if (!sharedPreferences.isRotate())
                        findNavController().navigate(R.id.action_success_to_auto, bundle)
                    else
                        findNavController().navigate(R.id.action_success_to_auto_Ver, bundle)

                }
            }
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentSuccessBinding = FragmentSuccessBinding.inflate(inflater, container, false)

    override fun setupPreViews() {
        super.setupPreViews()
        isWin = arguments?.getBoolean("win", false) ?: false
        isAuto = arguments?.getBoolean("auto", false) ?: false
        screenRotation()
    }

    override fun initView() {
        binding.apply {
            imgwin.setImageResource(if (!isWin) R.drawable.img_success_play1 else R.drawable.img_success_play2)
            txtPlayer.text =
                if (!isWin) getString(R.string.player_1) else getString(R.string.player_2)
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