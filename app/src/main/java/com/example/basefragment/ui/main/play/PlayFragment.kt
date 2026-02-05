package com.example.basefragment.ui.main.play

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.popBack
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.FragmentPlayBinding
import com.example.basefragment.ui.main.manual.ManualViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.basefragment.core.base.BackPressHandler
import com.example.basefragment.core.dialog.ExitDialog
import com.example.basefragment.core.dialog.ExitDialogHor
import com.example.basefragment.core.extention.gone
import com.example.basefragment.core.extention.screenRotation
import com.example.basefragment.core.extention.setForegroundColor
import com.example.basefragment.core.extention.visible
import com.example.basefragment.core.helper.RateHelper
import com.example.basefragment.core.helper.RateHelper.showExitDialogHor
import com.example.basefragment.core.helper.RateHelper.showRateDialog
import com.example.basefragment.utils.state.ExitState
import com.example.basefragment.utils.state.RateState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayFragment : BaseFragment<FragmentPlayBinding, PlayViewModel>(
    FragmentPlayBinding::inflate,
    PlayViewModel::class.java
), BackPressHandler {
    private var player1Wins = 0
    private var player2Wins = 0
    private var currentPlayer = 1
    private var isGameOver = false
    private var isAutoMode = false
    private var isAnimationRunning = true
    private var isProcessingClick = false
    private lateinit var player1List: List<ManualModel>
    private lateinit var player2List: List<ManualModel>
    // ✅ Handler để quản lý delay
    private val handler = Handler(Looper.getMainLooper())
    override fun onBackPressed(): Boolean {
        if (isAnimationRunning) return true
        showExitDialog()
        return true
    }
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
    // PlayFragment
    private fun showExitDialog() {
        val dialogExit = ExitDialogHor()
        dialogExit.show(childFragmentManager, "ExitDialogHor")

        dialogExit.onExitClick = {
            finishGame()
        }

        dialogExit.onCancelClick = {
            // Optional: do something on cancel
        }
    }

    override fun viewListener() {
        binding.apply {
            imgBack.onClick(requireContext()) {
                if (!isAnimationRunning) {
//                    showRateDialog(requireActivity(), sharedPreferences) { state ->
//                        if (state != RateState.CANCEL) {
//                            showToast(R.string.have_rated)
//                        }
//                        requireActivity().finish()
//
//                        // User cancel -> Không làm gì (ở lại app)
//                    }
                    showExitDialogHor(requireActivity()) { state ->
                        if (state != ExitState.EXIT) {

                            requireActivity().finish()
                        }
                        // User cancel -> Không làm gì (ở lại app)
                    }
                //                    showExitDialog()
                }
            }
        }
    }

    override fun setupPreViews() {

        isAutoMode = arguments?.getBoolean("isAutoMode", false) ?: false
        if (isAutoMode) {
            player1List = randomList()
            player2List = randomList()
        } else {
            player2List = arguments
                ?.getParcelableArray("player1List")
                ?.map { it as ManualModel }
                ?: emptyList()

            player1List = arguments
                ?.getParcelableArray("player2List")
                ?.map { it as ManualModel }
                ?: emptyList()
        }
        preloadBombGif()

        requireActivity().requestedOrientation = if (sharedPreferences.isRotate()) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    private fun preloadBombGif() {
        Glide.with(requireContext())
            .asGif()
            .load(R.raw.animation)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .preload()
    }

    private fun randomList(): List<ManualModel> {
        val list = mutableListOf<ManualModel>()
        repeat(9) { _ ->
            list.add(ManualModel(false))
        }
        val bombPositions = list.indices.shuffled().take(3)
        bombPositions.forEach { position ->
            list[position] = list[position].copy(bomb = true)
        }
        return list
    }

    private fun setupRecyclerViews() {
        binding.apply {
            recyclePlay.apply {
                adapter = player1Adapter
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                layoutManager =
                    object : androidx.recyclerview.widget.GridLayoutManager(requireContext(), 3) {
                        override fun canScrollVertically(): Boolean = false
                        override fun canScrollHorizontally(): Boolean = false
                    }
                itemAnimator = null
            }

            recyclePlay2.apply {
                adapter = player2Adapter
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                layoutManager =
                    object : androidx.recyclerview.widget.GridLayoutManager(requireContext(), 3) {
                        override fun canScrollVertically(): Boolean = false
                        override fun canScrollHorizontally(): Boolean = false
                    }
                itemAnimator = null
            }

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
        isAnimationRunning = true
        checkStatusPreView(false)
//        setupBackPressHandler()
        setupRecyclerViews()
        updateWinIndicators()

        binding.apply {
            lifecycleScope.launch {
                delay(500)

                linearPrePlay1.post {
                    linearPrePlay1.translationX = -linearPrePlay1.width.toFloat()
                    linearPrePlay1.alpha = 0f
                    linearPrePlay1.visible()

                    linearPrePlay1.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(1500)
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .start()
                }

                linearPrePlay2.post {
                    linearPrePlay2.translationX = linearPrePlay2.width.toFloat()
                    linearPrePlay2.alpha = 0f
                    linearPrePlay2.visible()

                    linearPrePlay2.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(1500)
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .start()
                }

                imgSet1.post {
                    imgSet1.translationY = -imgSet1.height.toFloat()
                    imgSet1.alpha = 0f
                    imgSet1.visible()

                    imgSet1.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(1500)
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .start()
                }

                imgSet2.post {
                    imgSet2.translationY = imgSet2.height.toFloat()
                    imgSet2.alpha = 0f
                    imgSet2.visible()

                    imgSet2.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(1500)
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .start()
                }

                imgVs.post {
                    imgVs.scaleX = 0f
                    imgVs.scaleY = 0f
                    imgVs.alpha = 0f
                    imgVs.visible()

                    imgVs.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(1500)
                        .setInterpolator(android.view.animation.OvershootInterpolator())
                        .start()
                }

                delay(1500 + 2000)

                linearPrePlay1.animate()
                    .translationX(-linearPrePlay1.width.toFloat())
                    .alpha(0f)
                    .setDuration(1500)
                    .setInterpolator(android.view.animation.AccelerateInterpolator())
                    .withEndAction {
                        linearPrePlay1.gone()
                    }
                    .start()

                linearPrePlay2.animate()
                    .translationX(linearPrePlay2.width.toFloat())
                    .alpha(0f)
                    .setDuration(1500)
                    .setInterpolator(android.view.animation.AccelerateInterpolator())
                    .withEndAction {
                        linearPrePlay2.gone()
                    }
                    .start()

                delay(1500)

                linearPrePlay.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(1500)
                    .setInterpolator(android.view.animation.AnticipateInterpolator())
                    .withEndAction {
                        linearPrePlay.gone()
                    }
                    .start()

                delay(500)

                recyclePlay.post {
                    recyclePlay.translationX = -recyclePlay.width.toFloat()
                    recyclePlay.alpha = 0f
                    recyclePlay.visible()

                    recyclePlay.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(1500)
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .start()
                }

                recyclePlay2.post {
                    recyclePlay2.translationX = recyclePlay2.width.toFloat()
                    recyclePlay2.alpha = 0f
                    recyclePlay2.visible()

                    recyclePlay2.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(1500)
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .start()
                }

                materialPlay1.post {
                    materialPlay1.translationY = -materialPlay1.height.toFloat()
                    materialPlay1.alpha = 0f
                    materialPlay1.visible()

                    materialPlay1.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(1500)
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .start()
                }

                materialPlay2.post {
                    materialPlay2.translationY = -materialPlay2.height.toFloat()
                    materialPlay2.alpha = 0f
                    materialPlay2.visible()

                    materialPlay2.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(1500)
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .start()
                }

                delay(1500)

                isAnimationRunning = false
                checkStatusPreView(true)
                currentPlayer = 1
                updateTurnState()
            }
        }
    }

    private fun checkStatusPreView(status: Boolean) {
        binding.apply {
            imgBack.isEnabled = status
            linearPlayerName.isEnabled = status
            linearPlay.isEnabled = status
            recyclePlay.isEnabled = status
            recyclePlay2.isEnabled = status

            if (!status) {
                player1Adapter.setEnabled(false)
                player2Adapter.setEnabled(false)
            }
        }
    }

    private fun handlePlayer1Click(item: ManualModel, position: Int) {
        if (isAnimationRunning) return
        if (isProcessingClick) return
        if (isGameOver) return
        if (currentPlayer != 1) return

        isProcessingClick = true

        if (item.bomb) {
            player1Wins++
            updateWinIndicators()
            checkGameOver()
        }

        if (!isGameOver) {
            handler.postDelayed({
                currentPlayer = 2
                updateTurnState()

                // ✅ Reset processing cho CẢ 2 adapter
                player1Adapter.setProcessing(false)
                player2Adapter.setProcessing(false)
                isProcessingClick = false
            }, 1200)
        } else {
            isProcessingClick = false
        }
    }

    private fun handlePlayer2Click(item: ManualModel, position: Int) {
        if (isAnimationRunning) return
        if (isProcessingClick) return
        if (isGameOver) return
        if (currentPlayer != 2) return

        isProcessingClick = true

        if (item.bomb) {
            player2Wins++
            updateWinIndicators()
            checkGameOver()
        }

        if (!isGameOver) {
            handler.postDelayed({
                currentPlayer = 1
                updateTurnState()

                // ✅ Reset processing cho CẢ 2 adapter
                player1Adapter.setProcessing(false)
                player2Adapter.setProcessing(false)
                isProcessingClick = false
            }, 1200)
        } else {
            isProcessingClick = false
        }
    }

    private fun updateTurnState() {
        binding.apply {
            if (currentPlayer == 1) {
                player1Adapter.setEnabled(true)
                player2Adapter.setEnabled(false)
                recyclePlay2.setForegroundColor(R.color.black2)
                recyclePlay.setForegroundColor(null)
            } else {
                player1Adapter.setEnabled(false)
                player2Adapter.setEnabled(true)
                recyclePlay.setForegroundColor(R.color.black2)
                recyclePlay2.setForegroundColor(null)
            }
        }
    }

    private fun checkGameOver() {
        lifecycleScope.launch {
            var win = false
            if (player1Wins >= 3) {
                isGameOver = true
                player1Adapter.setEnabled(false)
                player2Adapter.setEnabled(false)
                binding.recyclePlay.setForegroundColor(null)
                binding.recyclePlay2.setForegroundColor(null)
                win = true
                delay(2000)
                val bundle = Bundle().apply {
                    putBoolean("win", win)
                    putBoolean("auto", isAutoMode)
                }
                findNavController().navigate(R.id.action_play_to_success, bundle)
            } else if (player2Wins >= 3) {
                isGameOver = true
                player1Adapter.setEnabled(false)
                player2Adapter.setEnabled(false)
                binding.recyclePlay.setForegroundColor(null)
                binding.recyclePlay2.setForegroundColor(null)
                win = false
                delay(2000)
                val bundle = Bundle().apply {
                    putBoolean("win", win)
                    putBoolean("auto", isAutoMode)
                }
                findNavController().navigate(R.id.action_play_to_success, bundle)
            }
        }
    }

    private fun updateWinIndicators() {
        binding.apply {
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
                    if (!isAnimationRunning) {
                        finishGame()
                    }
                }
            }
        )
    }

    private fun finishGame() {
        handler.removeCallbacksAndMessages(null)
        popBack()
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)

        super.onDestroyView()
        screenRotation()
    }

    override fun onDestroy() {
        super.onDestroy()
        screenRotation()
    }


    override fun bindViewModel() {}
}