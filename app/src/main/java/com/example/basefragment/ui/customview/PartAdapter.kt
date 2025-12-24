package com.example.basefragment.ui.customview

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.base.AbsBaseAdapter
import com.example.basefragment.base.AbsBaseDiffCallBack
import com.example.basefragment.utils.onSingleClick
import com.bumptech.glide.Glide
import com.example.basefragment.R
import com.example.basefragment.databinding.ItemPartBinding
import com.example.basefragment.utils.DataHelper.dp

class PartAdapter : AbsBaseAdapter<String, ItemPartBinding>(R.layout.item_part, PathDiff()) {
    var onClick: ((Int,String) -> Unit)? = null
    var posPath = 0
//    var checkOnline = false
    fun setPos(pos: Int) {
        posPath = pos
    }

    class PathDiff : AbsBaseDiffCallBack<String>() {
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
               strokeWidth = 3.dp(context)
               strokeColor = ContextCompat.getColor(
                   context,
                   if (posPath == position) R.color.white else R.color.stroke_layercustom_select
               )
           }
       }

        binding.root.onSingleClick {
            onClick?.invoke(position,data)
        }
        when (data) {
            "none" -> {
                Glide.with(binding.root).load(R.drawable.imv_null).into(binding.imv)
            }

            "dice" -> {
                Glide.with(binding.root).load(R.drawable.imv_dice).into(binding.imv)
            }

            else -> {
                    Glide.with(binding.root).load(data).into(binding.imv)
            }
        }

    }
}