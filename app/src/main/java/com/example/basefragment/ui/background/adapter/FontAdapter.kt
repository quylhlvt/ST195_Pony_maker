package com.example.basefragment.ui.background.adapter

import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.base.AbsBaseAdapter
import com.example.basefragment.base.AbsBaseDiffCallBack
import com.example.basefragment.data.model.SelectedModel
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.R
import com.example.basefragment.databinding.ItemFontBinding

class FontAdapter :
    AbsBaseAdapter<SelectedModel, ItemFontBinding>(R.layout.item_font, DiffCallBack()) {
    var onClick: ((Int) -> Unit)? = null
    var posSelect = 0
    override fun bind(
        binding: ItemFontBinding,
        position: Int,
        data: SelectedModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.imv.onSingleClick {
            onClick?.invoke(position)
        }

        binding.tv.typeface = ResourcesCompat.getFont(binding.root.context, data.color)
       if(data.isSelected){
           binding.imv.setImageResource(R.drawable.imv_font_true)
       }else{
           binding.imv.setImageResource(R.drawable.imv_font_false)
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