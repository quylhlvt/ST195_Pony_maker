package com.example.basefragment.ui.main.play

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.basefragment.R
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.ItemBombPlay1UnchooseBinding
import com.example.basefragment.databinding.ItemBombPlay2UnchooseBinding
import kotlinx.coroutines.Dispatchers

class PlayAdapter(
    private val context: Context,
    private val isPlayer1: Boolean
) : ListAdapter<ManualModel, RecyclerView.ViewHolder>(DiffCallback()) {

    var onItemClick: ((ManualModel, Int) -> Unit)? = null
    private val clickedPositions = mutableSetOf<Int>()
    private var isEnabled = true
    private var isAnimating = false

    // Thời gian delay sau khi lật xong (ms)
    private val POST_FLIP_DELAY = 400L
    private val BOMB_IMAGE_CHANGE_DELAY = 400L
    init {
        Glide.with(context)
            .asGif()
            .placeholder(R.drawable.img_play1_choose_bomb_die)
            .load(R.raw.animation)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
    }
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        notifyDataSetChanged()
    }

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

                root.setOnClickListener {
                    if (!isEnabled || isAnimating) {
                        return@setOnClickListener
                    }

                    if (!clickedPositions.contains(position)) {
                        isAnimating = true
                        clickedPositions.add(position)
                        flipCardRealistic(imv, item)
                        onItemClick?.invoke(item, position)
                    }
                }
            }
        }

        private fun flipCardRealistic(imageView: android.widget.ImageView, item: ManualModel) {
            val scale = imageView.context.resources.displayMetrics.density
            imageView.cameraDistance = 8000 * scale

            // Lật nửa đầu: 0° -> 90°
            imageView.animate()
                .rotationY(90f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(250)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    // Đổi ảnh khi bài ở góc 90°
                    if (item.bomb) {
                        imageView.setImageResource(R.drawable.img_play1_choose_bomb_die)
                        // Delay rồi đổi sang died
                        imageView.postDelayed({
                            Glide.with(context)
                                .asGif()
                                .placeholder(R.drawable.img_play1_choose_bomb_die)
                                .load(R.raw.animation)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .into(imageView)
                        }, 100)
                        imageView.postDelayed({
                            imageView.setImageResource(R.drawable.img_play1_choose_died)
                        }, BOMB_IMAGE_CHANGE_DELAY)
                    } else {
                        imageView.setImageResource(R.drawable.img_play1_choose_live)
                    }

                    // Lật nửa sau: 90° -> 0°
                    imageView.animate()
                        .rotationY(0f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(250)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .withEndAction {
                            imageView.rotationY = 0f
                            imageView.scaleX = 1.0f
                            imageView.scaleY = 1.0f

                            // ✅ Delay sau khi lật xong mới cho phép click tiếp
                            imageView.postDelayed({
                                isAnimating = false
                            }, POST_FLIP_DELAY)
                        }
                        .start()
                }
                .start()
        }
    }

    inner class Player2ViewHolder(
        private val binding: ItemBombPlay2UnchooseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ManualModel, position: Int) {
            binding.apply {
                if (clickedPositions.contains(position)) {
                    if (item.bomb) {
                        imv.setImageResource(R.drawable.img_play2_choose_died)
                    } else {
                        imv.setImageResource(R.drawable.img_play2_choose_live)
                    }
                } else {
                    imv.setImageResource(R.drawable.img_unchoose_play2)
                }

                root.alpha = if (isEnabled) 1.0f else 0.5f

                root.setOnClickListener {
                    if (!isEnabled || isAnimating) {
                        return@setOnClickListener
                    }

                    if (!clickedPositions.contains(position)) {
                        isAnimating = true
                        clickedPositions.add(position)
                        flipCardRealistic(imv, item)
                        onItemClick?.invoke(item, position)
                    }
                }
            }
        }

        private fun flipCardRealistic(imageView: android.widget.ImageView, item: ManualModel) {
            val scale = imageView.context.resources.displayMetrics.density
            imageView.cameraDistance = 8000 * scale

            // Lật nửa đầu: 0° -> 90°
            imageView.animate()
                .rotationY(90f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(250)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    // Đổi ảnh khi bài ở góc 90°
                    if (item.bomb) {
                        imageView.setImageResource(R.drawable.img_play2_choose_bomb_die)
                        imageView.postDelayed({
                            Glide.with(context)
                                .asGif()
                                .placeholder(R.drawable.img_play2_choose_bomb_die)
                                .load(R.raw.animation)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .into(imageView)
                        }, 100)
                        // Delay rồi đổi sang died
                        imageView.postDelayed({
                            imageView.setImageResource(R.drawable.img_play2_choose_died)
                        }, BOMB_IMAGE_CHANGE_DELAY)
                    } else {
                        imageView.setImageResource(R.drawable.img_play2_choose_live)
                    }

                    // Lật nửa sau: 90° -> 0°
                    imageView.animate()
                        .rotationY(0f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(250)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .withEndAction {
                            imageView.rotationY = 0f
                            imageView.scaleX = 1.0f
                            imageView.scaleY = 1.0f

                            // ✅ Delay sau khi lật xong mới cho phép click tiếp
                            imageView.postDelayed({
                                isAnimating = false
                            }, POST_FLIP_DELAY)
                        }
                        .start()
                }
                .start()
        }
    }

    fun resetClicked() {
        clickedPositions.clear()
        isAnimating = false
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