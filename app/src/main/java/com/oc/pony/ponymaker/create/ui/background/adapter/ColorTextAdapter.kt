package com.oc.pony.ponymaker.create.ui.background.adapter

import androidx.recyclerview.widget.RecyclerView
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ItemColorEdtBinding
import com.oc.pony.ponymaker.create.utils.hide
import com.oc.pony.ponymaker.create.utils.onSingleClick
import com.oc.pony.ponymaker.create.utils.show

class ColorTextAdapter :
    com.oc.pony.ponymaker.create.base.AbsBaseAdapter<com.oc.pony.ponymaker.create.data.model.SelectedModel, ItemColorEdtBinding>(R.layout.item_color_edt, DiffCallBack()) {
    var onClick: ((Int) -> Unit)? = null
    var posSelect = 1
    override fun bind(
        binding: ItemColorEdtBinding,
        position: Int,
        data: com.oc.pony.ponymaker.create.data.model.SelectedModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.bg.onSingleClick {

            onClick?.invoke(position)
        }
        binding.bg.setBackgroundColor(data.color)
        if(position == 0){
            binding.imvPlus.show()
        }else{
            binding.imvPlus.hide()
        }
        if(data.isSelected){
            binding.imv.show()
        }else{
            binding.imv.hide()
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