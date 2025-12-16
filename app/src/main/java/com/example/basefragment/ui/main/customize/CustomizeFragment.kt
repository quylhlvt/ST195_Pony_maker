package com.example.basefragment.ui.main.customize

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var selectedNavIndex = 0
    private var selectedColorIndex = 0
    private lateinit var navAdapter: NavAdapter
    private lateinit var layerAdapter: LayerAdapter
    private lateinit var colorAdapter: ColorAdapter
    private var isInitialLoad = true
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
        binding.recyclerView2.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        navAdapter = NavAdapter(requireContext(), emptyList()) { index ->
            selectedNavIndex = index
            updateLayerRecyclerView()
        }
        binding.recyclerView2.adapter = navAdapter

        // --- RecyclerView: grid 5 cột ---
        binding.recycleColorItem.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        colorAdapter = ColorAdapter(emptyList()) { colorIndex ->
            selectedColorIndex = colorIndex  // ✅ Chỉ update index
            updateLayerRecyclerView()
        }
        binding.recycleColorItem.adapter = colorAdapter
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 5)
        layerAdapter = LayerAdapter(requireContext(), emptyList()) { imageIndex ->
            updateSelectedImage(imageIndex)
        }
        binding.recyclerView.adapter = layerAdapter
    }
    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentCharacter.collect { character ->
                character?.let {
                    updateCharacterPreview(
                        it
                    )
                }
            }}
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedBodyParts.collect { selectedParts ->
                Log.d("CustomizeFragment", "Selected body parts: ${selectedParts.size}")
                if (selectedParts.isEmpty()) return@collect
                // Update Nav RecyclerView
                navAdapter.setData(selectedParts)
                // Update Layer RecyclerView theo nav đang chọn
                if (isInitialLoad) {
                    Log.d("CustomizeFragment", "🔵 Initial load - reset to index 0")
                    selectedNavIndex = 0
                    selectedColorIndex = 0
                    isInitialLoad = false
                }
                updateColorRecyclerView()
                updateLayerRecyclerView()

            }
        }

    }

    private fun updateColorRecyclerView() {
        val bodyPart = viewModel.selectedBodyParts.value.getOrNull(selectedNavIndex) ?: return
        Log.d("CustomizeFragment", "updateColorRecyclerView: ${bodyPart.listPath.size} colors")
        colorAdapter.setData(bodyPart.listPath)
        colorAdapter.setSelectedIndex(selectedColorIndex)
    }
    override fun bindViewModel() {
        // Không cần làm gì thêm nếu đã dùng Hilt + BaseFragment
    }
    private fun updateCharacterPreview(character: CustomModel) {
        val container = binding.characterContainer
        container.removeAllViews() // Xóa view cũ

        // Sắp xếp theo layer (từ nav)
        val sortedParts = character.listPath.sortedBy { bodyPart ->
            bodyPart.nav.substringAfterLast("-").toIntOrNull() ?: 0
        }

        sortedParts.forEach { bodyPart ->
            // Dùng tất cả colorPath trong bodyPart
            bodyPart.listPath.forEach { color ->
                color.listPath.forEach { path ->
                    val imageView = ImageView(requireContext()).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    }

                    // Nếu path là drawable
                    val resId = resources.getIdentifier(path, "drawable", requireContext().packageName)
                    if (resId != 0) {
                        imageView.setImageResource(resId)
                    } else {
                        // Nếu path là URL, dùng Glide/Coil
                        // Glide.with(this@CustomizeFragment).load(path).into(imageView)
                    }

                    container.addView(imageView)
                }
            }
        }
    }

    // ✅ Update RecyclerView ảnh layer (grid 5 cột)
    private fun updateLayerRecyclerView() {
        val bodyPart = viewModel.selectedBodyParts.value.getOrNull(selectedNavIndex) ?: return
        val colorModel = bodyPart.listPath.getOrNull(selectedColorIndex) ?: return

        Log.d("CustomizeFragment", "updateLayerRecyclerView: ${colorModel.listPath.size} images")
        layerAdapter.setData(colorModel.listPath)
    }

    // ✅ Khi user chọn 1 ảnh layer
    private fun updateSelectedImage(imageIndex: Int) {
        val bodyPart = viewModel.selectedBodyParts.value.getOrNull(selectedNavIndex) ?: return
        val colorModel = bodyPart.listPath.getOrNull(selectedColorIndex) ?: return
        val selectedImagePath = colorModel.listPath.getOrNull(imageIndex) ?: return

        Log.d("CustomizeFragment", "Selected image: $selectedImagePath")

        // ✅ Update bodyPart với màu đã chọn (giữ nguyên list màu)
        // Chỉ cần update currentCharacter để preview
        val currentChar = viewModel.currentCharacter.value ?: return
        val updatedParts = currentChar.listPath.toMutableList()

        if (selectedNavIndex in updatedParts.indices) {
            val currentBodyPart = updatedParts[selectedNavIndex]
            // Giữ nguyên tất cả màu, chỉ đánh dấu màu này đang được dùng
            updatedParts[selectedNavIndex] = currentBodyPart.copy(
                listPath = arrayListOf(colorModel)  // Lưu màu đang chọn để preview
            )

            viewModel.updateBodyPart(updatedParts[selectedNavIndex], selectedNavIndex)
        }
    }
}
