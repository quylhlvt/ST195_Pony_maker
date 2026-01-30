package com.example.basefragment.ui.main.auto

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.databinding.FragmentAutoBinding
import com.example.basefragment.databinding.FragmentManualBinding
import com.example.basefragment.databinding.FragmentManualBinding.inflate
import com.example.basefragment.ui.main.manual.ManualViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class AutoFragment  : BaseFragment<FragmentAutoBinding, AutoViewModel>(FragmentAutoBinding::inflate,
    AutoViewModel::class.java
) {
    override fun viewListener() {

    }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentAutoBinding = FragmentAutoBinding.inflate(inflater, container, false)

    override fun initView() {

        requireActivity().requestedOrientation = if (sharedPreferences.isRotate()) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

//        lifecycleScope.

//        binding.textView.text = "Home Fragment"
//        binding.btnTest.setOnClickListener {
//            showSnackbar("Xin chào từ Home!")
//        }
    }
    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation =
            if (sharedPreferences.isRotate())
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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