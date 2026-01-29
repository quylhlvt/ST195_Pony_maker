package com.example.basefragment.ui.main.guide

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.databinding.FragmentAutoBinding
import com.example.basefragment.databinding.FragmentAutoBinding.inflate
import com.example.basefragment.databinding.FragmentGuideBinding
import com.example.basefragment.ui.main.auto.AutoViewModel

class GuideFragment: BaseFragment<FragmentGuideBinding, GuideViewModel>(FragmentGuideBinding::inflate,
    GuideViewModel::class.java
) {
    override fun viewListener() {

    }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentGuideBinding = FragmentGuideBinding.inflate(inflater, container, false)

    override fun initView() {

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