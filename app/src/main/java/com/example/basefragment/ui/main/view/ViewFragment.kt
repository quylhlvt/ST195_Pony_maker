package com.example.basefragment.ui.main.view

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.databinding.FragmentQuickBinding
import com.example.basefragment.databinding.FragmentQuickBinding.inflate
import com.example.basefragment.databinding.FragmentViewBinding
import com.example.basefragment.ui.main.quick.QuickViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewFragment  : BaseFragment<FragmentViewBinding, ViewViewModel>( FragmentViewBinding::inflate, ViewViewModel::class.java) {
    override fun viewListener() {


    }


    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentViewBinding = FragmentViewBinding.inflate(inflater, container, false)

    override fun initView() {
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
    }
}