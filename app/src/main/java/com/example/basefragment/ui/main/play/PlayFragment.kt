package com.example.basefragment.ui.main.play

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.FragmentAutoBinding
import com.example.basefragment.databinding.FragmentAutoBinding.inflate
import com.example.basefragment.databinding.FragmentPlayBinding
import com.example.basefragment.ui.main.auto.AutoViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class PlayFragment : BaseFragment<FragmentPlayBinding, PlayViewModel>(FragmentPlayBinding::inflate,
    PlayViewModel::class.java
) {
    private lateinit var selectedList: List<ManualModel>

    override fun viewListener() {

    }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentPlayBinding = FragmentPlayBinding.inflate(inflater, container, false)

    override fun initView() {
        selectedList = arguments
            ?.getParcelableArray("selectedList")
            ?.map { it as ManualModel }
            ?: emptyList()
        binding.txt.text ="${selectedList}"


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