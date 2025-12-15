package com.example.basefragment.ui.main.createPony

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.basefragment.core.extention.gone
import com.example.basefragment.core.extention.loadImage
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.databinding.ItemChooseBinding

class ChoosePonyAdapter(
    private val onItemClick: (CustomModel, Int) -> Unit

) : ListAdapter<CustomModel, ChoosePonyAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChooseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class ViewHolder(
        private val binding: ItemChooseBinding,
        private val onItemClick: (CustomModel, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(character: CustomModel, position: Int) {
            binding.apply {
                // Load thumbnail image
                loadImage(character.avatar, imvImage, onDismissLoading = {
                    sflShimmer.stopShimmer()
                    sflShimmer.gone()
                })

                // Click listener
                root.setOnClickListener {
                    onItemClick(character, position)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CustomModel>() {
        override fun areItemsTheSame(oldItem: CustomModel, newItem: CustomModel): Boolean {
            return oldItem.avatar == newItem.avatar
        }

        override fun areContentsTheSame(oldItem: CustomModel, newItem: CustomModel): Boolean {
            return oldItem == newItem
        }
    }
}
