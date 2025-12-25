package com.oc.pony.ponymaker.create.ui.customview

import android.view.View
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ItemColorBinding
import com.oc.pony.ponymaker.create.utils.onSingleClick

class ColorAdapter : com.oc.pony.ponymaker.create.base.AbsBaseAdapter<com.oc.pony.ponymaker.create.data.model.ColorModel, ItemColorBinding>(R.layout.item_color, DiffColor()) {
    var onClick: ((Int) -> Unit)? = null
    var posColor = 0
    fun setPos(pos: Int) {
        posColor = pos
    }

    class DiffColor : com.oc.pony.ponymaker.create.base.AbsBaseDiffCallBack<com.oc.pony.ponymaker.create.data.model.ColorModel>() {
        override fun itemsTheSame(oldItem: com.oc.pony.ponymaker.create.data.model.ColorModel, newItem: com.oc.pony.ponymaker.create.data.model.ColorModel): Boolean {
            return oldItem.color == newItem.color
        }

        override fun contentsTheSame(oldItem: com.oc.pony.ponymaker.create.data.model.ColorModel, newItem: com.oc.pony.ponymaker.create.data.model.ColorModel): Boolean {
            return oldItem.color != newItem.color
        }

    }

    override fun bind(
        binding: ItemColorBinding,
        position: Int,
        data: com.oc.pony.ponymaker.create.data.model.ColorModel,
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