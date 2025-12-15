package com.example.basefragment.ui.main.createPony

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.basefragment.data.model.custom.CustomModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
class ChoosePonyAdapter(
    private val onItemClick: (CustomModel, Int) -> Unit

) : ListAdapter<CustomModel, ChoosePonyAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChoosePonyBinding.inflate(
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
        private val binding: ItemChoosePonyBinding,
        private val onItemClick: (CustomModel, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(character: CustomModel, position: Int) {
            binding.apply {
                // Set character name
                tvCharacterName.text = character.name

                // Load thumbnail image
                Glide.with(itemView.context)
                    .load(character.thumbnail)
                    .into(ivThumbnail)

                // Show premium badge if needed
                if (character.isPremium) {
                    ivPremiumBadge.visibility = android.view.View.VISIBLE
                } else {
                    ivPremiumBadge.visibility = android.view.View.GONE
                }

                // Click listener
                root.setOnClickListener {
                    onItemClick(character, position)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CustomModel>() {
        override fun areItemsTheSame(oldItem: CustomModel, newItem: CustomModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CustomModel, newItem: CustomModel): Boolean {
            return oldItem == newItem
        }
    }
}
