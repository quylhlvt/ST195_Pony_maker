package com.example.basefragment.ui.main.customize

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.*
import com.example.basefragment.data.model.custom.AvatarModel
import com.example.basefragment.data.model.custom.BodyPartModel
import com.example.basefragment.databinding.FragmentCustomizeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomizeFragment : BaseFragment<FragmentCustomizeBinding, CustomizeViewModel>(
    FragmentCustomizeBinding::inflate,
    CustomizeViewModel::class.java
) {

    // Adapters
    private val adapterColor by lazy { ColorAdapter() }
    private val adapterNav by lazy { NavAdapter() }
    private val adapterPart by lazy { LayerAdapter() }

    // Reusable ImageViews & path cache
    private val imageViews = mutableListOf<AppCompatImageView>()
    private val currentRenderedPaths = mutableMapOf<Int, String>() // drawIndex -> path

    // Glide options tối ưu
    private val glideOptions by lazy {
        RequestOptions()
            .override(512, 512)
            .format(com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    // Shared RecyclerView pool
    private val sharedPool by lazy { RecyclerView.RecycledViewPool() }

    override fun initView() {
        requireActivity().hideNavigation(true)

        // Lấy arguments
        val characterIndex = arguments?.getInt("data", 0) ?: 0
        val hotTrendArr = arguments?.getSerializable("arr") as? ArrayList<ArrayList<Int>>

        // Khởi tạo ViewModel với data
        viewModel.initialize(characterIndex, hotTrendArr?.map { it.map(Int::toInt) })

        setupRecyclerViews()
        setupAdapters()
        setupActionButtons()
        observeViewModel()
    }

    override fun bindViewModel() {

    }

    private fun setupRecyclerViews() {
        binding.apply {
            recyclerLayer.apply {
                adapter = adapterPart
                itemAnimator = null
                setHasFixedSize(true)
                setItemViewCacheSize(30)
                recycledViewPool = sharedPool
            }
            recycleColorItem.apply {
                adapter = adapterColor
                itemAnimator = null
                setHasFixedSize(true)
                setItemViewCacheSize(20)
                recycledViewPool = sharedPool
            }
            recyclerNav.apply {
                adapter = adapterNav
                itemAnimator = null
                setHasFixedSize(true)
                setItemViewCacheSize(15)
                recycledViewPool = sharedPool
            }
        }
    }

    private fun setupAdapters() {
        adapterColor.onClick = { colorPos ->
            if (!checkNetwork()) return@onClick
            viewModel.updateColorPosition(viewModel.selectedNavPosition.value, colorPos)
        }

        adapterNav.onClick = { navPos ->
            if (!checkNetwork()) return@onClick
            viewModel.selectNav(navPos)
        }

        adapterPart.onClick = { layerPos, type ->
            if (!checkNetwork()) return@onClick
            val navPos = viewModel.selectedNavPosition.value
            when (type) {
                "none" -> viewModel.updateLayerPosition(navPos, 0)
                "dice" -> viewModel.randomizeCurrentLayer(navPos) // Bạn có thể thêm hàm này nếu muốn random riêng layer
                else -> viewModel.updateLayerPosition(navPos, layerPos)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentCharacter.collectLatest { character ->
                if (character != null) {
                    setupImageViews(character.listPath.size)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedNavPosition.collect { pos ->
                updateNavSelection(pos)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.layerStates.collect {
                renderAllLayers()
                updateColorPanelVisibility()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isFlipped.collect {
                renderAllLayers() // Flip ảnh hưởng toàn bộ
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.randomCount.collect { count ->
                binding.imgRandom.visibility = if (count < 3) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isControlsHidden.collect { hidden ->
                toggleControlsVisibility(hidden)
            }
        }
    }

    private fun updateNavSelection(pos: Int) {
        adapterNav.setSelected(pos)

        val part = viewModel.getCurrentPart() ?: return
        val state = viewModel.layerStates.value.getOrNull(pos) ?: return

        adapterColor.setSelected(state.colorPosition)
        adapterColor.submitList(part.listPath)

        adapterPart.setSelected(state.layerPosition)
        adapterPart.submitList(part.listPath[state.colorPosition].listPath)

        updateColorPanelVisibility()
        renderAllLayers()
    }

    private fun setupImageViews(count: Int) {
        if (imageViews.size == count) return

        binding.characterContainer.removeAllViews()
        imageViews.clear()
        currentRenderedPaths.clear()

        repeat(count) {
            val iv = AppCompatImageView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            binding.characterContainer.addView(iv)
            imageViews.add(iv)
        }
    }

    private fun renderAllLayers() {
        val character = viewModel.currentCharacter.value ?: return

        character.listPath.forEachIndexed { navIndex, part ->
            val state = viewModel.layerStates.value.getOrNull(navIndex) ?: return@forEachIndexed
            val path = part.listPath.getOrNull(state.colorPosition)
                ?.listPath?.getOrNull(state.layerPosition)

            // Giả sử bạn có layerOrder giống cũ (draw order từ filename), ở đây dùng navIndex làm drawIndex tạm
            // Nếu bạn có layerDrawOrder như trước, dùng nó
            val drawIndex = navIndex
            val imageView = imageViews.getOrNull(drawIndex) ?: return@forEachIndexed

            val finalPath = if (path == "none" || state.layerPosition == 0) "" else path.orEmpty()

            if (currentRenderedPaths[drawIndex] == finalPath) return@forEachIndexed
            currentRenderedPaths[drawIndex] = finalPath

            if (finalPath.isEmpty()) {
                imageView.gone()
                Glide.with(requireContext()).clear(imageView)
            } else {
                imageView.visible()
                Glide.with(requireContext())
                    .load(finalPath)
                    .apply(glideOptions)
                    .into(imageView)
            }
        }

        val scaleX = if (viewModel.isFlipped.value) -1f else 1f
        imageViews.forEach { it.scaleX = scaleX }
    }

    private fun updateColorPanelVisibility() {
        val hasMultipleColors = viewModel.getCurrentPart()?.listPath?.size ?: 1 > 1
        binding.imgChangColor.visibility = if (hasMultipleColors) View.VISIBLE else View.INVISIBLE

        val shouldShowPanel = hasMultipleColors && viewModel.showColorPanelForNav.value.getOrNull(viewModel.selectedNavPosition.value) == true
        binding.recycleColorItem.visibility = if (shouldShowPanel) View.VISIBLE else View.GONE
    }

    private fun toggleControlsVisibility(hidden: Boolean) {
        binding.apply {
            val visible = !hidden
            actionBar.btnActionBarCenter.visibility = if (visible) View.VISIBLE else View.GONE
            actionBar.btnActionBarCenter1.visibility = if (visible) View.VISIBLE else View.GONE
            actionBar.btnActionBarRight.visibility = if (visible) View.VISIBLE else View.GONE
            imgChangColor.visibility = if (visible && binding.imgChangColor.visibility == View.VISIBLE) View.VISIBLE else View.GONE
            imgRandom.visibility = if (visible && viewModel.canRandomMore()) View.VISIBLE else View.GONE
            recyclerLayer.visibility = if (visible) View.VISIBLE else View.GONE
            recyclerNav.visibility = if (visible) View.VISIBLE else View.GONE
            recycleColorItem.visibility = if (visible && binding.recycleColorItem.visibility == View.VISIBLE) View.VISIBLE else View.GONE

            actionBar.btnActionBarCenter2.setImageResource(
                if (hidden) R.drawable.ic_show_all_custom else R.drawable.ic_hide_all_custom
            )
        }
    }

    private fun setupActionButtons() {
        binding.apply {
            imgChangColor.onClick {
                viewModel.toggleColorPanel(viewModel.selectedNavPosition.value)
            }

            actionBar.btnActionBarCenter.onClick {
                viewModel.toggleFlip()
            }

            actionBar.btnActionBarCenter1.onClick {
                viewModel.toggleControls()
            }

            actionBar.btnActionBarCenter2.onClick {
                showConfirmDialog(
                    getString(R.string.reset),
                    getString(R.string.do_you_want_to_reset_all),
                    onYes = { viewModel.resetAll() }
                )
            }

            imgRandom.onClick {
                if (checkNetwork()) viewModel.randomizeAll()
            }

            actionBar.btnActionBarLeft.onClick {
                showConfirmDialog(
                    getString(R.string.exit),
                    getString(R.string.haven_t_saved_it_yet_do_you_want_to_exit),
                    onYes = { findNavController().navigateUp() }
                )
            }

            actionBar.btnActionBarRight.onClick {
                saveCharacter()
            }
        }
    }

    private fun checkNetwork(): Boolean {
        val character = viewModel.currentCharacter.value ?: return true
        return if (character.checkDataOnline && !isInternetAvailable(requireContext())) {
            showToast(R.string.please_check_your_network_connection)
            false
        } else true
    }

    private fun saveCharacter() {
        showLoadingSafe()
        val fileName = arguments?.getString("fileName") ?: ""
        val bitmap = binding.characterContainer.drawToBitmap()

        saveBitmap(requireContext(), bitmap, fileName, true) { success, path, oldPath ->
            hideLoadingSafe()
            if (success) {
                if (oldPath.isNotEmpty()) viewModel.deleteMyAvatar(oldPath)

                val savedOrder = mutableListOf<List<Int>>()
                layerDrawOrder.forEach { navIndex ->
                    savedOrder.add(listOf(layerStates[navIndex].layerPos, layerStates[navIndex].colorPos))
                }

                val avatarModel = AvatarModel(
                    path = path,
                    avatarBase = characterData?.avatar.orEmpty(),
                    layerOrder = savedOrder // Tùy theo model của bạn
                )
                viewModel.addMyAvatar(avatarModel)

                findNavController().navigate(
                    R.id.action_custom_to_addcharacter,
                    bundleOf("imagePath" to path)
                )
            } else {
                showToast(R.string.load_fail)
            }
        }
    }

    override fun viewListener() {
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentCustomizeBinding  = FragmentCustomizeBinding.inflate(inflater, container,false)

    override fun onDestroyView() {
        super.onDestroyView()
        imageViews.forEach { Glide.with(this).clear(it) }
        requireActivity().hideNavigation(false)
    }
}