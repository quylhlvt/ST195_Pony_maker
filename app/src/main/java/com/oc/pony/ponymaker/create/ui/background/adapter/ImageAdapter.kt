package com.oc.pony.ponymaker.create.ui.background.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ItemImageBinding
import com.oc.pony.ponymaker.create.utils.DataHelper.setMargins
import com.oc.pony.ponymaker.create.utils.hide
import com.oc.pony.ponymaker.create.utils.onSingleClick
import com.oc.pony.ponymaker.create.utils.show

class ImageAdapter :
    com.oc.pony.ponymaker.create.base.AbsBaseAdapter<com.oc.pony.ponymaker.create.data.model.SelectedModel, ItemImageBinding>(R.layout.item_image, DiffCallBack()) {
    var onClick: ((Int) -> Unit)? = null
    var posSelect = -1
    override fun bind(
        binding: ItemImageBinding,
        position: Int,
        data: com.oc.pony.ponymaker.create.data.model.SelectedModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.tvAddImage.isSelected = true
        binding.imvImage.onSingleClick {
            onClick?.invoke(position)
        }
        Glide.with(binding.root).load(data.path).into(binding.imvImage)
        if (position == 0) {
            binding.lnlAddItem.show()
        } else {
            binding.lnlAddItem.hide()
        }
        if (data.isSelected) {
            binding.vFocus.show()

        } else {
            binding.vFocus.hide()

        }
    }

    class DiffCallBack : com.oc.pony.ponymaker.create.base.AbsBaseDiffCallBack<com.oc.pony.ponymaker.create.data.model.SelectedModel>() {
        override fun itemsTheSame(
            oldItem: com.oc.pony.ponymaker.create.data.model.SelectedModel,
            newItem: com.oc.pony.ponymaker.create.data.model.SelectedModel
        ): Boolean {
            return oldItem == newItem
        }

        override fun contentsTheSame(
            oldItem: com.oc.pony.ponymaker.create.data.model.SelectedModel,
            newItem: com.oc.pony.ponymaker.create.data.model.SelectedModel
        ): Boolean {
            return oldItem.path != newItem.path || oldItem.isSelected != newItem.isSelected
        }

    }
}