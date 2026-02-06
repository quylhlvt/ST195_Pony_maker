package com.example.basefragment.ui.main.manual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.*
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.FragmentManual2Binding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManualFragment2 : BaseFragment<FragmentManual2Binding, ManualViewModel>(
    FragmentManual2Binding::inflate,
    ManualViewModel::class.java
) {
    private val sharedViewModel: ManualViewModel by activityViewModels()

    private val manualAdapter by lazy {
        ManualAdapter(requireContext(), true).apply {
            onSelectionChanged = {
                sharedViewModel.updatePlayer2List(getItems())
            }
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentManual2Binding = FragmentManual2Binding.inflate(inflater, container, false)

    override fun initView() {
        binding.apply {
            recycleChoose.setBackgroundResource(R.drawable.img_bg_choose_manual2)
            setupActionBar()
            setupRecyclerView()
            txtPlayer.text = getString(R.string.player_2)
        }
    }

    private fun FragmentManual2Binding.setupActionBar() {
        actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
            setImageActionBar(btnActionBarRight, R.drawable.guide)
        }
    }

    private fun FragmentManual2Binding.setupRecyclerView() {
        recycleChoose.apply {
            adapter = manualAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            layoutManager = object : androidx.recyclerview.widget.GridLayoutManager(requireContext(), 3) {
                override fun canScrollVertically(): Boolean = false
                override fun canScrollHorizontally(): Boolean = false
            }
            itemAnimator = null
        }
    }

    override fun viewListener() {
        binding.apply {
            setupActionBarListeners()
            setupNextButton()
        }
    }

    private fun FragmentManual2Binding.setupActionBarListeners() {
        actionBar.apply {
            btnActionBarLeft.onClick(requireContext()) {
                lifecycleScope.launch {
                    popBack()
                    sharedViewModel.reset2()
                    // ✅ KHÔNG RESET - GIỮ NGUYÊN LỰA CHỌN CỦA CẢ 2 PLAYER
                }
            }
            btnActionBarRight.onClick(requireContext()) {
                toGuideFromManual()
            }
        }
    }

    private fun FragmentManual2Binding.setupNextButton() {
        tvNext.onClick(requireContext()) {



            val selectedItems = manualAdapter.getSelectItems()

            if (selectedItems.size < 3) {
                showToast("Please select 3 items")
                return@onClick
            }

            // Lưu selection vào ViewModel
            sharedViewModel.updatePlayer2List(manualAdapter.getItems())

            // Lấy data để truyền sang PlayFragment
            val bundle = Bundle().apply {
                putParcelableArray("player1List", sharedViewModel.getPlayer1List().toTypedArray())
                putParcelableArray("player2List", sharedViewModel.getPlayer2List().toTypedArray())
            }

            // ✅ CHỈ RESET KHI ẤN NEXT - SAU KHI ĐÃ LẤY DATA

            // Navigate sang PlayFragment
            if (!sharedPreferences.isRotate()) {
                findNavController().navigate(R.id.action_manual_to_play, bundle)
            } else {
                findNavController().navigate(R.id.action_manual_to_play_Ver, bundle)
            }

            sharedViewModel.resetAll()

        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.player2List.collect { list ->
                    manualAdapter.updateList(list)
                }
            }
        }
    }

    override fun bindViewModel() {}
}