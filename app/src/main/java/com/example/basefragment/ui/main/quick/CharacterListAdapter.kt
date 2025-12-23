package com.example.basefragment.ui.main.quick

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.core.extention.loadImage
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.databinding.ItemCharacterBinding
import java.io.File

/**
 * ✅ Adapter hiển thị danh sách characters với ảnh đã lưu
 * ✅ Hỗ trợ incremental display - hiển thị ngay khi có item mới
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

    /**
     * ✅ Submit list với animation mượt mà
     * Gọi submitList() sẽ tự động trigger DiffUtil để so sánh và update
     */
    fun updateList(newList: List<CustomModel>) {
        submitList(newList.toList()) // toList() để tạo copy mới, trigger DiffUtil
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
                    binding.imvImage.setImageBitmap(bitmap)
                } else {
                    // Fallback nếu không load được
                    loadImage(character.avatar, binding.imvImage)
                }
            } else {
                // Fallback: Load avatar từ assets
                loadImage(character.avatar, binding.imvImage)
            }

            binding.root.setOnClickListener {
                onCharacterClick(character, position)
            }

            binding.root.setOnLongClickListener {
                onCharacterLongClick?.invoke(character, position)
                true
            }
        }
    }

    class CharacterDiffCallback : DiffUtil.ItemCallback<CustomModel>() {
        override fun areItemsTheSame(oldItem: CustomModel, newItem: CustomModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CustomModel, newItem: CustomModel): Boolean {
            // ✅ So sánh các thuộc tính quan trọng
            return oldItem.id == newItem.id &&
                    oldItem.imageSave == newItem.imageSave &&
                    oldItem.updatedAt == newItem.updatedAt
        }
    }
}