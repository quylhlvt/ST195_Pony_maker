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
import com.example.basefragment.core.extention.gone
import com.example.basefragment.core.extention.screenRotation
import com.example.basefragment.core.extention.setForegroundColor
import com.example.basefragment.core.extention.visible
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayFragment : BaseFragment<FragmentPlayBinding, PlayViewModel>(
    FragmentPlayBinding::inflate,
    PlayViewModel::class.java
) {
    private val manualViewModel: ManualViewModel by activityViewModels()
    private var player1Wins = 0
    private var player2Wins = 0
    private var currentPlayer = 1  // ✅ Đảm bảo Player 1 chơi trước
    private var isGameOver = false
    private var isAutoMode = false
    private var isAnimationRunning = true // ✅ Flag chặn click khi animation
    private var isProcessingClick = false // ✅ Flag chặn click liên tục
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

    override fun viewListener() {
        binding.apply {
            imgBack.onClick(requireContext()) {
                if (!isAnimationRunning) {
                    finishGame()
                }
            }
        }
    }

    override fun setupPreViews() {
        isAutoMode = arguments?.getBoolean("isAutoMode", false) ?: false
        if (isAutoMode) {
            player1List = randomList()
            player2List = randomList()
            Log.d("PlayFragment", "Auto Mode - Random lists generated")
        } else {
            player1List = arguments
                ?.getParcelableArray("player1List")
                ?.map { it as ManualModel }
                ?: emptyList()

            player2List = arguments
                ?.getParcelableArray("player2List")
                ?.map { it as ManualModel }
                ?: emptyList()
        }
        Log.d("PlayFragment", "Player 1 selected: ${player1List.count { it.bomb }}")
        Log.d("PlayFragment", "Player 2 selected: ${player2List.count { it.bomb }}")

        requireActivity().requestedOrientation = if (sharedPreferences.isRotate()) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
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
        isAnimationRunning = true // ✅ Bắt đầu chặn
        checkStatusPreView(false)
        setupBackPressHandler()
        setupRecyclerViews()
        updateWinIndicators()
        // ✅ KHÔNG gọi updateTurnState() ở đây - sẽ gọi sau animation

        binding.apply {
            lifecycleScope.launch {
                delay(500)

                // Animation vào cho linearPrePlay1 (từ trái)
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

                // Animation vào cho linearPrePlay2 (từ phải)
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

                // ✅ Animation hoàn tất - mở khóa
                isAnimationRunning = false
                checkStatusPreView(true)
                currentPlayer = 1  // ✅ Đảm bảo Player 1 chơi trước sau animation
                updateTurnState() // ✅ Gọi updateTurnState sau khi animation xong
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

    // ✅ Player 2 chọn vào bàn của Player 1
    private fun handlePlayer1Click(item: ManualModel, position: Int) {
        // ✅ CHẶN 1: Đang animation
        if (isAnimationRunning) {
            return
        }

        // ✅ CHẶN 2: Đang xử lý click trước đó
        if (isProcessingClick) {
            return
        }

        // ✅ CHẶN 3: Game đã kết thúc
        if (isGameOver) {
            return
        }

        // ✅ CHẶN 4: Không đúng lượt (Player 2 chọn bàn Player 1)
        if (currentPlayer != 1) {
            showToast("Đang là lượt của Player $currentPlayer!")
            return
        }

        // ✅ Đánh dấu đang xử lý
        isProcessingClick = true

        // Xử lý logic game
        if (item.bomb) {
            showToast("Player 2 trúng bom! Player 1 ghi điểm!")
            player1Wins++
            updateWinIndicators()
            checkGameOver()
        } else {
            showToast("An toàn!")
        }

        // ✅ Delay chuyển lượt
        if (!isGameOver) {
            Handler(Looper.getMainLooper()).postDelayed({
                currentPlayer = 2
                updateTurnState()

                isProcessingClick = false // ✅ Mở khóa
            }, 800)
        } else {
            isProcessingClick = false
        }
    }

    // ✅ Player 1 chọn vào bàn của Player 2
    private fun handlePlayer2Click(item: ManualModel, position: Int) {
        // ✅ CHẶN 1: Đang animation
        if (isAnimationRunning) {
            return
        }

        // ✅ CHẶN 2: Đang xử lý click trước đó
        if (isProcessingClick) {
            return
        }

        // ✅ CHẶN 3: Game đã kết thúc
        if (isGameOver) {
            return
        }

        // ✅ CHẶN 4: Không đúng lượt (Player 1 chọn bàn Player 2)
        if (currentPlayer != 2) {
            showToast("Đang là lượt của Player $currentPlayer!")
            return
        }

        // ✅ Đánh dấu đang xử lý
        isProcessingClick = true

        // Xử lý logic game
        if (item.bomb) {
            showToast("Player 1 trúng bom! Player 2 ghi điểm!")
            player2Wins++
            updateWinIndicators()
            checkGameOver()
        } else {
            showToast("An toàn!")
        }

        // ✅ Delay chuyển lượt
        if (!isGameOver) {
            Handler(Looper.getMainLooper()).postDelayed({
                currentPlayer = 1
                updateTurnState()
                isProcessingClick = false // ✅ Mở khóa
            }, 800)
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
                showToast("Lượt Player 2 - Chọn vào bàn Player 1")
                // Player 1 chọn vào bàn Player 2

            } else {
                player1Adapter.setEnabled(false)
                player2Adapter.setEnabled(true)
                recyclePlay.setForegroundColor(R.color.black2)
                recyclePlay2.setForegroundColor(null)
                showToast("Lượt Player 1 - Chọn vào bàn Player 2")
            }
        }
    }

    private fun checkGameOver() {
        if (player1Wins >= 3) {
            isGameOver = true
            showToast("🎉 Player 1 Chiến Thắng! 🎉")
            player1Adapter.setEnabled(false)
            player2Adapter.setEnabled(false)
            binding.recyclePlay.setForegroundColor(null)
            binding.recyclePlay2.setForegroundColor(null)
        } else if (player2Wins >= 3) {
            isGameOver = true
            showToast("🎉 Player 2 Chiến Thắng! 🎉")
            player1Adapter.setEnabled(false)
            player2Adapter.setEnabled(false)
            binding.recyclePlay.setForegroundColor(null)
            binding.recyclePlay2.setForegroundColor(null)
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
        popBack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        screenRotation()
    }

    override fun bindViewModel() {}
}