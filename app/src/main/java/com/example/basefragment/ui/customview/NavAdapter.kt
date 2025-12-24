package com.example.basefragment.ui.customview

import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.base.AbsBaseAdapter
import com.example.basefragment.base.AbsBaseDiffCallBack
import com.example.basefragment.data.model.BodyPartModel
import com.example.basefragment.utils.onSingleClick
import com.bumptech.glide.Glide
import com.example.basefragment.R
import com.example.basefragment.databinding.ItemNavigationBinding

class NavAdapter :
    AbsBaseAdapter<BodyPartModel, ItemNavigationBinding>(R.layout.item_navigation, DiffNav()) {
    var posNav = 0
    var onClick: ((Int) -> Unit)? = null

    class DiffNav : AbsBaseDiffCallBack<BodyPartModel>() {
        override fun itemsTheSame(oldItem: BodyPartModel, newItem: BodyPartModel): Boolean {
            return oldItem.icon == newItem.icon
        }

        override fun contentsTheSame(oldItem: BodyPartModel, newItem: BodyPartModel): Boolean {
            return oldItem.icon != newItem.icon
        }

    }

    fun setPos(pos: Int) {
        posNav = pos
    }

    override fun bind(
        binding: ItemNavigationBinding,
        position: Int,
        data: BodyPartModel,
        holder: RecyclerView.ViewHolder
    ) {
        Glide.with(binding.root).load(data.icon).into(binding.imv)
        if (posNav == position) {
            binding.bg.setImageResource(R.drawable.bg_navi_true)
        } else {
            binding.bg.setImageResource(R.drawable.bg_navi_false)
        }
        binding.root.onSingleClick {
            onClick?.invoke(position)
        }
    }

}