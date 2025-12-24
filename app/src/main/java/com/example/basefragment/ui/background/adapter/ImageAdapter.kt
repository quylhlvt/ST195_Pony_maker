package com.example.basefragment.ui.background.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.base.AbsBaseAdapter
import com.example.basefragment.base.AbsBaseDiffCallBack
import com.example.basefragment.data.model.SelectedModel
import com.example.basefragment.utils.dpToPx
import com.example.basefragment.utils.hide
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.utils.show
import com.bumptech.glide.Glide
import com.example.basefragment.R
import com.example.basefragment.databinding.ItemImageBinding

class ImageAdapter :
    AbsBaseAdapter<SelectedModel, ItemImageBinding>(R.layout.item_image, DiffCallBack()) {
    var onClick: ((Int) -> Unit)? = null
    var posSelect = -1
    override fun bind(
        binding: ItemImageBinding,
        position: Int,
        data: SelectedModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.imv.onSingleClick {
            onClick?.invoke(position)
        }
        Glide.with(binding.root).load(data.path).into(binding.imv)
        if (position == 0) {
            binding.ll0.show()
        } else {
            binding.ll0.hide()
        }
        if (data.isSelected) {
            binding.cv.strokeWidth = dpToPx(2f, binding.root.context).toInt()
        } else {
            binding.cv.strokeWidth = dpToPx(0f, binding.root.context).toInt()
        }
    }

    class DiffCallBack : AbsBaseDiffCallBack<SelectedModel>() {
        override fun itemsTheSame(
            oldItem: SelectedModel,
            newItem: SelectedModel
        ): Boolean {
            return oldItem == newItem
        }

        override fun contentsTheSame(
            oldItem: SelectedModel,
            newItem: SelectedModel
        ): Boolean {
            return oldItem.path != newItem.path || oldItem.isSelected != newItem.isSelected
        }

    }
}