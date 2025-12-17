package com.example.basefragment.ui.main.customize

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
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
import com.example.basefragment.core.extention.loadImage
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.data.model.custom.ColorModel
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.databinding.FragmentCustomizeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomizeFragment :
    BaseFragment<FragmentCustomizeBinding, CustomizeViewModel>(
        FragmentCustomizeBinding::inflate,
        CustomizeViewModel::class.java
    ) {

    private val mainViewModel: ViewModelActivity by activityViewModels()
    private lateinit var navAdapter: NavAdapter
    private lateinit var layerAdapter: LayerAdapter
    private lateinit var colorAdapter: ColorAdapter

    private var lastNavIndex = -1

    // 🔥 Helper function: Lấy đường dẫn ảnh thật từ ColorModel
    private fun ColorModel.getImagePath(): String? {
        return listPath.firstOrNull { path ->
            path != "none" && path != "dice" && path.contains("/")
        }
    }

    override fun initView() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
            setImageActionBar(btnActionBarCenter, R.drawable.ic_reset_all_custom)
        }

        val characterIndex = arguments?.getInt("characterIndex", -1) ?: -1
        val templateIndex = arguments?.getInt("templateIndex", -1) ?: -1

        when {
            characterIndex >= 0 -> {
                viewModel.initCharacter(mainViewModel, index = characterIndex)
            }
            templateIndex >= 0 -> {
                val template = mainViewModel.getCharacterByIndex(templateIndex)
                viewModel.initCharacter(mainViewModel, template = template)
            }
            else -> {
                findNavController().navigateUp()
                return
            }
        }

        // NAV Adapter
        binding.recyclerView2.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        navAdapter = NavAdapter(requireContext(), emptyList()) { navIndex ->
            viewModel.selectNav(navIndex)
        }
        binding.recyclerView2.adapter = navAdapter

        // COLOR Adapter
        binding.recycleColorItem.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        colorAdapter = ColorAdapter(emptyList()) { colorIndex ->
            viewModel.selectColor(colorIndex, layerAdapter.selectedIndex)
        }
        binding.recycleColorItem.adapter = colorAdapter

        // LAYER Adapter
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 5)

        layerAdapter = LayerAdapter(requireContext(), emptyList()) { layerIndex ->
            viewModel.selectLayer(layerIndex)
        }
        binding.recyclerView.adapter = layerAdapter
    }

    override fun viewListener() {
        binding.actionBar.apply {
            btnActionBarLeft.setOnClickListener {
                findNavController().navigateUp()
            }
            btnActionBarRight.setOnClickListener {
                viewModel.saveCharacter(mainViewModel)
                mainViewModel.refreshApiData()
                findNavController().navigateUp()
            }
            btnActionBarCenter.setOnClickListener {
                viewModel.resetCurrentVariant()
            }
        }

        binding.imgRandom.setOnClickListener {
            viewModel.randomizeCharacter()
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
            // 🔥 Không có color → lấy danh sách ảnh thật, sau đó thêm "none" và "dice" vào đầu
            val realImages = bodyPart.listPath.mapNotNull { colorModel ->
                colorModel.getImagePath()
            }

            // 🔥 Thêm "none" và "dice" vào đầu (theo thứ tự: "none" trước, "dice" sau nếu index != 1)
            val layerImages = mutableListOf<String>()
            val index = bodyPart.position.toIntOrNull() ?: 0  // Lấy index từ position
            if (index != 1) {
                layerImages.add("none")
                layerImages.add("dice")
            } else {
                layerImages.add("dice")
            }
            layerImages.addAll(realImages)  // Thêm các ảnh thật vào sau

            if (layerImages.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                return
            }

            binding.recyclerView.visibility = View.VISIBLE
            val actualLayerIndex = if (selection.layer == -1) 0 else selection.layer
            layerAdapter.setDataWithSelection(layerImages, actualLayerIndex)
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
            // 🔥 Không có color → lấy danh sách ảnh thật, sau đó thêm "none" và "dice" vào đầu
            val realImages = bodyPart.listPath.mapNotNull { colorModel ->
                colorModel.getImagePath()
            }

            // 🔥 Thêm "none" và "dice" vào đầu (theo thứ tự: "none" trước, "dice" sau nếu index != 1)
            val layerImages = mutableListOf<String>()
            val index = bodyPart.position.toIntOrNull() ?: 0  // Lấy index từ position (nếu cần, hoặc hardcode nếu biết)
            if (index != 1) {
                layerImages.add("none")
                layerImages.add("dice")
            } else {
                layerImages.add("dice")
            }
            layerImages.addAll(realImages)  // Thêm các ảnh thật vào sau

            if (layerImages.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                return
            }

            binding.recyclerView.visibility = View.VISIBLE
            val actualLayerIndex = if (selection.layer == -1) 0 else selection.layer
            layerAdapter.setDataWithSelection(layerImages, actualLayerIndex)
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
    }
    private fun updateCharacterPreview(character: CustomModel?) {
        binding.characterContainer.removeAllViews()

        val originalBodyParts = viewModel.selectedBodyParts.value
        val allSelections = (0 until originalBodyParts.size).map { viewModel.getSelection(it) }

        val hasAnySelection = allSelections.any { it.layer != -1 }

        if (!hasAnySelection) {
            character?.avatar?.let { avatarPath ->
                val imageView = ImageView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    scaleType = ImageView.ScaleType.FIT_XY
                }
                loadImage(avatarPath, imageView)
                binding.characterContainer.addView(imageView)
            }
            return
        }

        allSelections.forEach { selection ->
            if (selection.layer == -1) return@forEach

            val bodyPart = originalBodyParts.getOrNull(selection.nav) ?: return@forEach
            val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

            val imagePath: String? = if (!hasColor) {
                // 🔥 Case KHÔNG có color
                val listPath = bodyPart.listPath.firstOrNull()?.listPath ?: return@forEach
                val rawPath = listPath.getOrNull(selection.layer) ?: return@forEach

                when (rawPath) {
                    "none" -> null
                    "dice" -> {
                        // Random ảnh thật (giống applyPreviewSingleImage)
                        val realImages = listPath.filter {
                            it != "none" && it != "dice" && it.contains("/")
                        }
                        realImages.randomOrNull()
                    }
                    else -> rawPath.takeIf { it.contains("/") && it.isNotBlank() }
                }
            } else {
                // Case có color (giữ nguyên logic cũ)
                val colorIndex = if (selection.color == -1) 0 else selection.color
                bodyPart.listPath.getOrNull(colorIndex)?.listPath?.getOrNull(selection.layer)
                    ?.takeIf { it != "none" && it.isNotBlank() }
            }

            if (!imagePath.isNullOrBlank()) {
                val imageView = ImageView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    scaleType = ImageView.ScaleType.FIT_XY
                }
                loadImage(imagePath, imageView)
                binding.characterContainer.addView(imageView)
            }
        }
    }
    override fun bindViewModel() {}
}