package com.example.basefragment.ui.main.add_character

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.basefragment.R
import com.example.basefragment.ViewModelActivity
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.databinding.FragmentAddCharacterBinding
import com.example.basefragment.databinding.FragmentChoosePonyBinding
import com.example.basefragment.databinding.FragmentChoosePonyBinding.inflate
import com.example.basefragment.ui.main.createPony.ChoosePonyViewModel
import kotlinx.coroutines.launch

class AddCharacterFragment : BaseFragment<FragmentAddCharacterBinding, AddCharacterViewModel>( FragmentAddCharacterBinding::inflate, AddCharacterViewModel::class.java) {
    private val mainViewModel: ViewModelActivity by activityViewModels()

    override fun viewListener() {

    }


    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentAddCharacterBinding = FragmentAddCharacterBinding.inflate(inflater, container, false)

    override fun initView() {

//        binding.textView.text = "Home Fragment"
//        binding.btnTest.setOnClickListener {
//            showSnackbar("Xin chào từ Home!")
//        }
    }

    override fun observeData() {
//        viewModel.data.observe(viewLifecycleOwner) { text ->
//            binding.textView.text = text
//        }
    }

    override fun bindViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Lấy backgrounds từ MainViewModel
            mainViewModel.backgrounds.collect { backgrounds ->
                // Setup RecyclerView
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.stickers.collect { stickers ->
                // Setup RecyclerView
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.speechs.collect { stickers ->
                // Setup RecyclerView
            }
        }
    }
}