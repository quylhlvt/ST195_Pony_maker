package com.example.basefragment.ui.main.play

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.popBack
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.FragmentPlayBinding
import com.example.basefragment.ui.main.manual.ManualViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayFragment : BaseFragment<FragmentPlayBinding, PlayViewModel>(
    FragmentPlayBinding::inflate,
    PlayViewModel::class.java
) {
    // ✅ Sử dụng activityViewModels để access shared ViewModel
    private val manualViewModel: ManualViewModel by activityViewModels()
    private var player1Wins = 0
    private var player2Wins = 0
    private var currentPlayer = 1 // 1 for Player 1, 2 for Player 2
    private var isGameOver = false
    private lateinit var player1List: List<ManualModel>
    private lateinit var player2List: List<ManualModel>
    private val player1Adapter by lazy {
        PlayAdapter(requireContext(), isPlayer1 = true).apply {
            onItemClick = { item, position ->
                handlePlayer1Click(item, position)
            }
        }
    }

    private val player2Adapter by lazy {
        PlayAdapter(requireContext(), isPlayer1 = false).apply {
            onItemClick = { item, position ->
                handlePlayer2Click(item, position)
            }
        }
    }
    override fun viewListener() {}
    override fun setupPreViews() {
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

        requireActivity().requestedOrientation = if (sharedPreferences.isRotate()) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    private fun setupRecyclerViews() {
        binding.apply {
            // Setup Player 1 RecyclerView
            recyclePlay.apply {
                adapter = player1Adapter
                layoutManager = GridLayoutManager(requireContext(), 3).apply {
                    isItemPrefetchEnabled = false
                }
                setHasFixedSize(true)
                itemAnimator = null
            }

            // Setup Player 2 RecyclerView
            recyclePlay2
                .apply {
                adapter = player2Adapter
                layoutManager = GridLayoutManager(requireContext(), 3).apply {
                    isItemPrefetchEnabled = false
                }
                setHasFixedSize(true)
                itemAnimator = null
            }

            // Set initial data
            player1Adapter.submitList(player1List)
            player2Adapter.submitList(player2List)
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentPlayBinding = FragmentPlayBinding.inflate(inflater, container, false)

    override fun initView() {

        setupBackPressHandler()
        setupRecyclerViews()
        updateWinIndicators()

        binding.imgBack.onClick(requireContext()) {
            finishGame()
        }
    }
    private fun handlePlayer1Click(item: ManualModel, position: Int) {
        if (isGameOver || currentPlayer != 2) return

        if (item.bomb) {
            // Player 2 clicked a bomb on Player 1's board
            showToast("Player 2 hit a bomb!")
            player1Wins++
            updateWinIndicators()
            checkGameOver()
            if (!isGameOver) {
                startNewRound()
            }
        } else {
            // Safe - switch turn
            currentPlayer = 1
            showToast("Safe! Player 1's turn")
        }
    }
    private fun checkGameOver() {
        if (player1Wins >= 3) {
            isGameOver = true
            showToast("Player 1 Wins!")
            // You can show a dialog or navigate to result screen here
        } else if (player2Wins >= 3) {
            isGameOver = true
            showToast("Player 2 Wins!")
            // You can show a dialog or navigate to result screen here
        }
    }
    private fun startNewRound() {
        // Reset adapters for new round
        player1Adapter.resetClicked()
        player2Adapter.resetClicked()

        // Optionally shuffle the lists for new round
        // You might want to add this logic

        showToast("New Round! Player ${if (currentPlayer == 1) "1" else "2"}'s turn")
    }

    private fun handlePlayer2Click(item: ManualModel, position: Int) {
        if (isGameOver || currentPlayer != 1) return

        if (item.bomb) {
            // Player 1 clicked a bomb on Player 2's board
            showToast("Player 1 hit a bomb!")
            player2Wins++
            updateWinIndicators()
            checkGameOver()
            if (!isGameOver) {
                startNewRound()
            }
        } else {
            // Safe - switch turn
            currentPlayer = 2
            showToast("Safe! Player 2's turn")
        }
    }

    private fun updateWinIndicators() {
        binding.apply {
            // Update Player 1 wins
            val winImages1 = listOf(imgWin11, imgWin12, imgWin13)
            winImages1.forEachIndexed { index, imageView ->
                imageView.setImageResource(
                    if (index < player1Wins) {
                        R.drawable.img_heart_win_number_true_play1
                    } else {
                        R.drawable.img_heart_win_number_false
                    }
                )
            }

            // Update Player 2 wins
            val winImages2 = listOf(imgWin21, imgWin22, imgWin23)
            winImages2.forEachIndexed { index, imageView ->
                imageView.setImageResource(
                    if (index < player2Wins) {
                        R.drawable.img_heart_win_number_true_play2
                    } else {
                        R.drawable.img_heart_win_number_false
                    }
                )
            }
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
        manualViewModel.resetAll()
        popBack()
    }




    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    override fun bindViewModel() {}
}