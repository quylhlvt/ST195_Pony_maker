package com.example.basefragment.ui.main.add_character

import android.graphics.BitmapFactory
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.speech.SpeechRecognizer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.basefragment.R
import com.example.basefragment.ViewModelActivity
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.loadImage
import com.example.basefragment.databinding.FragmentAddCharacterBinding
import com.example.basefragment.databinding.FragmentChoosePonyBinding
import com.example.basefragment.databinding.FragmentChoosePonyBinding.inflate
import com.example.basefragment.ui.main.createPony.ChoosePonyViewModel
import com.example.basefragment.ui.onboarding.permission.PermissionViewModel
import kotlinx.coroutines.launch

class AddCharacterFragment : BaseFragment<FragmentAddCharacterBinding, AddCharacterViewModel>( FragmentAddCharacterBinding::inflate, AddCharacterViewModel::class.java) {
    private val permissionViewModel: PermissionViewModel by viewModels()
    private val characterId: String by lazy {
        arguments?.getString("characterId") ?: ""
    }

    private val imagePath: String by lazy {
        arguments?.getString("imagePath") ?: ""
    }
//
//    private val backgroundImageAdapter by lazy { BackgroundImageAdapter() }
//    private val backgroundColorAdapter by lazy { BackgroundColorAdapter() }
//    private val stickerAdapter by lazy { StickerAdapter() }
//    private val speechAdapter by lazy { SpeechAdapter() }
//    private val textFontAdapter by lazy { TextFontAdapter(requireContext()) }
//    private val textColorAdapter by lazy { TextColorAdapter() }
//    private val buttonNavigationList by lazy {
//        arrayListOf(
//            binding.btnBackground,
//            binding.btnSticker,
//            binding.btnSpeech,
//            binding.btnText,
//        )
//    }
    override fun viewListener() {

    }


    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentAddCharacterBinding = FragmentAddCharacterBinding.inflate(inflater, container, false)

    override fun initView() {
        if (imagePath.isNotEmpty()) {
            loadImage(requireContext(),imagePath, binding.imageView)
        }

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
            viewModelActivity.backgrounds.collect { backgrounds ->
                // Setup RecyclerView
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModelActivity.stickers.collect { stickers ->
                // Setup RecyclerView
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModelActivity.speechs.collect { stickers ->
                // Setup RecyclerView
            }
        }
    }
}