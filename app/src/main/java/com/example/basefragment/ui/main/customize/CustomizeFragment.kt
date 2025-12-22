package com.example.basefragment.ui.main.customize

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.basefragment.R
import com.example.basefragment.ViewModelActivity
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.drawToBitmap
import com.example.basefragment.core.extention.gone
import com.example.basefragment.core.extention.hideNavigation
import com.example.basefragment.core.extention.loadImage
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.core.extention.toggetShow
import com.example.basefragment.data.datalocal.manager.CharacterImageManager
import com.example.basefragment.data.model.custom.ColorModel
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.databinding.FragmentCustomizeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import javax.inject.Inject

@AndroidEntryPoint
class CustomizeFragment :
    BaseFragment<FragmentCustomizeBinding, CustomizeViewModel>(
        FragmentCustomizeBinding::inflate,
        CustomizeViewModel::class.java
    ) {
    @Inject
    lateinit var imageManager: CharacterImageManager
    private val mainViewModel: ViewModelActivity by activityViewModels()
    private lateinit var navAdapter: NavAdapter
    private lateinit var layerAdapter: LayerAdapter
    private lateinit var colorAdapter: ColorAdapter

    private var lastNavIndex = -1
    private var checkShow = true
    private var isQuickRandom = false
    // 🔥 Helper function: Lấy đường dẫn ảnh thật từ ColorModel
    private fun ColorModel.getImagePath(): String? {
        return listPath.firstOrNull { path ->
            path != "none" && path != "dice" && path.contains("/")
        }
    }

    override fun initView() {
        requireActivity().hideNavigation(true)
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
            setImageActionBar(btnActionBarCenter, R.drawable.ic_flip_all_custom)
            setImageActionBar(btnActionBarCenter1, R.drawable.ic_reset_all_custom)
            setImageActionBar(btnActionBarCenter2, R.drawable.ic_show_all_custom)
            setImageActionBar(btnActionBarRight, R.drawable.next_app)
        }

        val characterIndex = arguments?.getInt("characterIndex", -1) ?: -1
        val templateIndex = arguments?.getInt("templateIndex", -1) ?: -1
        isQuickRandom = arguments?.getBoolean("isQuickRandom", false) ?: false
        when {
            characterIndex >= 0 -> {
                viewModel.initCharacter(mainViewModel, index = characterIndex, isquick = isQuickRandom)
            }

            templateIndex >= 0 -> {
                val template = mainViewModel.getCharacterByIndex(templateIndex)
                viewModel.initCharacter(mainViewModel, template = template,isquick = isQuickRandom)
            }

            else -> {
                findNavController().navigateUp()
                return
            }
        }

        // ✨ NAV Adapter - CÓ AUTO-SCROLL
        binding.recyclerView2.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        navAdapter = NavAdapter(
            bodyParts = emptyList(),
            onClick = { navIndex ->
                viewModel.selectNav(navIndex)
            }
        )
        binding.recyclerView2.adapter = navAdapter

// ✨ COLOR Adapter - CÓ AUTO-SCROLL
        binding.recycleColorItem.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        colorAdapter = ColorAdapter(
            colors = emptyList(),
            onColorSelected = { colorIndex ->
                viewModel.selectColor(colorIndex, layerAdapter.selectedIndex)
            },
            onScrollToPosition = { position ->
                binding.recycleColorItem.smoothScrollToPosition(position)
            }
        )
        binding.recycleColorItem.adapter = colorAdapter

// ✨ LAYER Adapter - CÓ AUTO-SCROLL
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 5)

        layerAdapter = LayerAdapter(
            imagePaths = emptyList(),
            onImageSelected = { layerIndex ->
                viewModel.selectLayer(layerIndex)
            },
            onScrollToPosition = { position ->
                binding.recyclerView.smoothScrollToPosition(position)
            }
        )
        binding.recyclerView.adapter = layerAdapter
    }

    override fun viewListener() {
        binding.actionBar.apply {
            btnActionBarLeft.setOnClickListener {
                // 🔥 XÓA temp character khi back mà không save
                if (isQuickRandom) {
                    val characterId = viewModel.currentCharacter.value?.id
                    if (characterId != null && characterId.startsWith("temp_from_quick_")) {
                        mainViewModel.deleteCharacter(characterId)
                        Log.d("CustomizeFragment", "🗑️ Deleted temp character: $characterId")
                    }
                }
                findNavController().navigateUp()
            }
            btnActionBarRight.setOnClickListener {
                saveCharacterWithImage()
                requireActivity().hideNavigation(true)

            }
            btnActionBarCenter.setOnClickListener {
                viewModel.toggleFlip()
                requireActivity().hideNavigation(true)

            }
            btnActionBarCenter1.setOnClickListener {
                viewModel.resetCurrentVariant()
                requireActivity().hideNavigation(true)

            }
            btnActionBarCenter2.setOnClickListener {
                checkShow = !checkShow
                setImageActionBar(
                    btnActionBarCenter2,
                    if (checkShow) R.drawable.ic_show_all_custom else R.drawable.ic_hide_all_custom
                )

                val navIndex = viewModel.currentNavIndex.value
                val bodyPart = viewModel.selectedBodyParts.value
                    .getOrNull(navIndex) ?: return@setOnClickListener
                val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }
                binding.apply {
                    imgRandom.toggetShow()
                    frameLayer.toggetShow()
                    recyclerView2.toggetShow()
                    if (hasColor) {
                        recycleColorItem.toggetShow()
                        imgChangColor.toggetShow()
                    }

                }
            }
        }

        binding.imgRandom.setOnClickListener {
            viewModel.randomizeCharacter()
            requireActivity().hideNavigation(true)

        }


    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentCustomizeBinding = FragmentCustomizeBinding.inflate(inflater, container, false)

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedBodyParts.collect { parts ->
                navAdapter.setData(parts)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.triggerLayerUpdate.collect { trigger ->
                if (trigger > 0) {
                    updateLayerRecyclerOnly(viewModel.currentNavIndex.value)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentNavIndex.collect { navIndex ->
                if (navIndex != lastNavIndex) {
                    lastNavIndex = navIndex

                    navAdapter.setSelectedIndex(navIndex)
                    updateColorRecycler(navIndex)
                    updateLayerRecycler(navIndex)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentCharacter.collect { character ->
                updateCharacterPreview(character)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isFlipped.collect {
                updateCharacterPreview(viewModel.currentCharacter.value)
            }
        }
        // ✨ THÊM: Observe color update trigger
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.triggerColorUpdate.collect { trigger ->
                if (trigger > 0) {
                    updateColorRecycler(viewModel.currentNavIndex.value)
                }
            }
        }

    }

    private fun updateColorRecycler(navIndex: Int) {
        val bodyPart = viewModel.selectedBodyParts.value.getOrNull(navIndex) ?: return

        // 🔥 Kiểm tra xem có color hay không
        val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

        if (!hasColor) {
            // 🔥 Không có color → ẩn color recycler
            binding.recycleColorItem.visibility = View.GONE
            binding.imgChangColor.visibility = View.GONE
            colorAdapter.setData(emptyList())
        } else {
            // 🔥 Có color → hiển thị color recycler
            binding.recycleColorItem.visibility = View.VISIBLE
            binding.imgChangColor.visibility = View.VISIBLE

            colorAdapter.setData(bodyPart.listPath)

            val selection = viewModel.getSelection(navIndex)
            colorAdapter.setSelectedIndex(selection.color)
        }
    }

    private fun updateLayerRecyclerOnly(navIndex: Int) {
        val bodyPart = viewModel.selectedBodyParts.value.getOrNull(navIndex) ?: return

        if (bodyPart.listPath.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            return
        }

        val selection = viewModel.getSelection(navIndex)
        val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

        if (!hasColor) {
            val layers = viewModel.getLayerDisplayList(navIndex)

            binding.recyclerView.visibility =
                if (layers.isEmpty()) View.GONE else View.VISIBLE

            layerAdapter.setDataWithSelection(
                layers,
                if (selection.layer == -1) 0 else selection.layer
            )

            return
        }

        // 🔥 Có color → logic cũ (không thay đổi)
        val actualColorIndex = if (selection.color == -1) 0 else selection.color
        val color = bodyPart.listPath.getOrNull(actualColorIndex) ?: return

        if (color.listPath.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            return
        }

        binding.recyclerView.visibility = View.VISIBLE
        val actualLayerIndex = if (selection.layer == -1) 0 else selection.layer

        layerAdapter.setDataWithSelection(color.listPath, actualLayerIndex)
        requireActivity().hideNavigation(true)

    }

    private fun updateLayerRecycler(navIndex: Int) {
        val bodyPart = viewModel.selectedBodyParts.value.getOrNull(navIndex) ?: return

        if (bodyPart.listPath.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            return
        }

        val selection = viewModel.getSelection(navIndex)
        val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

        if (!hasColor) {
            val layers = viewModel.getLayerDisplayList(navIndex)

            binding.recyclerView.visibility =
                if (layers.isEmpty()) View.GONE else View.VISIBLE

            layerAdapter.setDataWithSelection(
                layers,
                if (selection.layer == -1) 0 else selection.layer
            )

            return
        }

        // 🔥 Có color → logic cũ (không thay đổi)
        val actualColorIndex = if (selection.color == -1) 0 else selection.color
        val color = bodyPart.listPath.getOrNull(actualColorIndex) ?: return

        if (color.listPath.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            return
        }

        binding.recyclerView.visibility = View.VISIBLE
        val actualLayerIndex = if (selection.layer == -1) 0 else selection.layer

        layerAdapter.setDataWithSelection(color.listPath, actualLayerIndex)
        requireActivity().hideNavigation(true)

    }

    private fun updateCharacterPreview(character: CustomModel?) {
        val isFlipped = viewModel.isFlipped.value
        binding.characterContainer.removeAllViews()
        if (character == null) return
        // 🔥 FIX: Sort theo zIndex (render order), KHÔNG phải position (nav order)
        val sortedParts = character.listPath.sortedBy { it.zIndex }

        for (bodyPart in sortedParts) {
            // 🔥 TÌM navIndex THẬT từ character.listPath GỐC (trước khi sort)
            val navIndex = viewModel.selectedBodyParts.value.indexOfFirst { it.position == bodyPart.position }
            if (navIndex == -1) continue

            // 🔥 Lấy path từ selection
            val imagePath = viewModel.getImagePathForSelection(navIndex)

            // 🔥 Skip nếu không có ảnh
            if (imagePath.isNullOrBlank()) continue

            // 🔥 Render layer
            val iv = ImageView(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = ImageView.ScaleType.FIT_XY
                scaleX = if (isFlipped) -1f else 1f
            }
            loadImage(imagePath, iv)
            binding.characterContainer.addView(iv)
        }
        requireActivity().hideNavigation(true)
    }
    // ✅ CHỈ CẬP NHẬT HÀM saveCharacterWithImage() trong CustomizeFragment.kt

    private fun saveCharacterWithImage() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (!viewModel.hasAnyRealImageSelected()) {
                    Toast.makeText(requireContext(), "Please select at least one item", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                binding.characterContainer.post {
                    try {
                        val currentCharacter = viewModel.currentCharacter.value

                        // 🔥 NẾU LÀ TEMP từ quick → TẠO ID MỚI
                        val finalCharacterId = if (isQuickRandom &&
                            currentCharacter?.id?.startsWith("temp_from_quick_") == true) {

                            // 🔥 Xóa temp character
                            val tempId = currentCharacter.id
                            mainViewModel.deleteCharacter(tempId)
                            Log.d("CustomizeFragment", "🗑️ Deleted temp character: $tempId")

                            // 🔥 Tạo ID mới HOÀN TOÀN
                            java.util.UUID.randomUUID().toString()
                        } else {
                            // ✅ Giữ nguyên ID (edit mode hoặc new từ template)
                            currentCharacter?.id ?: java.util.UUID.randomUUID().toString()
                        }

                        Log.d("CustomizeFragment", "💾 Saving character:")
                        Log.d("CustomizeFragment", "   - Old ID: ${currentCharacter?.id}")
                        Log.d("CustomizeFragment", "   - Final ID: $finalCharacterId")
                        Log.d("CustomizeFragment", "   - isQuickRandom: $isQuickRandom")

                        // 🔥 Render bitmap
                        val bitmap = binding.characterContainer.drawToBitmap()

                        // 🔥 Delete old image (if exists)
                        imageManager.deleteOldImage(finalCharacterId)

                        // 🔥 Save new image
                        val imagePath = imageManager.saveBitmap(bitmap, finalCharacterId)

                        if (imagePath != null) {
                            // ✅ Save character với ID phù hợp
                            if (isQuickRandom && currentCharacter?.id?.startsWith("temp_from_quick_") == true) {
                                // 🔥 Quick random → Save với ID mới
                                viewModel.saveCharacterWithNewId(
                                    mainViewModel = mainViewModel,
                                    newCharacterId = finalCharacterId,
                                    imagePath = imagePath
                                )
                            } else {
                                lifecycleScope.launch {
                                    viewModel.saveCharacter(mainViewModel, imagePath,finalCharacterId)

                                }
                                // ✅ Normal mode → Save bình thường
                            }

                            mainViewModel.refreshApiData()

                            Log.d("CustomizeFragment", "✅ Character saved:")
                            Log.d("CustomizeFragment", "   - ID: $finalCharacterId")
                            Log.d("CustomizeFragment", "   - Image: $imagePath")
                            Log.d("CustomizeFragment", "   - Total customized: ${mainViewModel.customizedCharacters.value.size}")

                            Toast.makeText(requireContext(), "Character saved!", Toast.LENGTH_SHORT).show()

                            findNavController().navigate(
                                R.id.action_customizeFragment_to_addFragment,
                                bundleOf(
                                    "characterId" to finalCharacterId,
                                    "imagePath" to imagePath
                                )
                            )
                        } else {
                            Log.e("CustomizeFragment", "❌ Failed to save image")
                            Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("CustomizeFragment", "❌ Error: ${e.message}", e)
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("CustomizeFragment", "❌ Error: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            requireActivity().hideNavigation(true)
        }
    }

    override fun bindViewModel() {}

}