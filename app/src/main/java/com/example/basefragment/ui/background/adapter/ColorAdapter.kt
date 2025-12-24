package com.example.basefragment.ui.background.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.base.AbsBaseAdapter
import com.example.basefragment.base.AbsBaseDiffCallBack
import com.example.basefragment.data.model.SelectedModel
import com.example.basefragment.utils.dpToPx
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.R
import com.example.basefragment.databinding.ItemColorBgBinding

class ColorAdapter :
    AbsBaseAdapter<SelectedModel, ItemColorBgBinding>(R.layout.item_color_bg, DiffCallBack()) {
    var onClick: ((Int) -> Unit)? = null
    var posSelect = -1
    override fun bind(
        binding: ItemColorBgBinding,
        position: Int,
        data: SelectedModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.imv.onSingleClick {
            onClick?.invoke(position)
        }
        if(position==0){
            binding.imv.setBackgroundResource(R.drawable.imv_add_color)
        }else{
            binding.imv.setBackgroundColor(data.color)
        }
        if (data.isSelected) {
            binding.vFocus.visibility= View.VISIBLE
        } else {
            binding.vFocus.visibility= View.GONE
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