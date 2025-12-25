package com.oc.pony.ponymaker.create.ui.category

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerDrawable
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ItemCategoryBinding
import com.oc.pony.ponymaker.create.utils.onSingleClick

class CategoryAdapter : com.oc.pony.ponymaker.create.base.AbsBaseAdapter<com.oc.pony.ponymaker.create.data.model.CustomModel, ItemCategoryBinding>(
    R.layout.item_category, DiffCallBack()
) {
    var onCLick: ((Int) -> Unit)? = null
    override fun bind(
        binding: ItemCategoryBinding,
        position: Int,
        data: com.oc.pony.ponymaker.create.data.model.CustomModel,
        holder: RecyclerView.ViewHolder
    ) {
        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(_root_ide_package_.com.oc.pony.ponymaker.create.utils.shimmer)
        }
        Glide.with(binding.root).load(data.avt).placeholder(shimmerDrawable).into(binding.imv)
        binding.imv.onSingleClick {
            onCLick?.invoke(position)
        }
    }

    class DiffCallBack : com.oc.pony.ponymaker.create.base.AbsBaseDiffCallBack<com.oc.pony.ponymaker.create.data.model.CustomModel>() {
        override fun itemsTheSame(
            oldItem: com.oc.pony.ponymaker.create.data.model.CustomModel, newItem: com.oc.pony.ponymaker.create.data.model.CustomModel
        ): Boolean {
            return oldItem.avt == newItem.avt
        }

        override fun contentsTheSame(
            oldItem: com.oc.pony.ponymaker.create.data.model.CustomModel, newItem: com.oc.pony.ponymaker.create.data.model.CustomModel
        ): Boolean {
            return oldItem.avt != newItem.avt
        }

    }
}