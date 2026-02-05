package com.example.basefragment.ui.main.playmutiplay

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.popBack
import com.example.basefragment.core.extention.setForegroundColor
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.FragmentMultiplayerBinding
import com.example.basefragment.databinding.FragmentPlayMutiplayBinding
import com.example.basefragment.ui.main.play.PlayAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint

class PlayMultiplayerFragment : BaseFragment<FragmentPlayMutiplayBinding,PlayMultiplayeViewModel>(FragmentPlayMutiplayBinding::inflate,
    PlayMultiplayeViewModel::class.java
) {
    private var playerWins = 0
    private var isGameOver = false
    private var isProcessingClick = false
    private lateinit var playerList: List<ManualModel>
    private var count=1
    private val handler = Handler(Looper.getMainLooper())

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.onClick(requireContext()) {
                    finishGame()
            }
        }
    }
    private val playerAdapter by lazy {
        MutiPlayAdapter(requireContext()).apply {
            onItemClick = { item, position ->
                handlePlayerClick(item, position)
            }
        }
    }
    private fun FragmentPlayMutiplayBinding.setupActionBar() {
        actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
        }
    }
    private fun handlePlayerClick(item: ManualModel, position: Int) {

        if (isGameOver) {
            return
        }
        if (!item.bomb) {
                playerAdapter.setProcessing(false)
            return
        }
        playerWins++
        if (playerWins >= count) {
            isGameOver = true
            playerAdapter.setEnabled(false)

            // ✅ Navigate sau delay
            handler.postDelayed({
                val bundle = Bundle().apply {
                    putBoolean("playmulti", true)
                }
                findNavController().navigate(R.id.action_playMultiplayer_to_success, bundle)
            }, 2000)
        } else {
            // ✅ Reset processing để cho phép click tiếp
            handler.postDelayed({
                playerAdapter.setProcessing(false)
            }, 900)
        }
    }

    override fun setupPreViews() {
        super.setupPreViews()
        count = arguments?.getInt("count", 1) ?: 1
        playerList = randomList()
        preloadBombGif()
    }
    private fun randomList(): List<ManualModel> {
        val list = mutableListOf<ManualModel>()
        repeat(28) { _ ->
            list.add(ManualModel(false))
        }
        val bombPositions = list.indices.shuffled().take(count)
        bombPositions.forEach { position ->
            list[position] = list[position].copy(bomb = true)
        }
        return list
    }
    private fun preloadBombGif() {
        Glide.with(requireContext())
            .asGif()
            .load(R.raw.animation)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .preload()
    }
    private fun setupRecyclerViews() {
        binding.apply {
            recyclePlay.apply {
                adapter = playerAdapter
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                layoutManager =
                    object : androidx.recyclerview.widget.GridLayoutManager(requireContext(), 4) {
                        override fun canScrollVertically(): Boolean = false
                        override fun canScrollHorizontally(): Boolean = false
                    }
                itemAnimator = null
            }

            playerAdapter.submitList(playerList)
        }
    }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentPlayMutiplayBinding = FragmentPlayMutiplayBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.apply {
            setupActionBar()
        }
        setupRecyclerViews()
        setupBackPressHandler()
//        lifecycleScope.

//        binding.textView.text = "Home Fragment"
//        binding.btnTest.setOnClickListener {
//            showSnackbar("Xin chào từ Home!")
//        }
    }
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
        handler.removeCallbacksAndMessages(null)
        popBack()
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