package com.example.basefragment.ui.onboarding.permission

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.helper.SharedPreferencesManager.setPermissionScreen
import com.example.basefragment.databinding.FragmentPermissionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionFragment : BaseFragment<FragmentPermissionBinding, PermissionViewModel>( FragmentPermissionBinding::inflate, PermissionViewModel::class.java) {
    override fun viewListener() {
        binding.tvContinue.onClick {
            setPermissionScreen(true)
            findNavController().navigate(R.id.action_permission_to_home)
        }
    }


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

    override fun bindViewModel() {
    }
}