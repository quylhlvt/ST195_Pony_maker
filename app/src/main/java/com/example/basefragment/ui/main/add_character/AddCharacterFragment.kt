package com.example.basefragment.ui.main.add_character

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.databinding.FragmentAddCharacterBinding
import com.example.basefragment.databinding.FragmentChoosePonyBinding
import com.example.basefragment.databinding.FragmentChoosePonyBinding.inflate
import com.example.basefragment.ui.main.createPony.ChoosePonyViewModel

class AddCharacterFragment : BaseFragment<FragmentAddCharacterBinding, AddCharacterViewModel>( FragmentAddCharacterBinding::inflate, AddCharacterViewModel::class.java) {
    override fun viewListener() {

    }


    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentAddCharacterBinding = FragmentAddCharacterBinding.inflate(inflater, container, false)

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