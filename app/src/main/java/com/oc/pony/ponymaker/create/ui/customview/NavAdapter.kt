package com.oc.pony.ponymaker.create.ui.customview

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ItemNavigationBinding
import com.oc.pony.ponymaker.create.utils.onSingleClick

class NavAdapter :
    com.oc.pony.ponymaker.create.base.AbsBaseAdapter<com.oc.pony.ponymaker.create.data.model.BodyPartModel, ItemNavigationBinding>(R.layout.item_navigation, DiffNav()) {
    var posNav = 0
    var onClick: ((Int) -> Unit)? = null

    class DiffNav : com.oc.pony.ponymaker.create.base.AbsBaseDiffCallBack<com.oc.pony.ponymaker.create.data.model.BodyPartModel>() {
        override fun itemsTheSame(oldItem: com.oc.pony.ponymaker.create.data.model.BodyPartModel, newItem: com.oc.pony.ponymaker.create.data.model.BodyPartModel): Boolean {
            return oldItem.icon == newItem.icon
        }

        override fun contentsTheSame(oldItem: com.oc.pony.ponymaker.create.data.model.BodyPartModel, newItem: com.oc.pony.ponymaker.create.data.model.BodyPartModel): Boolean {
            return oldItem.icon != newItem.icon
        }

    }

    fun setPos(pos: Int) {
        posNav = pos
    }

    override fun bind(
        binding: ItemNavigationBinding,
        position: Int,
        data: com.oc.pony.ponymaker.create.data.model.BodyPartModel,
        holder: RecyclerView.ViewHolder
    ) {
        Glide.with(binding.root).load(data.icon).into(binding.imv)
        if (posNav == position) {
            binding.bg.setImageResource(R.drawable.bg_10_stroke_linear_color_white)
        } else {
            binding.bg.setImageResource(R.drawable.bg_white_8)
        }
        binding.root.onSingleClick {
            onClick?.invoke(position)
        }
    }

}