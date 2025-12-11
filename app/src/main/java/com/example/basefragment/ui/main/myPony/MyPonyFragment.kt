package com.example.basefragment.ui.main.myPony

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.databinding.FragmentMyPonyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPonyFragment : BaseFragment<FragmentMyPonyBinding>() {

    private val viewModel: MyPonyViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMyPonyBinding = FragmentMyPonyBinding.inflate(inflater, container, false)

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
}