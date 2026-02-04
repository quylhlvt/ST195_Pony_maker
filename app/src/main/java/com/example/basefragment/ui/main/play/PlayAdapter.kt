package com.example.basefragment.ui.main.play

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.R
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.ItemBombPlay1UnchooseBinding
import com.example.basefragment.databinding.ItemBombPlay2UnchooseBinding

class PlayAdapter(
    private val context: Context,
    private val isPlayer1: Boolean
) : ListAdapter<ManualModel, RecyclerView.ViewHolder>(DiffCallback()) {

    var onItemClick: ((ManualModel, Int) -> Unit)? = null
    private val clickedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (isPlayer1) {
            val binding = ItemBombPlay1UnchooseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            Player1ViewHolder(binding)
        } else {
            val binding = ItemBombPlay2UnchooseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            Player2ViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is Player1ViewHolder -> holder.bind(item, position)
            is Player2ViewHolder -> holder.bind(item, position)
        }
    }

    inner class Player1ViewHolder(
        private val binding: ItemBombPlay1UnchooseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ManualModel, position: Int) {
            binding.apply {
                // Set initial state (unchoose/hidden)
                if (clickedPositions.contains(position)) {
                    // Show revealed state
                    if (item.bomb) {
                        imv.setImageResource(R.drawable.img_play1_choose_bomb_die)
                    } else {
                        imv.setImageResource(R.drawable.img_play1_choose_live)
                    }
                } else {
                    // Show hidden state
                    imv.setImageResource(R.drawable.img_unchoose_play1)
                }

                root.setOnClickListener {
                    if (!clickedPositions.contains(position)) {
                        clickedPositions.add(position)

                        // Reveal the item
                        if (item.bomb) {
                            imv.setImageResource(R.drawable.img_play1_choose_bomb_die)
                        } else {
                            imv.setImageResource(R.drawable.img_play1_choose_live)
                        }

                        onItemClick?.invoke(item, position)
                    }
                }
            }
        }
    }

    inner class Player2ViewHolder(
        private val binding: ItemBombPlay2UnchooseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ManualModel, position: Int) {
            binding.apply {
                // Set initial state (unchoose/hidden)
                if (clickedPositions.contains(position)) {
                    // Show revealed state
                    if (item.bomb) {
                        imv.setImageResource(R.drawable.img_play2_choose_bomb_die)
                    } else {
                        imv.setImageResource(R.drawable.img_play2_choose_live)
                    }
                } else {
                    // Show hidden state
                    imv.setImageResource(R.drawable.img_unchoose_play2)
                }

                root.setOnClickListener {
                    if (!clickedPositions.contains(position)) {
                        clickedPositions.add(position)

                        // Reveal the item
                        if (item.bomb) {
                            imv.setImageResource(R.drawable.img_play2_choose_bomb_die)
                        } else {
                            imv.setImageResource(R.drawable.img_play2_choose_live)
                        }

                        onItemClick?.invoke(item, position)
                    }
                }
            }
        }
    }

    fun resetClicked() {
        clickedPositions.clear()
        notifyDataSetChanged()
    }

    fun isPositionClicked(position: Int): Boolean {
        return clickedPositions.contains(position)
    }

    fun getClickedCount(): Int {
        return clickedPositions.size
    }

    private class DiffCallback : DiffUtil.ItemCallback<ManualModel>() {
        override fun areItemsTheSame(oldItem: ManualModel, newItem: ManualModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ManualModel, newItem: ManualModel): Boolean {
            return oldItem == newItem
        }
    }
}