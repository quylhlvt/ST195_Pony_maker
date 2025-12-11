package com.example.basefragment.ui.onboarding.permission

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.databinding.FragmentPermissionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionFragment : BaseFragment<FragmentPermissionBinding>() {

    private val viewModel: PermissionViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentPermissionBinding = FragmentPermissionBinding.inflate(inflater, container, false)

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