package com.example.basefragment.ui.main.play

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.basefragment.R
import com.example.basefragment.core.base.BackPressHandler
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.dialog.ExitDialog
import com.example.basefragment.core.extention.popBack
import com.example.basefragment.core.extention.screenRotation
import com.example.basefragment.core.extention.setForegroundColor
import com.example.basefragment.core.extention.visible
import com.example.basefragment.core.helper.RateHelper.showExitDialogHor
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.FragmentPrePlayBinding
import com.example.basefragment.utils.state.ExitState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PrePlayFragment : BaseFragment<FragmentPrePlayBinding, PlayViewModel>(
    FragmentPrePlayBinding::inflate, PlayViewModel::class.java
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

    override fun onBackPressed(): Boolean {
        if (isAnimationRunning) return true
        showExitDialog()
        return true
    }

    // PrePlayFragment
    private fun showExitDialog() {
        showExitDialogHor(requireActivity()) { state ->
            if (state != ExitState.EXIT) {
                requireActivity().finish()
            }
            // User cancel -> Không làm gì (ở lại app)
        }
    }

    override fun viewListener() {
        binding.apply {

//            imgBack.onClick(requireContext()) {
//                if (!isAnimationRunning) {
//                    finishGame()
//                }
//            }
        }
    }

    override fun setupPreViews() {
//        resetGameState()
//        resetAllAnimations()
        isAutoMode = arguments?.getBoolean("isAutoMode", false) ?: false
        if (isAutoMode) {
            player1List = randomList()
            player2List = randomList()
        } else {
            player2List = arguments?.getParcelableArray("player1List")?.map { it as ManualModel }
                ?: emptyList()

            player1List = arguments?.getParcelableArray("player2List")?.map { it as ManualModel }
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
        Glide.with(requireContext()).asGif().load(R.raw.animation)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).preload()
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): FragmentPrePlayBinding = FragmentPrePlayBinding.inflate(inflater, container, false)

    override fun initView() {
        isAnimationRunning = true

        checkStatusPreView(false)
        setupRecyclerViews()
        updateWinIndicators()
        // ✅ KHÔNG gọi updateTurnState() ở đây - sẽ gọi sau animation

        binding.apply {
            imgprePlayer1.visibility = View.GONE
            txtprePlayer1.visibility = View.GONE
            imgprePlayer2.visibility = View.GONE
            txtprePlayer2.visibility = View.GONE
            imgSet1.visibility = View.GONE
            imgSet2.visibility = View.GONE
            imgVs.visibility = View.GONE

            recyclePlay.visibility = View.GONE
            recyclePlay2.visibility = View.GONE
            materialPlay1.visibility = View.GONE
            materialPlay2.visibility = View.GONE

            lifecycleScope.launch {
                delay(300)

                // Animation vào cho linearPrePlay1 (từ trái)
                imgprePlayer1.post {
                    imgprePlayer1.translationY = -imgprePlayer1.height.toFloat()
                    imgprePlayer1.alpha = 0f
                    imgprePlayer1.visible()

                    imgprePlayer1.animate().translationY(0f).alpha(1f).setDuration(1000)
                        .setInterpolator(android.view.animation.DecelerateInterpolator()).start()
                }

                txtprePlayer1.post {
                    txtprePlayer1.alpha = 0f
                    txtprePlayer1.visible()

                    txtprePlayer1.animate().alpha(1f).setDuration(1000).start()
                }

                // Animation cho imgSet1 (từ trái)
                imgSet1.post {
                    imgSet1.translationX = -imgSet1.width.toFloat()
                    imgSet1.alpha = 0f
                    imgSet1.visible()

                    imgSet1.animate().translationX(0f).alpha(1f).setDuration(800)
                        .setInterpolator(android.view.animation.DecelerateInterpolator()).start()
                }


                // Animation cho VS icon (zoom in)


                // Animation cho imgSet2 (từ phải)
                imgSet2.post {
                    imgSet2.translationX = imgSet2.width.toFloat()
                    imgSet2.alpha = 0f
                    imgSet2.visible()

                    imgSet2.animate().translationX(0f).alpha(1f).setDuration(800)
                        .setInterpolator(android.view.animation.DecelerateInterpolator()).start()
                }
                delay(200)
                imgVs.post {
                    imgVs.scaleX = 0f
                    imgVs.scaleY = 0f
                    imgVs.alpha = 0f
                    imgVs.visible()

                    imgVs.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(1200)
                        .setInterpolator(android.view.animation.OvershootInterpolator()).start()
                }
                // Animation cho imgprePlayer2 (từ dưới lên - phải)
                imgprePlayer2.post {
                    imgprePlayer2.translationY = imgprePlayer2.height.toFloat()
                    imgprePlayer2.alpha = 0f
                    imgprePlayer2.visible()

                    imgprePlayer2.animate().translationY(0f).alpha(1f).setDuration(1000)
                        .setInterpolator(android.view.animation.DecelerateInterpolator()).start()
                }

                txtprePlayer2.post {
                    txtprePlayer2.alpha = 0f
                    txtprePlayer2.visible()

                    txtprePlayer2.animate().alpha(1f).setDuration(1000).start()
                }

                // ========== PHASE 2: Giữ pre-play hiển thị ==========
                delay(2000) // Hiển thị 2 giây

                // ========== PHASE 3: Ẩn pre-play views ==========

                // Ẩn player 1 section
                imgprePlayer1.animate().translationY(-imgprePlayer1.height.toFloat()).alpha(0f)
                    .setDuration(800)
                    .setInterpolator(android.view.animation.AccelerateInterpolator()).start()

                txtprePlayer1.animate().alpha(0f).setDuration(800).start()

                // Ẩn player 2 section
                imgprePlayer2.animate().translationY(imgprePlayer2.height.toFloat()).alpha(0f)
                    .setDuration(800)
                    .setInterpolator(android.view.animation.AccelerateInterpolator()).start()

                txtprePlayer2.animate().alpha(0f).setDuration(800).start()

                // Ẩn set images và VS
                imgSet1.animate().translationX(-imgSet1.width.toFloat()).alpha(0f).setDuration(800)
                    .setInterpolator(android.view.animation.AccelerateInterpolator()).start()

                imgSet2.animate().translationX(imgSet2.width.toFloat()).alpha(0f).setDuration(800)
                    .setInterpolator(android.view.animation.AccelerateInterpolator()).start()

                imgVs.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(800)
                    .setInterpolator(android.view.animation.AnticipateInterpolator()).start()


                delay(800)

                // ========== PHASE 4: Hiện game boards ==========

                // Player 2 board (từ trên xuống)
                materialPlay2.post {
                    materialPlay2.translationY = -materialPlay2.height.toFloat()
                    materialPlay2.alpha = 0f
                    materialPlay2.visible()

                    materialPlay2.animate().translationY(0f).alpha(1f).setDuration(1000)
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .withEndAction {
                            recyclePlay2.visible()
                        }.start()
                }

                delay(200)

                // Player 1 board (từ dưới lên)
                materialPlay1.post {
                    materialPlay1.translationY = materialPlay1.height.toFloat()
                    materialPlay1.alpha = 0f
                    materialPlay1.visible()

                    materialPlay1.animate().translationY(0f).alpha(1f).setDuration(1000)
                        .setInterpolator(android.view.animation.DecelerateInterpolator())
                        .withEndAction {
                            recyclePlay.visible()
                        }.start()
                }
                delay(200)
                linearPrePlay.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(800)
                    .setInterpolator(android.view.animation.AnticipateInterpolator()).start()
                delay(2200)

                // ✅ Animation hoàn tất - mở khóa
                isAnimationRunning = false
                checkStatusPreView(true)

                currentPlayer = 1
                updateTurnState()
            }
        }
    }

    private fun checkStatusPreView(status: Boolean) {
        binding.apply {
//            imgBack.isEnabled = status
//            linearPlayerName.isEnabled = status
//            linearPlay.isEnabled = status
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
                findNavController().navigate(R.id.action_prePlay_to_success, bundle)
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
                findNavController().navigate(R.id.action_prePlay_to_success, bundle)
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