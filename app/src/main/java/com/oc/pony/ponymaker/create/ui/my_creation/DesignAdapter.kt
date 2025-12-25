package com.oc.pony.ponymaker.create.ui.my_creation

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerDrawable
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ItemMyDesignBinding
import com.oc.pony.ponymaker.create.utils.hide
import com.oc.pony.ponymaker.create.utils.onSingleClick
import com.oc.pony.ponymaker.create.utils.show

class DesignAdapter :
    com.oc.pony.ponymaker.create.base.AbsBaseAdapter<String, ItemMyDesignBinding>(R.layout.item_my_design, DiffCallBack()) {
    var onClick: ((Int, String) -> Unit)? = null
    var arrCheckTick = arrayListOf<Int>()
    var checkLongClick = false
    override fun bind(
        binding: ItemMyDesignBinding,
        position: Int,
        data: String,
        holder: RecyclerView.ViewHolder
    ) {
        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(_root_ide_package_.com.oc.pony.ponymaker.create.utils.shimmer)
        }

        Glide.with(binding.root).load(data).placeholder(shimmerDrawable).into(binding.imvImage)
        binding.imvImage.onSingleClick {
            onClick?.invoke(position,"item")
        }
        binding.btnDelete.onSingleClick {
            onClick?.invoke(position,"delete")
        }
        binding.imvImage.setOnLongClickListener  {
            onClick?.invoke(position, "longclick")
            true
        }
        binding.btnSelect.onSingleClick {
            onClick?.invoke(position, "tick")
        }

        if(checkLongClick){
            binding.btnSelect.show()
            if (position in arrCheckTick) {
                binding.btnSelect.setImageResource(R.drawable.imv_check_true)
            } else {
                binding.btnSelect.setImageResource(R.drawable.imv_check_false)
            }
            binding.btnDelete.hide()
        }else{
            binding.btnSelect.hide()
            binding.btnDelete.show()
        }
    }

    class DiffCallBack : com.oc.pony.ponymaker.create.base.AbsBaseDiffCallBack<String>() {
        override fun itemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun contentsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean {
            return oldItem != newItem
        }
    }
}