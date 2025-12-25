package com.oc.pony.ponymaker.create.ui.tutorial

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oc.pony.ponymaker.create.databinding.ItemTutorialBinding


class ViewPagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var data = arrayListOf<com.oc.pony.ponymaker.create.data.model.TutorialModel>()
    fun getData(mData: List<com.oc.pony.ponymaker.create.data.model.TutorialModel>) {
        data = mData as ArrayList<com.oc.pony.ponymaker.create.data.model.TutorialModel>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var binding = ItemTutorialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(position)
        }
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(val binding: ItemTutorialBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.mTutorialModel = data[position]
            binding.imv.setImageResource(data[position].bg)

        }
    }
}