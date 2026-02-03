package com.example.basefragment.ui.main.manual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.*
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.FragmentManualBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManualFragment2 : BaseFragment<FragmentManualBinding, ManualViewModel>(
    FragmentManualBinding::inflate,
    ManualViewModel::class.java
) {
    private lateinit var player1List: List<ManualModel>

    private val manualAdapter by lazy {
        ManualAdapter(requireContext()).apply {
            onSelectionChanged = {
                viewModel.updatePlayer2List(getItems())
            }
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentManualBinding = FragmentManualBinding.inflate(inflater, container, false)


    override fun initView() {
        // Lấy player1List từ arguments (fallback)
        player1List = arguments
            ?.getParcelableArray("selectedList")
            ?.map { it as ManualModel }
            ?: viewModel.getCurrentPlayer1List()

        binding.apply {
            setupActionBar()
            setupRecyclerView()
            txtPlayer.text = getString(R.string.player_2)
        }

        // Khởi tạo danh sách từ ViewModel
        viewModel.initPlayer2List()
    }

    private fun FragmentManualBinding.setupActionBar() {
        actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
            setImageActionBar(btnActionBarRight, R.drawable.guide)
        }
    }

    private fun FragmentManualBinding.setupRecyclerView() {
        recycleChoose.apply {
            adapter = manualAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    override fun viewListener() {
        binding.apply {
            setupActionBarListeners()
            setupNextButton()
        }
    }

    private fun FragmentManualBinding.setupActionBarListeners() {
        actionBar.apply {
            btnActionBarLeft.onClick(requireContext()) {
                popBack()
            }
            btnActionBarRight.onClick(requireContext()) {
                toGuideFromManual()
            }
        }
    }

    private fun FragmentManualBinding.setupNextButton() {
        tvNext.onClick(requireContext()) {
            val selectedItems = manualAdapter.getSelectItems()

            if (selectedItems.size < 3) {
                showToast("Please select 3 items")
                return@onClick
            }

            // Lưu state trước khi navigate
            viewModel.updatePlayer2List(manualAdapter.getItems())

            val bundle = Bundle().apply {
                putParcelableArray("player1List", player1List.toTypedArray())
                putParcelableArray("player2List", manualAdapter.getItems().toTypedArray())
            }

            findNavController().navigate(R.id.action_manual_to_play, bundle)
        }
    }

    override fun observeData() {
        // Collect Flow để restore state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.player2List.collect { list ->
                    if (list.isNotEmpty()) {
                        manualAdapter.updateList(list)
                    }
                }
            }
        }
    }

    override fun bindViewModel() {}
}