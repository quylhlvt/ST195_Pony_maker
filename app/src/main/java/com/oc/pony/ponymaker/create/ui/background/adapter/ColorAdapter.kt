package com.oc.pony.ponymaker.create.ui.background.adapter

import androidx.recyclerview.widget.RecyclerView
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ItemColorBgBinding
import com.oc.pony.ponymaker.create.utils.hide
import com.oc.pony.ponymaker.create.utils.onSingleClick
import com.oc.pony.ponymaker.create.utils.show

class ColorAdapter :
    com.oc.pony.ponymaker.create.base.AbsBaseAdapter<com.oc.pony.ponymaker.create.data.model.SelectedModel, ItemColorBgBinding>(R.layout.item_color_bg, DiffCallBack()) {
    var onClick: ((Int) -> Unit)? = null
    var posSelect = -1
    override fun bind(
        binding: ItemColorBgBinding,
        position: Int,
        data: com.oc.pony.ponymaker.create.data.model.SelectedModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.imvColor.onSingleClick {
            onClick?.invoke(position)
        }
        if(position==0){
            binding.imvColor.setBackgroundResource(R.drawable.imv_add_color)
        }else{
            binding.imvColor.setBackgroundColor(data.color)
        }
        if (data.isSelected) {
            binding.vFocus1.show()
        } else {
            binding.vFocus1.hide()
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