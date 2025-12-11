package com.example.basefragment.ui.main.createPony

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.databinding.FragmentChoosePonyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChoosePonyFragment : BaseFragment<FragmentChoosePonyBinding, ChoosePonyViewModel>( FragmentChoosePonyBinding::inflate, ChoosePonyViewModel::class.java) {
    override fun viewListener() {

    }


    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentChoosePonyBinding = FragmentChoosePonyBinding.inflate(inflater, container, false)

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