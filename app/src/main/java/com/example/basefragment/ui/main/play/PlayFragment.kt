package com.example.basefragment.ui.main.play

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.popBack
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
    private lateinit var player1List: List<ManualModel>
    private lateinit var player2List: List<ManualModel>
    override fun viewListener() {

    }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentPlayBinding = FragmentPlayBinding.inflate(inflater, container, false)

    override fun initView() {
        player1List = arguments
            ?.getParcelableArray("player1List")
            ?.map { it as ManualModel }
            ?: emptyList()

        player2List = arguments
            ?.getParcelableArray("player2List")
            ?.map { it as ManualModel }
            ?: emptyList()

        // Log để kiểm tra
        Log.d("PlayFragment", "Player 1 items: ${player1List}")
        Log.d("PlayFragment", "Player 2 items: ${player2List}")


    binding.imgTvCenter.onClick(requireContext()){
        popBack()
    }
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