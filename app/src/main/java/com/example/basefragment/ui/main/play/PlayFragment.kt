package com.example.basefragment.ui.main.play

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.FragmentPlayBinding
import com.example.basefragment.ui.main.manual.ManualViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayFragment : BaseFragment<FragmentPlayBinding, PlayViewModel>(
    FragmentPlayBinding::inflate,
    PlayViewModel::class.java
) {
    private val manualViewModel: ManualViewModel by activityViewModels()

    private lateinit var player1List: List<ManualModel>
    private lateinit var player2List: List<ManualModel>

    override fun viewListener() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentPlayBinding = FragmentPlayBinding.inflate(inflater, container, false)

    override fun initView() {
        setupBackPressHandler()

        player1List = arguments
            ?.getParcelableArray("player1List")
            ?.map { it as ManualModel }
            ?: emptyList()

        player2List = arguments
            ?.getParcelableArray("player2List")
            ?.map { it as ManualModel }
            ?: emptyList()

        Log.d("PlayFragment", "Player 1 items: $player1List")
        Log.d("PlayFragment", "Player 2 items: $player2List")

        binding.imgTvCenter.onClick(requireContext()) {
            finishGame()
        }
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
        viewLifecycleOwner.lifecycleScope.launch {
            // ✅ Bước 1: Reset ViewModel
            manualViewModel.resetAllLists()

            // ✅ Bước 2: Delay nhỏ để đảm bảo Flow emit
            delay(50)

            // ✅ Bước 3: Set flag
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set("should_reset", true)

            // ✅ Bước 4: PopBack
            findNavController().popBackStack(R.id.manualFragment, false)
        }
    }

    override fun bindViewModel() {}
}