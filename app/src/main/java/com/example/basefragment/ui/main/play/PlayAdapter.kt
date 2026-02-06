package com.example.basefragment.ui.main.play

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.R
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.ItemBombPlay1UnchooseBinding
import com.example.basefragment.databinding.ItemBombPlay2UnchooseBinding
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.basefragment.utils.music.SoundEffect.playClick

class PlayAdapter(
    private val context: Context,
    private val isPlayer1: Boolean
) : ListAdapter<ManualModel, RecyclerView.ViewHolder>(DiffCallback()) {

    var onItemClick: ((ManualModel, Int) -> Unit)? = null
    private val clickedPositions = mutableSetOf<Int>()
    private var isEnabled = true
    private var isProcessing = false // ✅ Đổi tên từ isAnimating → isProcessing

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        notifyDataSetChanged()
    }

    fun setProcessing(processing: Boolean) {
        isProcessing = processing
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
                // ✅ Reset GIF layer
                imvGif.visibility = View.INVISIBLE

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
                    // ✅ CHẶN 1: Adapter disabled
                    if (!isEnabled) {
                        return@setOnClickListener
                    }

                    // ✅ CHẶN 2: Đang xử lý click khác
                    if (isProcessing) {
                        return@setOnClickListener
                    }

                    // ✅ CHẶN 3: Position đã click (atomic check + add)
                    if (!clickedPositions.add(position)) {
                        return@setOnClickListener
                    }

                    isProcessing = true
                    playClick(context, R.raw.flip)
                    flipCardRealistic(imv, imvGif, item)
                    onItemClick?.invoke(item, position)
                }
            }
        }

        private fun flipCardRealistic(imageView: ImageView, imageViewGif: ImageView, item: ManualModel) {
            val scale = imageView.context.resources.displayMetrics.density
            imageView.cameraDistance = 8000 * scale
            imageViewGif.cameraDistance = 8000 * scale

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
                        imageView.setImageResource(R.drawable.img_play1_choose_bomb_die)

                        imageViewGif.postDelayed({
                            imageViewGif.visibility = View.VISIBLE
                            Glide.with(context)
                                .asGif()
                                .load(R.raw.animation)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .into(imageViewGif)
                        }, 250)

                        imageView.postDelayed({
                            Glide.with(context).clear(imageViewGif)
                            imageViewGif.visibility = View.INVISIBLE
                            imageView.setImageResource(R.drawable.img_play1_choose_died)
                        }, 850)
                    } else {
                        imageView.setImageResource(R.drawable.img_play1_choose_live)
                    }

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

    inner class Player2ViewHolder(
        private val binding: ItemBombPlay2UnchooseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ManualModel, position: Int) {
            binding.apply {
                // ✅ Reset GIF layer
                imvGif.visibility = View.INVISIBLE

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
                    // ✅ CHẶN 1: Adapter disabled
                    if (!isEnabled) {
                        return@setOnClickListener
                    }

                    // ✅ CHẶN 2: Đang xử lý click khác
                    if (isProcessing) {
                        return@setOnClickListener
                    }

                    // ✅ CHẶN 3: Position đã click (atomic check + add)
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
                        imageView.setImageResource(R.drawable.img_play2_choose_bomb_die)

                        imageViewGif.postDelayed({
                            imageViewGif.visibility = View.VISIBLE
                            Glide.with(context)
                                .asGif()
                                .load(R.raw.animation)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .into(imageViewGif)
                        }, 250)

                        imageView.postDelayed({
                            Glide.with(context).clear(imageViewGif)
                            imageViewGif.visibility = View.INVISIBLE
                            imageView.setImageResource(R.drawable.img_play2_choose_died)
                        }, 850)
                    } else {
                        imageView.setImageResource(R.drawable.img_play2_choose_live)
                    }

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