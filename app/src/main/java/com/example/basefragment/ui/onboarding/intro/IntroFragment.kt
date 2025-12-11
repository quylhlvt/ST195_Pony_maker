package com.example.basefragment.ui.onboarding.intro

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.helper.SharedPreferencesManager.isPermissionScreen
import com.example.basefragment.databinding.FragmentIntroBinding
import com.example.basefragment.databinding.FragmentPermissionBinding
import com.example.basefragment.ui.onboarding.permission.PermissionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroFragment : BaseFragment<FragmentIntroBinding, IntroViewModel>( FragmentIntroBinding::inflate, IntroViewModel::class.java) {
    override fun viewListener() {
        binding.next.onClick{
            if (!isPermissionScreen()){
                findNavController().navigate(R.id.action_intro_to_permission)
                return@onClick}
            findNavController().navigate(R.id.action_intro_to_home)
        }
    }


    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentIntroBinding = FragmentIntroBinding.inflate(inflater, container, false)

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