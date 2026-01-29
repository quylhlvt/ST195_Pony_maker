package com.example.basefragment.ui.main.playmutiplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.databinding.FragmentMultiplayerBinding
import com.example.basefragment.databinding.FragmentPlayMutiplayBinding


class PlayMultiplayerFragment : BaseFragment<FragmentPlayMutiplayBinding,PlayMultiplayeViewModel>(FragmentPlayMutiplayBinding::inflate,
    PlayMultiplayeViewModel::class.java
) {
    override fun viewListener() {

    }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentPlayMutiplayBinding = FragmentPlayMutiplayBinding.inflate(inflater, container, false)

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