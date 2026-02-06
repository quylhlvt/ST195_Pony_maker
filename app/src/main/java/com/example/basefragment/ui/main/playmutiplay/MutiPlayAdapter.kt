package com.example.basefragment.ui.main.playmutiplay

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.R
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.ItemBombPlay1UnchooseBinding
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.basefragment.utils.music.SoundEffect.playClick

class MutiPlayAdapter(
    private val context: Context,
) : ListAdapter<ManualModel, MutiPlayAdapter.PlayerViewHolder>(DiffCallback()) {

    var onItemClick: ((ManualModel, Int) -> Unit)? = null
    private val clickedPositions = mutableSetOf<Int>()
    private var isEnabled = true
    private var isProcessing = false

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        notifyDataSetChanged()
    }

    fun setProcessing(processing: Boolean) {
        isProcessing = processing
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ItemBombPlay1UnchooseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class PlayerViewHolder(
        private val binding: ItemBombPlay1UnchooseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ManualModel, position: Int) {
            binding.apply {
                // ✅ Reset GIF layer về invisible ban đầu
                imvGif.visibility = View.INVISIBLE

                // Hiển thị trạng thái
                if (clickedPositions.contains(position)) {
                    if (item.bomb) {
                        imv.setImageResource(R.drawable.img_play1_choose_died)
                    } else {
                        imv.setImageResource(R.drawable.img_play1_choose_live)
                    }
                } else {
                    imv.setImageResource(R.drawable.img_unchoose_play1)
                }

                root.alpha = if (isEnabled) 1.0f else 0.5f

                // Click handler
                root.setOnClickListener {
                    if (!isEnabled) {
                        return@setOnClickListener
                    }
                    if (isProcessing) {
                        return@setOnClickListener
                    }
                    if (!clickedPositions.add(position)) {
                        return@setOnClickListener
                    }

                    isProcessing = true
                    flipCardRealistic(imv, imvGif, item)
                    onItemClick?.invoke(item, position)
                }
            }
        }

        private fun flipCardRealistic(imageView: ImageView, imageViewGif: ImageView, item: ManualModel) {
            val scale = imageView.context.resources.displayMetrics.density
            imageView.cameraDistance = 8000 * scale
            imageViewGif.cameraDistance = 8000 * scale
            playClick(context, R.raw.flip)
            // ✅ Đồng bộ animation cho CẢ 2 ImageView
            imageView.animate()
                .rotationY(90f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(150)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            imageViewGif.animate()
                .rotationY(90f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(150)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    if (item.bomb) {
                        playClick(context, R.raw.bomb)
                        playClick(context, R.raw.bomb)
                        // ✅ Hiển thị ảnh tĩnh bomb_die ở layer chính
                        imageView.setImageResource(R.drawable.img_play1_choose_bomb_die)

                        // ✅ Sau 250ms → Hiển thị GIF ở layer trên
                        imageViewGif.postDelayed({
                            imageViewGif.visibility = View.VISIBLE
                            Glide.with(context)
                                .asGif()
                                .load(R.raw.animation)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .into(imageViewGif)
                        }, 250)

                        // ✅ Sau 850ms (250 + 600) → Ẩn GIF và hiển thị ảnh died
                        imageView.postDelayed({
                            // Clear và ẩn GIF layer
                            Glide.with(context).clear(imageViewGif)
                            imageViewGif.visibility = View.INVISIBLE

                            // Hiển thị ảnh died ở layer chính
                            imageView.setImageResource(R.drawable.img_play1_choose_died)
                        }, 850)
                    } else {
                        imageView.setImageResource(R.drawable.img_play1_choose_live)
                    }

                    // ✅ Lật nửa sau cho CẢ 2 ImageView
                    imageView.animate()
                        .rotationY(0f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .withEndAction {
                            imageView.rotationY = 0f
                            imageView.scaleX = 1.0f
                            imageView.scaleY = 1.0f
                        }
                        .start()

                    imageViewGif.animate()
                        .rotationY(0f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .withEndAction {
                            imageViewGif.rotationY = 0f
                            imageViewGif.scaleX = 1.0f
                            imageViewGif.scaleY = 1.0f
                        }
                        .start()
                }
                .start()
        }
    }

    fun resetClicked() {
        clickedPositions.clear()
        isProcessing = false
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