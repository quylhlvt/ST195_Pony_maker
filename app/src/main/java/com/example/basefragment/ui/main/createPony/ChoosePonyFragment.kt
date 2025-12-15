package com.example.basefragment.ui.main.createPony

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
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
    private val mainViewModel: ViewModelActivity by activityViewModels()
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


    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentChoosePonyBinding = FragmentChoosePonyBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
        }
        adapter = ChoosePonyAdapter { character, position ->
            findNavController().navigate(
                R.id.action_createPony_to_custom,
                bundleOf(
                    "mode" to "CREATE",
                    "templateId" to character.id // ID của template
                )
            )
        }

        binding.recycleChoose.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@ChoosePonyFragment.adapter
        }

    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(
                androidx.lifecycle.Lifecycle.State.STARTED
            ) {

                launch {
                    mainViewModel.characters.collect { characters ->
                        adapter.submitList(characters)
                    }
                }

                launch {
                    mainViewModel.isLoading.collect { isLoading ->
                        // binding.progressBar.isVisible = isLoading
                    }
                }

                launch {
                    mainViewModel.error.collect { error ->
                        error?.let { showSnackbar(it) }
                    }
                }
            }
        }
    }


    override fun bindViewModel() {
    }
}