package com.example.basefragment.ui.customview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.core.graphics.toColorInt
import com.example.basefragment.base.AbsBaseAdapter
import com.example.basefragment.base.AbsBaseDiffCallBack
import com.example.basefragment.data.model.ColorModel
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.R
import com.example.basefragment.databinding.ItemColorBinding

class ColorAdapter : AbsBaseAdapter<ColorModel, ItemColorBinding>(R.layout.item_color, DiffColor()) {
    var onClick: ((Int) -> Unit)? = null
    var posColor = 0
    fun setPos(pos: Int) {
        posColor = pos
    }

    class DiffColor : AbsBaseDiffCallBack<ColorModel>() {
        override fun itemsTheSame(oldItem: ColorModel, newItem: ColorModel): Boolean {
            return oldItem.color == newItem.color
        }

        override fun contentsTheSame(oldItem: ColorModel, newItem: ColorModel): Boolean {
            return oldItem.color != newItem.color
        }

    }

    override fun bind(
        binding: ItemColorBinding,
        position: Int,
        data: ColorModel,
        holder: RecyclerView.ViewHolder
    ) {
//        if(position == arr.size-1){
//            setLayoutParam(binding.ctl,0f,0f,0f,0f)
//        }else{
//            setLayoutParam(binding.ctl,0f, dpToPx(100f,binding.root.context),0f,0f)
//        }
        if (posColor == position) {
            binding.imv.visibility = View.VISIBLE
        } else {
            binding.imv.visibility = View.GONE
        }
        binding.bg.setColorFilter("#${data.color}".toColorInt())
        binding.bg.onSingleClick {
            onClick?.invoke(position)
        }
    }
}