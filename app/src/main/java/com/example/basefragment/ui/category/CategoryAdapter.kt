package com.example.basefragment.ui.category

import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.base.AbsBaseAdapter
import com.example.basefragment.base.AbsBaseDiffCallBack
import com.example.basefragment.data.model.CustomModel
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.utils.shimmer
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerDrawable
import com.example.basefragment.R
import com.example.basefragment.databinding.ItemCategoryBinding

class CategoryAdapter : AbsBaseAdapter<CustomModel, ItemCategoryBinding>(
    R.layout.item_category, DiffCallBack()
) {
    var onCLick: ((Int) -> Unit)? = null
    override fun bind(
        binding: ItemCategoryBinding,
        position: Int,
        data: CustomModel,
        holder: RecyclerView.ViewHolder
    ) {
        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(shimmer)
        }
        Glide.with(binding.root).load(data.avt).placeholder(shimmerDrawable).into(binding.imv)
        binding.imv.onSingleClick {
            onCLick?.invoke(position)
        }
    }

    class DiffCallBack : AbsBaseDiffCallBack<CustomModel>() {
        override fun itemsTheSame(
            oldItem: CustomModel, newItem: CustomModel
        ): Boolean {
            return oldItem.avt == newItem.avt
        }

        override fun contentsTheSame(
            oldItem: CustomModel, newItem: CustomModel
        ): Boolean {
            return oldItem.avt != newItem.avt
        }

    }
}