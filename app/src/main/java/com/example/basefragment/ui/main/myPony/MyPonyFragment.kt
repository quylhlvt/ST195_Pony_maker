package com.example.basefragment.ui.main.myPony

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.gone
import com.example.basefragment.core.extention.invisible
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.core.extention.setTextActionBar
import com.example.basefragment.core.extention.visible
import com.example.basefragment.data.model.mypony.MyAlbumModel
import com.example.basefragment.databinding.FragmentMyPonyBinding
import com.example.basefragment.ui.main.add_character.AddCharacterFragmentDirections
import com.example.basefragment.ui.main.myPony.adapter.MyAvatarAdapter
import com.example.basefragment.ui.main.myPony.adapter.MyDesignAdapter
import com.example.basefragment.ui.main.view.ViewFragmentDirections
import com.example.basefragment.utils.share.whatsapp.StickerPack
import com.example.basefragment.utils.share.whatsapp.WhitelistCheck
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPonyFragment : BaseFragment<FragmentMyPonyBinding, MyPonyViewModel>(
    FragmentMyPonyBinding::inflate,
    MyPonyViewModel::class.java
) {
    private lateinit var myAvatarAdapter: MyAvatarAdapter
    private lateinit var myDesignAdapter: MyDesignAdapter

    // Tab type: true = Avatar, false = Design
    private val isAvatarTab = MutableStateFlow(true)

    companion object {
        private const val ADD_PACK_REQUEST = 200
        private const val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
        private const val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
        private const val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"
        private const val MIN_STICKERS_WHATSAPP = 3
        private const val MAX_STICKERS_WHATSAPP = 30
    }

    override fun initView() {
        setupActionBar()
        setupTabs()
        setupRecyclerViews()
        setupBottomButtons()
        setupTouchListenerForResetSelection()
        // Load initial data
        loadAvatarData()
    }
    private fun setupTouchListenerForResetSelection() {
        binding.apply {
            // Touch cho RecyclerView
            val touchListener = object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    if (e.action == MotionEvent.ACTION_UP) {
                        val child = rv.findChildViewUnder(e.x, e.y)
                        if (child == null) {
                            resetSelection()
                            return true
                        }
                    }
                    return false
                }

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
                override fun onRequestDisallowInterceptTouchEvent(disallow: Boolean) {}
            }

            recycleAvatar.addOnItemTouchListener(touchListener)
            recycleDesign.addOnItemTouchListener(touchListener)
        }
    }
    private fun setupActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
            setTextActionBar(tvCenter, getString(R.string.my_work))
            setImageActionBar(btnActionBarRight, R.drawable.ic_not_select_all)
            btnActionBarRight.invisible()
        }
    }

    private fun setupTabs() {
        binding.apply {
            btnMyAvatar.onClick {
                switchTab(true)
            }

            btnMyDesign.onClick {
                switchTab(false)
            }
        }
    }

    private fun switchTab(isAvatar: Boolean) {
        isAvatarTab.value = isAvatar

        binding.apply {
            if (isAvatar) {
                // Avatar tab
                imvFocusMyDesign.setImageResource(R.drawable.bg_btn_type_unselected)
                imvFocusMyAvatar.setImageResource(R.drawable.bg_btn_type_selected)

                recycleAvatar.visible()
                recycleDesign.gone()
                tvMyAvatar.setTextColor(requireContext().getColor(R.color.white))
                tvMyDesign.setTextColor(requireContext().getColor(R.color.app_color3))

                loadAvatarData()
            } else {
                // Design tab
                imvFocusMyDesign.setImageResource(R.drawable.bg_btn_type_selected)
                imvFocusMyAvatar.setImageResource(R.drawable.bg_btn_type_unselected)
                recycleAvatar.gone()
                recycleDesign.visible()
                tvMyAvatar.setTextColor(requireContext().getColor(R.color.app_color3))
                tvMyDesign.setTextColor(requireContext().getColor(R.color.white))

                loadDesignData()
            }
        }

        // Reset selection when switching tabs
        resetSelection()
    }

    private fun setupRecyclerViews() {
        // Avatar adapter
        myAvatarAdapter = MyAvatarAdapter(requireContext()).apply {
            onItemClick = { path ->
                handleItemClick(path.path, true,1, idEdit= path.idEdit )
            }

            onLongClick = { position ->
                handleLongClick(position, true)
            }

            onItemTick = { position ->
                toggleSelection(position, true)
            }

            onEditClick = { path ->
                navigateToEdit(path)
            }

            onDeleteClick = { path ->
                confirmDelete(arrayListOf(path), true)
            }
        }

        binding.recycleAvatar.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = myAvatarAdapter
            itemAnimator = null
        }

        // Design adapter
        myDesignAdapter = MyDesignAdapter().apply {
            onItemClick = { path ->
                handleItemClick(path, false, 2,"0")
            }

            onLongClick = { position ->
                handleLongClick(position, false)
            }

            onItemTick = { position ->
                toggleSelection(position, false)
            }

            onDeleteClick = { path ->
                confirmDelete(arrayListOf(path), false)
            }
        }

        binding.recycleDesign.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = myDesignAdapter
            itemAnimator = null
        }
    }

    private fun setupBottomButtons() {
        binding.apply {
            btnWhatsapp.onClick { handleWhatsAppShare() }
            btnTelegram.onClick { handleTelegramShare() }
            btnDownload.onClick { handleDownload() }
            actionBar.btnActionBarRight.onClick { handleSelectAll() }
        }
    }

    override fun observeData() {
        // ✅ Observe avatar list TỪ MyPonyViewModel
        viewLifecycleOwner.lifecycleScope.launch {
        viewModelActivity.customizedCharacters.collect { customized ->
           val list= customized.map {
               MyAlbumModel(
                   path = it.imageSave,
                   idEdit = it.id,
                   type = 1
               )

            }
            myAvatarAdapter.submitList(list)
            updateEmptyState(customized.isEmpty() && isAvatarTab.value)

            updateSelectionUI()
        }}
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myAvatarList.collect { list ->
                myAvatarAdapter.submitList(list)

            }
        }

        // ✅ Observe design list
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myDesignList.collect { list ->
                myDesignAdapter.submitList(list)
                updateEmptyState(list.isEmpty() && !isAvatarTab.value)
                updateSelectionUI()
            }
        }

        // ✅ Observe download state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.downloadState.collect { state ->
                when (state) {
                    MyPonyViewModel.DownloadState.LOADING -> {
                        // Show loading
                    }
                    MyPonyViewModel.DownloadState.SUCCESS -> {
                        showToast(R.string.download_success)
                    }
                    MyPonyViewModel.DownloadState.ERROR -> {
                        showToast(R.string.download_failed_please_try_again_later)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.noItem.isVisible = isEmpty
    }

    private fun updateSelectionUI() {
        val currentList = if (isAvatarTab.value) {
            myAvatarAdapter.items
        } else {
            myDesignAdapter.items
        }

        val hasSelection = currentList.any { it.isShowSelection }
        val allSelected = currentList.isNotEmpty() && currentList.all { it.isSelected }
        val selectedCount = currentList.count { it.isSelected }

        // Update action bar
        binding.actionBar.apply {
            if (hasSelection) {
                btnActionBarRight.visible()
                tvCenter.text = "Selected $selectedCount/${currentList.size}"
                btnActionBarRight.setImageResource(
                    if (allSelected) R.drawable.ic_select_all else R.drawable.ic_not_select_all
                )
            } else {
                btnActionBarRight.invisible()
                tvCenter.text = getString(R.string.my_work)
            }
        }

        // Update bottom bar
        if (hasSelection) {
            binding.lnlBottom.visible()
            if (isAvatarTab.value) {
                binding.lnlBottomTop.visible()
                binding.btnDownload.gone()
            } else {
                binding.lnlBottomTop.gone()
                binding.btnDownload.visible()
            }
        } else {
            binding.lnlBottom.gone()
        }
    }

    // ==================== Data Loading ====================

    private fun loadAvatarData() {
        viewModel.loadMyAvatar(requireContext(), true)
    }

    private fun loadDesignData() {
        viewModel.loadMyDesign(requireContext())
    }

    // ==================== Selection Management ====================

    private fun handleItemClick(path: String, isAvatar: Boolean, type:Int, idEdit: String) {
        val currentList = if (isAvatar) myAvatarAdapter.items else myDesignAdapter.items

        if (currentList.any { it.isShowSelection }) {
            // In selection mode, toggle selection
            val position = currentList.indexOfFirst { it.path == path }
            if (position >= 0) {
                toggleSelection(position, isAvatar)
            }
        } else {
            // Normal mode, navigate to view
            navigateToView(path,type, idEdit)
        }
    }

    private fun handleLongClick(position: Int, isAvatar: Boolean) {
        viewModel.showLongClick(position, isAvatar)
        updateSelectionUI()
    }

    private fun toggleSelection(position: Int, isAvatar: Boolean) {
        viewModel.toggleSelect(position, isAvatar)
        updateSelectionUI()

        // Auto-exit if no selection
        val currentList = if (isAvatar) myAvatarAdapter.items else myDesignAdapter.items
        if (currentList.none { it.isSelected }) {
            resetSelection()
        }
    }

    private fun handleSelectAll() {
        val currentList = if (isAvatarTab.value) {
            myAvatarAdapter.items
        } else {
            myDesignAdapter.items
        }

        val shouldSelectAll = !currentList.all { it.isSelected }
        viewModel.selectAll(shouldSelectAll, isAvatarTab.value)
        updateSelectionUI()
    }

    private fun resetSelection() {
        viewModel.selectAll(false, isAvatarTab.value)
        updateSelectionUI()
    }

    private fun getSelectedItems(): List<MyAlbumModel> {
        return if (isAvatarTab.value) {
            myAvatarAdapter.items.filter { it.isSelected }
        } else {
            myDesignAdapter.items.filter { it.isSelected }
        }
    }

    // ==================== Navigation ====================

    private fun navigateToView(path: String, type: Int, idEdit: String) {


        val action = MyPonyFragmentDirections
            .actionMyponyToView(path,idEdit,type)
        findNavController().navigate(action)
        // findNavController().navigate(R.id.action_to_view, bundle)
    }

    private fun navigateToEdit(path: String) {
//        findNavController().navigate(
//            R.id.action_mypony_to_custom,
//            bundleOf("characterIndex" to path, "isQuickRandom" to true),
//        )
        val globalIndex = viewModelActivity.characters.value.indexOfFirst {
        it.id == path
    }

    if (globalIndex >= 0) {
        findNavController().navigate(
            R.id.action_mypony_to_custom,
            bundleOf("characterIndex" to globalIndex)
        )
    }
    }

    // ==================== Actions ====================

    private fun confirmDelete(paths: ArrayList<String>, isAvatar: Boolean) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(R.string.are_you_sure_want_to_delete_this_item)
            .setPositiveButton("Delete") { _, _ ->
                if (isAvatar) {
                    viewModel.deleteItem(requireContext(), paths)
                } else {
                    viewModel.deleteItemDesign(paths, requireContext())
                }
                resetSelection()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleDownload() {
        val selected = getSelectedItems()
        if (selected.isEmpty()) {
            showToast(R.string.please_select_an_image)
            return
        }

        val paths = ArrayList(selected.map { it.path })
        viewModel.downloadFiles(requireContext(), paths)
        resetSelection()
    }

    // ==================== WhatsApp Integration ====================

    private fun handleWhatsAppShare() {
        val selected = getSelectedItems()

        when {
            selected.isEmpty() -> {
                showToast(R.string.please_select_an_image)
                return
            }
            selected.size < MIN_STICKERS_WHATSAPP -> {
                showToast(R.string.limit_3_items)
                return
            }
            selected.size > MAX_STICKERS_WHATSAPP -> {
                showToast(R.string.limit_30_items)
                return
            }
        }

        showPackNameDialog { packName ->
            val paths = ArrayList(selected.map { it.path })
            viewModel.addToWhatsapp(requireContext(), packName, paths) { stickerPack ->
                if (stickerPack != null) {
                    addToWhatsapp(stickerPack)
                    resetSelection()
                } else {
                    showToast("Failed to create sticker pack")
                }
            }
        }
    }

    private fun showPackNameDialog(onConfirm: (String) -> Unit) {
        val editText = android.widget.EditText(requireContext()).apply {
            hint = "Enter sticker pack name"
            setText("My Ponies ${System.currentTimeMillis() % 1000}")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Create Sticker Pack")
            .setView(editText)
            .setPositiveButton("Create") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    onConfirm(name)
                } else {
                    showToast("Please enter a pack name")
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun addToWhatsapp(sp: StickerPack) {
        if (sp.stickers.size >= MIN_STICKERS_WHATSAPP) {
            addStickerPackageToWhatsApp(sp)
        } else {
            showErrorDialog()
        }
    }

    private fun addStickerPackageToWhatsApp(sp: StickerPack) {
        val intent = Intent()
        intent.action = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
        intent.putExtra(EXTRA_STICKER_PACK_ID, sp.identifier)
        intent.putExtra(EXTRA_STICKER_PACK_AUTHORITY,
            WhitelistCheck.CONTENT_PROVIDER_AUTHORITY)
        intent.putExtra(EXTRA_STICKER_PACK_NAME, sp.name)

        try {
            @Suppress("DEPRECATION")
            startActivityForResult(intent, ADD_PACK_REQUEST)
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.invalid_action_msg)
        } catch (e: Exception) {
            Log.e("MyPonyFragment", "Error adding sticker pack", e)
            showToast("Failed to add sticker pack")
        }
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.invalid_action)
            .setMessage(R.string.invalid_action_msg)
            .setNegativeButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_PACK_REQUEST) {
            when (resultCode) {
                android.app.Activity.RESULT_OK -> {
                    showToast("Sticker pack added successfully")
                }
                android.app.Activity.RESULT_CANCELED -> {
                    val validationError = data?.getStringExtra("validation_error")
                    if (validationError != null) {
                        Log.e("MyPonyFragment", "Validation failed: $validationError")
                        showToast("Failed: $validationError")
                    } else {
                        showToast("Cancelled")
                    }
                }
            }
        }
    }

    // ==================== Telegram Integration ====================

    private fun handleTelegramShare() {
        val selected = getSelectedItems()

        if (selected.isEmpty()) {
            showToast(R.string.please_select_an_image)
            return
        }

        val paths = ArrayList(selected.map { it.path })
        viewModel.addToTelegram(requireContext(), paths)
        resetSelection()
    }

    // ==================== Utility ====================

    private fun showToast(messageResId: Int) {
        android.widget.Toast.makeText(requireContext(), messageResId, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    // ==================== View Listener ====================

    override fun viewListener() {
        binding.actionBar.apply {
            btnActionBarLeft.setOnClickListener {
                findNavController().navigateUp()
                if (myAvatarAdapter.items.any { it.isShowSelection } ||
                    myDesignAdapter.items.any { it.isShowSelection }) {
                    resetSelection()

                }
            }
        }
    }

    override fun bindViewModel() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMyPonyBinding = FragmentMyPonyBinding.inflate(inflater, container, false)

    override fun onResume() {
        super.onResume()
        // Reload data when returning to fragment
        if (isAvatarTab.value) {
            loadAvatarData()
        } else {
            loadDesignData()
        }
    }
}