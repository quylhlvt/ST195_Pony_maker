package com.oc.pony.ponymaker.create.ui.customview

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ItemPartBinding
import com.oc.pony.ponymaker.create.utils.DataHelper.dp
import com.oc.pony.ponymaker.create.utils.DataHelper.setMargins
import com.oc.pony.ponymaker.create.utils.onSingleClick

class PartAdapter : com.oc.pony.ponymaker.create.base.AbsBaseAdapter<String, ItemPartBinding>(R.layout.item_part, PathDiff()) {
    var onClick: ((Int,String) -> Unit)? = null
    var posPath = 0
    //    var checkOnline = false
    fun setPos(pos: Int) {
        posPath = pos
    }

    class PathDiff : com.oc.pony.ponymaker.create.base.AbsBaseDiffCallBack<String>() {
        override fun itemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun contentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem != newItem
        }

    }

    override fun bind(
        binding: ItemPartBinding,
        position: Int,
        data: String,
        holder: RecyclerView.ViewHolder
    ) {
        binding.apply {
            bg.apply {
                if (posPath == position) strokeWidth = 3.dp(context) else strokeWidth = 1.dp(context)
                strokeColor = ContextCompat.getColor(
                    context,
                    if (posPath == position) R.color.white else R.color.stroke_layercustom_select
                )
            }

            imageBgItem.setBackgroundResource( if (posPath == position) R.drawable.bg_16_stroke_linear_color else R.drawable.bg_white_16)
        }

        binding.root.onSingleClick {
            onClick?.invoke(position,data)
        }
        when (data) {
            "none" -> {
                binding.imv.setMargins(26 ,26,26,26)
                Glide.with(binding.root).load(R.drawable.ic_none).into(binding.imv)
            }

            "dice" -> {
                Glide.with(binding.root).load(R.drawable.ic_random_layer).into(binding.imv)
            }

            else -> {
                Glide.with(binding.root).load(data).into(binding.imv)
            }
        }

    }
}