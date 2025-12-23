package com.example.basefragment.ui.main.quick

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.databinding.FragmentQuickBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuickFragment : BaseFragment<FragmentQuickBinding, QuickViewModel>(
    FragmentQuickBinding::inflate,
    QuickViewModel::class.java
) {

    private lateinit var quickRandomAdapter: CharacterListAdapter
    private var progressDialog: AlertDialog? = null

    override fun initView() {
        setImageActionBar(binding.actionBar.btnActionBarLeft, R.drawable.back_app)
        setupRecyclerView()
        setupSwipeRefresh()

        // ✅ Check data và generate nếu cần
        checkAndGenerateData()
    }

    private fun setupRecyclerView() {
        quickRandomAdapter = CharacterListAdapter(
            onCharacterClick = { character, position ->
                // ✅ Navigate to CustomizeFragment để edit
                val globalIndex = viewModelActivity.characters.value.indexOfFirst {
                    it.id == character.id
                }

                if (globalIndex >= 0) {
                    findNavController().navigate(
                        R.id.action_quick_to_custom,
                        bundleOf("characterIndex" to globalIndex)
                    )
                } else {
                    // ✅ Character chưa có trong combined list, thêm vào
                    viewModelActivity.updateOrAddCharacter(character)

                    // Wait một chút để combined list update
                    lifecycleScope.launch {
                        delay(100)
                        val newIndex = viewModelActivity.characters.value.indexOfFirst {
                            it.id == character.id
                        }
                        if (newIndex >= 0) {
                            findNavController().navigate(
                                R.id.action_quick_to_custom,
                                bundleOf("characterIndex" to newIndex,
                                    "isQuickRandom" to true
                                    )
                            )
                        }
                    }
                }
            }
        )

        binding.recyclerViewQuick.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = quickRandomAdapter
            itemAnimator = null
        }
    }

    /**
     * ✅ Setup SwipeRefreshLayout
     */
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    /**
     * ✅ Check data và generate nếu chưa có
     */
    private fun checkAndGenerateData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // ✅ Show loading ngay
                showLoadingSafe()

                val hasData = viewModel.hasQuickRandomData()

                if (!hasData) {
                    // ✅ Chưa có data → generate mới
                    startGeneration()
                } else {
                    // ✅ Đã có data → load từ cache
                    // Loading sẽ tự hide khi observe collect data
                    hideLoadingSafe()
                }
            } catch (e: Exception) {
                hideLoadingSafe()
            }
        }
    }

    /**
     * ✅ Refresh data - Generate lại characters mới
     */
    private fun refreshData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.regenerateQuickRandomCharacters()
            } catch (e: Exception) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun observeData() {
        // ✅ Observe real-time updates từ ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentGeneratedCharacters.collect { characters ->
                // ✅ Update list ngay lập tức khi có item mới
                quickRandomAdapter.submitList(characters.toList())

                // ✅ Hide loading khi có data đầu tiên
                if (characters.isNotEmpty()) {
                    hideLoadingSafe()
                }
            }
        }

        // ✅ Observe generation state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isGenerating.collect { isGenerating ->
                // ✅ Tắt refresh khi hoàn thành generate
                if (!isGenerating && binding.swipeRefreshLayout.isRefreshing) {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        // ✅ Observe generation progress (optional - hiển thị progress)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.generationProgress.collect { progress ->
                if (progress.current > 0) {
                    // Có thể hiển thị progress text
                    // binding.tvProgress.text = "${progress.current}/${progress.total}"
                }
            }
        }
    }

    override fun viewListener() {
        // ✅ Back button
        binding.actionBar.btnActionBarLeft.onClick {
            findNavController().navigateUp()
        }
    }

    private fun startGeneration() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.generateQuickRandomCharacters()
            } catch (e: Exception) {
                hideLoadingSafe()
            }
        }
    }

    override fun bindViewModel() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentQuickBinding {
        return FragmentQuickBinding.inflate(inflater, container, false)
    }
}