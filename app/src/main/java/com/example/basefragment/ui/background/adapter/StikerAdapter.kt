package com.example.basefragment.ui.background.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.base.AbsBaseAdapter
import com.example.basefragment.base.AbsBaseDiffCallBack
import com.example.basefragment.data.model.SelectedModel
import com.example.basefragment.utils.onSingleClick
import com.bumptech.glide.Glide
import com.example.basefragment.R
import com.example.basefragment.databinding.ItemStikerBgBinding

class StikerAdapter :
    AbsBaseAdapter<SelectedModel, ItemStikerBgBinding>(R.layout.item_stiker_bg, DiffCallBack()) {
    var onClick: ((String) -> Unit)? = null
    override fun bind(
        binding: ItemStikerBgBinding,
        position: Int,
        data: SelectedModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.imv.onSingleClick {
            onClick?.invoke(data.path)
        }
        Glide.with(binding.root).load(data.path).into(binding.imv)
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