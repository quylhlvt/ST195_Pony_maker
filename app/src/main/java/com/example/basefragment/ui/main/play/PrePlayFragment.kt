package com.example.basefragment.ui.main.play

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.popBack
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.FragmentPlayBinding
import com.example.basefragment.databinding.FragmentPlayBinding.inflate
import com.example.basefragment.databinding.FragmentPrePlayBinding
import com.example.basefragment.ui.main.manual.ManualViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
@AndroidEntryPoint
class PrePlayFragment  : BaseFragment<FragmentPrePlayBinding, PlayViewModel>(
    FragmentPrePlayBinding::inflate,
    PlayViewModel::class.java
) {
    // ✅ Sử dụng activityViewModels để access shared ViewModel
    private val manualViewModel: ManualViewModel by activityViewModels()

    private lateinit var player1List: List<ManualModel>
    private lateinit var player2List: List<ManualModel>

    override fun viewListener() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentPrePlayBinding = FragmentPrePlayBinding.inflate(inflater, container, false)

    override fun initView() {
        setupBackPressHandler()

        // ✅ Nhận data từ Bundle
        player1List = arguments
            ?.getParcelableArray("player1List")
            ?.map { it as ManualModel }
            ?: emptyList()

        player2List = arguments
            ?.getParcelableArray("player2List")
            ?.map { it as ManualModel }
            ?: emptyList()

        Log.d("PlayFragment", "Player 1 selected: ${player1List.count { it.bomb }}")
        Log.d("PlayFragment", "Player 2 selected: ${player2List.count { it.bomb }}")

//        binding.imgTvCenter.onClick(requireContext()) {
//            finishGame()
//        }
    }

    override fun observeData() {}

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishGame()
                }
            }
        )
    }
    private fun finishGame() {
        manualViewModel.resetAll()
        popBack()
    }

    override fun bindViewModel() {}
}