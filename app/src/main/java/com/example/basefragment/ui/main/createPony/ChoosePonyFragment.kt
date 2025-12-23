package com.example.basefragment.ui.main.createPony

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.basefragment.R
import com.example.basefragment.ViewModelActivity
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.data.datalocal.manager.AppDataManager
import com.example.basefragment.databinding.FragmentChoosePonyBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChoosePonyFragment : BaseFragment<FragmentChoosePonyBinding, ChoosePonyViewModel>(
    FragmentChoosePonyBinding::inflate,
    ChoosePonyViewModel::class.java
) {
    private lateinit var adapter: ChoosePonyAdapter
    override fun viewListener() {
        binding.actionBar.apply {
            btnActionBarLeft.setOnClickListener {
                findNavController().navigateUp()
            }
        }
//        binding.swipeRefresh?.setOnRefreshListener {
//            mainViewModel.refreshApiData()
//        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }
    private fun refreshData() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.swipeRefreshLayout.isRefreshing = true // Bắt đầu vòng quay

            try {
                val success = viewModelActivity.appDataManager.refreshTemplatesFromApi()
                if (success) {
                    showToast(getString(R.string.load_success))
                } else {
                    showToast(getString(R.string.load_fail))
                }
            } catch (e: Exception) {

            } finally {
                binding.swipeRefreshLayout.isRefreshing = false // Tắt vòng quay
            }
        }
    }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentChoosePonyBinding = FragmentChoosePonyBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
        }
        if (viewModelActivity.templates.value.isEmpty()) {
            showLoadingSafe()
        }
        setupRecyclerView()
        setupSwipeRefresh()
        val hasData = viewModelActivity.templates.value.isNotEmpty()
        val isLoading = viewModelActivity.isLoading.value

        if (!hasData && isLoading) {
            showLoadingSafe()
        } else if (!hasData && !isLoading) {
            // Data failed to load, trigger reload
            viewModelActivity.retryLoadData()
            showLoadingSafe()
        }
    }
    private fun setupRecyclerView() {
        adapter = ChoosePonyAdapter { character, position ->
            findNavController().navigate(
                R.id.action_createPony_to_custom,
                bundleOf("templateIndex" to position)
            )
        }

        binding.recycleChoose.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@ChoosePonyFragment.adapter
            itemAnimator = null
        }
    }
    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(
                androidx.lifecycle.Lifecycle.State.STARTED
            ) {
                launch {
                    viewModelActivity.templates.collect { templates ->
                        if (templates.isNotEmpty()) {
                            adapter.submitList(templates)
                            hideLoadingSafe()
                        }
                    }
                }

                // Keep loading state observer for manual refresh
                launch {
                    viewModelActivity.isLoading.collect { isLoading ->
                        if (isLoading && viewModelActivity.templates.value.isEmpty()) {
                            showLoadingSafe()
                        } else if (!isLoading) {
                            hideLoadingSafe()
                        }
                    }
                }

                // Keep error observer
                launch {
                    viewModelActivity.error.collect { error ->
                        error?.let {
                            hideLoadingSafe()
                            showSnackbar(it)
                        }
                    }
                }

            }
        }
    }

    override fun bindViewModel() {
    }
}