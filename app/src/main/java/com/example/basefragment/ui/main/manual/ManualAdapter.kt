package com.example.basefragment.ui.main.manual

import android.annotation.SuppressLint
import android.content.Context
import com.bumptech.glide.Glide
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseAdapter
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.ItemBombManualUnchooseBinding
import com.example.basefragment.utils.music.SoundEffect

class ManualAdapter(
    private val context: Context, private  val checkPlay: Boolean= false
) : BaseAdapter<ManualModel, ItemBombManualUnchooseBinding>(
    ItemBombManualUnchooseBinding::inflate
) {
    fun updateList(newList: List<ManualModel>) {
        submitList(newList)
    }
    private val MAX_SELECT = 3

    var onSelectionChanged: ((List<ManualModel>) -> Unit)? = null

    override fun onBind(
        binding: ItemBombManualUnchooseBinding,
        item: ManualModel,
        position: Int
    ) {
        val selectedCount = items.count { it.bomb }
        val isLocked = selectedCount >= MAX_SELECT && !item.bomb

        binding.apply {
            if (!checkPlay){
                imv.setBackgroundResource(R.drawable.img_unchoose_manual2)
            }
            // Load image
            if (item.bomb) {
                imv.setImageResource(R.drawable.ic_character)
            } else {
                imv.setImageDrawable(null)
            }
            // UI state
            root.isSelected = item.bomb
            root.isEnabled = !isLocked

            root.setOnClickListener{
                if (isLocked) return@setOnClickListener

                item.bomb = !item.bomb
                SoundEffect.playClick1(context)

                // ⚠️ RE-BIND TOÀN BỘ để update lock
                notifyDataSetChanged()

                onSelectionChanged?.invoke(items.filter { it.bomb })
            }
        }
    }

    fun getItems(): List<ManualModel> =
        items
    fun getSelectItems(): List<ManualModel> =
        items.filter { it.bomb }
}
