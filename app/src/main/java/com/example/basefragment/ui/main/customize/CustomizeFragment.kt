package com.example.basefragment.ui.main.customize

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.ViewModelActivity
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.databinding.FragmentCustomizeBinding

import com.example.basefragment.ui.main.createPony.ChoosePonyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class CustomizeFragment : BaseFragment<FragmentCustomizeBinding, CustomizeViewModel>(
    FragmentCustomizeBinding::inflate, CustomizeViewModel::class.java
) {

    private val mainViewModel: ViewModelActivity by activityViewModels()
    private var characterIndex: Int = -1

    override fun viewListener() {
        binding.actionBar.apply {
            btnActionBarLeft.setOnClickListener { findNavController().navigateUp() }

            btnActionBarRight.setOnClickListener {
                viewModel.saveCharacter(mainViewModel)
                mainViewModel.refreshApiData()
            }
            btnActionBarCenter.setOnClickListener { viewModel.resetCharacter() }
        }

        binding.imgRandom.setOnClickListener { viewModel.randomizeCharacter() }
    }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): FragmentCustomizeBinding = FragmentCustomizeBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
            setImageActionBar(btnActionBarCenter, R.drawable.ic_reset_all_custom)
        }
        characterIndex = arguments?.getInt("characterIndex") ?: -1
        if (characterIndex >= 0) {
            // Edit từ Quick/MyWork
            viewModel.initCharacter(mainViewModel,index = characterIndex)
        } else {
            val template: CustomModel? = arguments?.getParcelable("template")
            template?.let { viewModel.initCharacter(mainViewModel,template = it) }
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentCharacter.collect { character ->
                character?.let {
                    // TODO: cập nhật preview, hình ảnh, body part
                }
            }

            viewModel.selectedBodyParts.collect { selectedParts ->
                // TODO: highlight body parts hoặc cập nhật UI
            }
        }
    }

    override fun bindViewModel() {
        // Không cần làm gì thêm nếu đã dùng Hilt + BaseFragment
    }
}
