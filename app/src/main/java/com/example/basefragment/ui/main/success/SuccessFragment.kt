package com.example.basefragment.ui.main.success

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.databinding.FragmentPlayMutiplayBinding
import com.example.basefragment.databinding.FragmentPlayMutiplayBinding.inflate
import com.example.basefragment.databinding.FragmentSuccessBinding
import com.example.basefragment.ui.main.playmutiplay.PlayMultiplayeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class SuccessFragment  : BaseFragment<FragmentSuccessBinding,SuccessViewModel>(FragmentSuccessBinding::inflate,
    SuccessViewModel::class.java
) {
    override fun viewListener() {
    }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentSuccessBinding = FragmentSuccessBinding.inflate(inflater, container, false)

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