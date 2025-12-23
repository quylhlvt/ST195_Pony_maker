package com.example.basefragment.ui.main.customize

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.R
import com.example.basefragment.core.extention.dp
import com.example.basefragment.core.extention.gone
import com.example.basefragment.core.extention.loadImage
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.data.model.custom.BodyPartModel
import com.example.basefragment.data.model.custom.ColorModel
import com.example.basefragment.databinding.ItemBottomCustomBinding
import com.example.basefragment.databinding.ItemColorBinding
import com.example.basefragment.databinding.ItemLayerBinding

// ==================== NavAdapter ====================
class NavAdapter : RecyclerView.Adapter<NavAdapter.NavViewHolder>() {

    private var bodyParts = listOf<BodyPartModel>()
    var posNav = 0
    var onClick: ((Int) -> Unit)? = null

    fun submitList(newBodyParts: List<BodyPartModel>) {
        bodyParts = newBodyParts
        notifyDataSetChanged()
    }

    fun setPos(index: Int) {
        if (index < 0 || index >= bodyParts.size) return
        val old = posNav
        posNav = index
        notifyItemChanged(old)
        notifyItemChanged(posNav)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {
        val binding = ItemBottomCustomBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NavViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
        holder.bind(bodyParts[position], position == posNav)
    }

    override fun getItemCount() = bodyParts.size

    inner class NavViewHolder(private val binding: ItemBottomCustomBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bodyPart: BodyPartModel, isSelected: Boolean) {
            binding.apply {
                vFocus.visibility = if (isSelected) View.VISIBLE else View.GONE
                sflShimmer.visibility = View.VISIBLE
                sflShimmer.startShimmer()
                loadImage(bodyPart.nav, imvImage, onDismissLoading = {
                    sflShimmer.stopShimmer()
                    sflShimmer.gone()
                })
                root.onClick(200) {
                    val position = adapterPosition
                    if (position == RecyclerView.NO_POSITION) return@onClick
                    onClick?.invoke(position)
                }
            }
        }
    }
}

// ==================== LayerAdapter ====================
class LayerAdapter : RecyclerView.Adapter<LayerAdapter.LayerViewHolder>() {

    private var imagePaths = listOf<String>()
    var posPath = -1
    var onClick: ((Int, String) -> Unit)? = null

    fun submitList(newPaths: List<String>, callback: (() -> Unit)? = null) {
        imagePaths = newPaths
        notifyDataSetChanged()
        callback?.invoke()
    }

    fun setPos(index: Int) {
        val oldIndex = posPath
        posPath = index

        // ✅ Chỉ notify item hợp lệ
        if (oldIndex >= 0 && oldIndex < imagePaths.size) {
            notifyItemChanged(oldIndex)
        }
        if (posPath >= 0 && posPath < imagePaths.size) {
            notifyItemChanged(posPath)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayerViewHolder {
        val binding = ItemLayerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LayerViewHolder, position: Int) {
        holder.bind(imagePaths[position], position == posPath && posPath >= 0)
    }

    override fun getItemCount() = imagePaths.size

    inner class LayerViewHolder(private val binding: ItemLayerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(imagePath: String, isSelected: Boolean) {
            binding.apply {
                // ✅ Reset shimmer
                sflShimmer.visibility = View.VISIBLE
                sflShimmer.startShimmer()

                // ✅ Load image based on type
                when {
                    imagePath == "none" -> {
                        imvImage.setImageResource(R.drawable.ic_none)
                        sflShimmer.stopShimmer()
                        sflShimmer.gone()
                    }
                    imagePath == "dice" -> {
                        imvImage.setImageResource(R.drawable.ic_random_layer)
                        sflShimmer.stopShimmer()
                        sflShimmer.gone()
                    }
                    imagePath.isBlank() -> {
                        imvImage.setImageResource(R.drawable.ic_none)
                        sflShimmer.stopShimmer()
                        sflShimmer.gone()
                    }
                    else -> {
                        loadImage(imagePath, imvImage, onDismissLoading = {
                            sflShimmer.stopShimmer()
                            sflShimmer.gone()
                        })
                    }
                }

                // ✅ Update stroke color based on selection
                cardLayer.apply {
                    strokeWidth = 3.dp(context)
                    strokeColor = ContextCompat.getColor(
                        context,
                        if (isSelected) R.color.white else R.color.stroke_layercustom_select
                    )
                }

                // ✅ Click listener
                root.onClick(200) {
                    val position = adapterPosition
                    if (position == RecyclerView.NO_POSITION) return@onClick

                    val oldPosition = posPath
                    posPath = position

                    // Chỉ notify item hợp lệ
                    if (oldPosition >= 0 && oldPosition < imagePaths.size) {
                        notifyItemChanged(oldPosition)
                    }
                    notifyItemChanged(posPath)

                    onClick?.invoke(position, imagePath)
                }
            }
        }
    }
}

// ==================== ColorAdapter ====================
class ColorAdapter : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var colors = listOf<ColorModel>()
    var posColor = -1
    var onClick: ((Int) -> Unit)? = null

    fun submitList(newColors: List<ColorModel>) {
        colors = newColors
        notifyDataSetChanged()
    }

    fun setPos(index: Int) {
        val oldIndex = posColor
        // ✅ Nếu index = -1 và có data, chọn item đầu tiên
        posColor = if (index == -1 && colors.isNotEmpty()) 0 else index

        // ✅ Chỉ notify khi có thay đổi
        if (oldIndex != posColor) {
            if (oldIndex >= 0 && oldIndex < colors.size) {
                notifyItemChanged(oldIndex)
            }
            if (posColor >= 0 && posColor < colors.size) {
                notifyItemChanged(posColor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position], position == posColor && posColor >= 0)
    }

    override fun getItemCount() = colors.size

    inner class ColorViewHolder(private val binding: ItemColorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(colorModel: ColorModel, isSelected: Boolean) {
            binding.apply {
                // ✅ Parse and set color
                val backgroundColor = try {
                    if (colorModel.color.isNotEmpty()) {
                        android.graphics.Color.parseColor("#${colorModel.color}")
                    } else {
                        android.graphics.Color.LTGRAY
                    }
                } catch (e: Exception) {
                    android.graphics.Color.LTGRAY
                }

                ivColorPreview.backgroundTintList = ColorStateList.valueOf(backgroundColor)

                // ✅ Show/Hide selection indicator
                viewSelected.visibility = if (isSelected) View.VISIBLE else View.GONE

                // ✅ Click listener
                root.onClick(200) {
                    val position = adapterPosition
                    if (position == RecyclerView.NO_POSITION) return@onClick

                    val old = posColor
                    posColor = position
                    notifyItemChanged(old)
                    notifyItemChanged(posColor)
                    onClick?.invoke(position)
                }
            }
        }
    }
}