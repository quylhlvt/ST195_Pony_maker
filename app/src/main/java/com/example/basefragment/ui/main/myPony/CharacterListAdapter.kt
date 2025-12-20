package com.example.basefragment.ui.main.myPony

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.R
import com.example.basefragment.core.extention.gone
import com.example.basefragment.core.extention.loadImage
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.core.extention.visible
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.databinding.ItemCharacterBinding
import kotlinx.coroutines.launch
import java.io.File

/**
 * ✅ Adapter hiển thị danh sách characters với ảnh đã lưu
 */
class CharacterListAdapter(
    private val onCharacterClick: (CustomModel, Int) -> Unit,
    private val onCharacterLongClick: ((CustomModel, Int) -> Unit)? = null
) : ListAdapter<CustomModel, CharacterListAdapter.CharacterViewHolder>(CharacterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val binding = ItemCharacterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CharacterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class CharacterViewHolder(
        private val binding: ItemCharacterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(character: CustomModel, position: Int) {
            // ✅ Load ảnh đã lưu từ imageSave
            if (character.imageSave.isNotEmpty() && File(character.imageSave).exists()) {
                // Load từ file path
                val bitmap = BitmapFactory.decodeFile(character.imageSave)
                if (bitmap != null) {
                    binding.imgCharacter.setImageBitmap(bitmap)
                } else {
                    // Fallback nếu không load được
                    loadImage(character.avatar, binding.imgCharacter)
                }
            } else {
                // Fallback: Load avatar từ assets
                loadImage(character.avatar, binding.imgCharacter)
            }

            // ✅ Hiển thị thông tin khác (optional)
            binding.tvCharacterName.text = "Character #${position + 1}"
            binding.tvLastUpdated.text = formatTimestamp(character.updatedAt)

            // ✅ Click listeners
            binding.root.setOnClickListener {
                onCharacterClick(character, position)
            }

            binding.root.setOnLongClickListener {
                onCharacterLongClick?.invoke(character, position)
                true
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val diff = System.currentTimeMillis() - timestamp
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            return when {
                days > 0 -> "$days days ago"
                hours > 0 -> "$hours hours ago"
                minutes > 0 -> "$minutes minutes ago"
                else -> "Just now"
            }
        }
    }

    class CharacterDiffCallback : DiffUtil.ItemCallback<CustomModel>() {
        override fun areItemsTheSame(oldItem: CustomModel, newItem: CustomModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CustomModel, newItem: CustomModel): Boolean {
            return oldItem == newItem
        }
    }
}

// ✅ ItemCharacterBinding Layout (item_character.xml)
/*
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="12dp">

        <ImageView
            android:id="@+id/imgCharacter"
            android:layout_width="match_parent"
            android:layout_height="200dp"
            android:scaleType="centerCrop"
            android:background="#F0F0F0" />

        <TextView
            android:id="@+id/tvCharacterName"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="Character"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="@android:color/black" />

        <TextView
            android:id="@+id/tvLastUpdated"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:text="Last updated"
            android:textSize="12sp"
            android:textColor="@android:color/darker_gray" />

    </LinearLayout>

</androidx.cardview.widget.CardView>
*/

//





//binding.actionBar.apply {
//    setImageActionBar(btnActionBarLeft, R.drawable.back_app)
//}
//setupRecyclerView()
//
//characterAdapter = CharacterListAdapter(
//onCharacterClick = { character, position ->
//    // ✅ Navigate to edit (tìm index trong combined list)
//    val globalIndex = viewModelActivity.characters.value.indexOfFirst {
//        it.id == character.id
//    }
//
//    if (globalIndex >= 0) {
//        findNavController().navigate(
//            R.id.action_mypony_to_custom,
//            bundleOf("characterIndex" to globalIndex)
//        )
//    }
//},
//onCharacterLongClick = { character, position ->
//    showCharacterOptions(character)
//}
//binding.recycleAvatar.apply {
//    layoutManager = GridLayoutManager(requireContext(), 2)
//    adapter = characterAdapter
//}
//override fun observeData() {
//    // ✅ Chỉ hiển thị customized characters (không có templates)
//    viewLifecycleOwner.lifecycleScope.launch {
//        viewModelActivity.customizedCharacters.collect { customized ->
//            characterAdapter.submitList(customized)
//
//            if (customized.isEmpty()) {
//                binding.noItem.visible()
//                binding.recycleAvatar.gone()
//            } else {
//                binding.noItem.gone()
//                binding.recycleAvatar.visible()
//            }
//        }
//    }
//}
//private fun showCharacterOptions(character: CustomModel) {
//    android.app.AlertDialog.Builder(requireContext())
//        .setTitle("Character Options")
//        .setItems(arrayOf("Edit", "Delete", "Duplicate")) { _, which ->
//            when (which) {
//                0 -> editCharacter(character)
//                1 -> confirmDeleteCharacter(character)
//                2 -> duplicateCharacter(character)
//            }
//        }
//        .setNegativeButton("Cancel", null)
//        .show()
//}
//private fun editCharacter(character: CustomModel) {
//    val globalIndex = viewModelActivity.characters.value.indexOfFirst {
//        it.id == character.id
//    }
//
//    if (globalIndex >= 0) {
//        findNavController().navigate(
//            R.id.action_mypony_to_custom,
//            bundleOf("characterIndex" to globalIndex)
//        )
//    }
//}
//
//private fun confirmDeleteCharacter(character: CustomModel) {
//    android.app.AlertDialog.Builder(requireContext())
//        .setTitle("Delete Character")
//        .setMessage("Are you sure you want to delete this character?")
//        .setPositiveButton("Delete") { _, _ ->
//            viewModelActivity.deleteCharacter(character.id)
//        }
//        .setNegativeButton("Cancel", null)
//        .show()
//}
//
//private fun duplicateCharacter(character: CustomModel) {
//    val duplicated = character.copy(
//        id = java.util.UUID.randomUUID().toString(),
//        updatedAt = System.currentTimeMillis(),
//        imageSave = "" // ✅ Clear image, sẽ capture lại khi save
//    )
//    viewModelActivity.updateOrAddCharacter(duplicated)
//}