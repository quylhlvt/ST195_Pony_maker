package com.example.basefragment.ui.main.customize

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
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
import com.example.basefragment.core.extention.gone
import com.example.basefragment.core.extention.loadImage
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.core.extention.toggetShow
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
    private var checkShow = true

    // 🔥 Helper function: Lấy đường dẫn ảnh thật từ ColorModel
    private fun ColorModel.getImagePath(): String? {
        return listPath.firstOrNull { path ->
            path != "none" && path != "dice" && path.contains("/")
        }
    }

    override fun initView() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
            setImageActionBar(btnActionBarCenter, R.drawable.ic_flip_all_custom)
            setImageActionBar(btnActionBarCenter1, R.drawable.ic_reset_all_custom)
            setImageActionBar(btnActionBarCenter2, R.drawable.ic_show_all_custom)
            setImageActionBar(btnActionBarRight, R.drawable.next_app)
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

        navAdapter = NavAdapter( emptyList()) { navIndex ->
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

        layerAdapter = LayerAdapter( emptyList()) { layerIndex ->
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
                viewModel.toggleFlip()
            }
            btnActionBarCenter1.setOnClickListener {
                viewModel.resetCurrentVariant()
            }
            btnActionBarCenter2.setOnClickListener {
                checkShow= !checkShow
                setImageActionBar(btnActionBarCenter2,if (checkShow) R.drawable.ic_show_all_custom else R.drawable.ic_hide_all_custom )

                val navIndex = viewModel.currentNavIndex.value
                val bodyPart = viewModel.selectedBodyParts.value
                    .getOrNull(navIndex) ?: return@setOnClickListener
                val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }
                binding.apply {
                    imgRandom.toggetShow()
                    frameLayer.toggetShow()
                    recyclerView2.toggetShow()
                    if (hasColor){
                        recycleColorItem.toggetShow()
                        imgChangColor.toggetShow()
                    }

                }
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isFlipped.collect {
                updateCharacterPreview(viewModel.currentCharacter.value)
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
    }
    private fun updateCharacterPreview(character: CustomModel?) {
        val isFlipped = viewModel.isFlipped.value
        binding.characterContainer.removeAllViews()
        if (character == null) return

        // 🔥 Render theo z-index
        val sortedParts = character.listPath.sortedBy { it.zIndex }

        for (bodyPart in sortedParts) {
            // 🔥 TÌM navIndex THẬT từ character.listPath gốc
            val navIndex = character.listPath.indexOf(bodyPart)
            if (navIndex == -1) continue

            // 🔥 Lấy path từ selection
            val imagePath = viewModel.getImagePathForSelection(navIndex)
            if (imagePath.isNullOrBlank()) {
                // 🔥 Nếu không có path → check xem có phải nav hiện tại không
                val currentNavIndex = viewModel.currentNavIndex.value
                if (navIndex == currentNavIndex) {
                    // Nav hiện tại mà không có selection → hiển thị avatar
                    if (character.avatar?.isNotBlank() == true) {
                        val avatarView = ImageView(requireContext()).apply {
                            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                            scaleType = ImageView.ScaleType.FIT_XY
                        }
                        loadImage(character.avatar!!, avatarView)
                        binding.characterContainer.addView(avatarView)
                    }
                }
                continue
            }

            // 🔥 Có path → hiển thị layer
            val iv = ImageView(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = ImageView.ScaleType.FIT_XY
                scaleX = if (isFlipped) -1f else 1f
            }
            loadImage(imagePath, iv)
            binding.characterContainer.addView(iv)
        }
    }

    override fun bindViewModel() {}

}