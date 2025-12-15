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
import com.example.basefragment.data.model.custom.CharacterConfiguration
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.databinding.FragmentChoosePonyBinding
import com.example.basefragment.databinding.FragmentChoosePonyBinding.inflate
import com.example.basefragment.databinding.FragmentCustomizeBinding
import com.example.basefragment.ui.main.createPony.ChoosePonyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomizeFragment :  BaseFragment<FragmentCustomizeBinding, CustomizeViewModel>( FragmentCustomizeBinding::inflate, CustomizeViewModel::class.java) {


    private val mainViewModel: ViewModelActivity by activityViewModels()
    private var currentCharacter: CustomModel? = null

    override fun viewListener() {
        binding.actionBar.apply {
            // Back button
            btnActionBarLeft.setOnClickListener {
                findNavController().navigateUp()
            }

            // Next button (đi tới BackgroundFragment)
            btnActionBarRight.setOnClickListener {
                // Lưu configuration hiện tại vào MainViewModel
                saveCurrentConfiguration()

                // Navigate
                findNavController().navigate(
                    R.id.action_custom_to_addcharacter
                )
            }
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentCustomizeBinding = FragmentCustomizeBinding.inflate(inflater, container, false)

    override fun initView() {
        // Lấy character từ arguments
        val characterId = arguments?.getString("characterId")
        val characterIndex = arguments?.getInt("characterIndex", -1) ?: -1

        // Lấy character data
        currentCharacter = if (characterId != null) {
            mainViewModel.getCharacterById(characterId)
        } else if (characterIndex >= 0) {
            mainViewModel.getCharacterByIndex(characterIndex)
        } else {
            null
        }

        currentCharacter?.let { character ->
            // Hiển thị character
            displayCharacter(character)

            // Setup RecyclerView cho body parts
            setupBodyPartsRecyclerView(character)
        }
    }

    private fun displayCharacter(character: CustomModel) {
        binding.apply {
            // Hiển thị tên character
            tvCharacterName.text = character.name

            // Load thumbnail
            // Glide.with(requireContext())
            //     .load(character.thumbnail)
            //     .into(ivCharacter)

            // Hiển thị thông tin khác
            android.util.Log.d("CustomFragment", "Đang customize: ${character.name}")
            android.util.Log.d("CustomFragment", "Body parts: ${character.bodyParts.size}")
        }
    }

    private fun setupBodyPartsRecyclerView(character: CustomModel) {
        // Setup RecyclerView để hiển thị các body parts có thể customize
        // Mỗi item sẽ có danh sách màu để chọn

        val adapter = LayerAdapter(character.bodyParts) { bodyPart, colorIndex ->
            // Callback khi chọn màu cho 1 body part
            onBodyPartColorSelected(bodyPart.id, colorIndex)
        }

        binding.recyclerViewLayers.adapter = adapter
    }

    private fun onBodyPartColorSelected(bodyPartId: String, colorIndex: Int) {
        // Lưu lại selection
        viewModel.updateBodyPartSelection(bodyPartId, colorIndex)

        // Cập nhật preview
        updateCharacterPreview()
    }

    private fun updateCharacterPreview() {
        // Logic để cập nhật preview của character với các layer đã chọn
        // Ví dụ: overlay các layer lên nhau theo thứ tự layerOrder
        android.util.Log.d("CustomFragment", "Updating character preview")
    }

    private fun saveCurrentConfiguration() {
        currentCharacter?.let { character ->
            val config = CharacterConfiguration(
                characterId = character.id,
                selectedParts = viewModel.getSelectedParts()
            )
            mainViewModel.setCurrentConfiguration(config)
        }
    }

    override fun observeData() {
        // Observe ViewModel nếu cần
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedParts.collect { selectedParts ->
                // Cập nhật UI khi có thay đổi
                updateCharacterPreview()
            }
        }
    }

    override fun bindViewModel() {
        // Bind nếu cần
    }
}