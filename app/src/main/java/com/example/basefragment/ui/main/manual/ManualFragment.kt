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
class ManualFragment : BaseFragment<FragmentManualBinding, ManualViewModel>(
    FragmentManualBinding::inflate,
    ManualViewModel::class.java
) {

    private val manualAdapter by lazy {
        ManualAdapter(requireContext()).apply {
            onSelectionChanged = {
                viewModel.updatePlayer1List(getItems())
            }
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentManualBinding = FragmentManualBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.apply {
            setupActionBar()
            setupRecyclerView()
            txtPlayer.text = getString(R.string.player_1)
        }

        // Khởi tạo danh sách từ ViewModel
        viewModel.initPlayer1List()
    }

    // ✅ THÊM onResume để kiểm tra reset flag
    override fun onResume() {
        super.onResume()

        // Kiểm tra flag từ PlayFragment
        val shouldReset = findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("should_reset")

        if (shouldReset == true) {
            // Reset adapter
            val emptyList = List(9) { ManualModel(false) }
            manualAdapter.updateList(emptyList)

            // Clear flag
            findNavController().currentBackStackEntry
                ?.savedStateHandle
                ?.remove<Boolean>("should_reset")
        }
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

            viewModel.updatePlayer1List(manualAdapter.getItems())

            val bundle = Bundle().apply {
                putParcelableArray("selectedList", manualAdapter.getItems().toTypedArray())
            }

            findNavController().navigate(R.id.action_manualFragment_to_manualFragment2, bundle)
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.player1List.collect { list ->
                    if (list.isNotEmpty()) {
                        manualAdapter.updateList(list)
                    }
                }
            }
        }
    }

    override fun bindViewModel() {}
}