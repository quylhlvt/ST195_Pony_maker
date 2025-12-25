package com.oc.pony.ponymaker.create.ui.background.adapter

import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ItemFontBinding
import com.oc.pony.ponymaker.create.utils.onSingleClick

class FontAdapter :
    com.oc.pony.ponymaker.create.base.AbsBaseAdapter<com.oc.pony.ponymaker.create.data.model.SelectedModel, ItemFontBinding>(R.layout.item_font, DiffCallBack()) {
    var onClick: ((Int) -> Unit)? = null
    var posSelect = 0
    override fun bind(
        binding: ItemFontBinding,
        position: Int,
        data: com.oc.pony.ponymaker.create.data.model.SelectedModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.imv.onSingleClick {
            onClick?.invoke(position)
        }

        binding.tv.typeface = ResourcesCompat.getFont(binding.root.context, data.color)
       if(data.isSelected){
           binding.imv.setImageResource(R.drawable.imv_font_true)
           binding.tv.setTextColor("#ffffff".toColorInt())
       }else{
           binding.imv.setImageResource(R.drawable.imv_font_false)
           binding.tv.setTextColor("#1F2F4F".toColorInt())
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