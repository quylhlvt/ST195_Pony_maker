package com.example.basefragment.ui.main.customize

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
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
// NavAdapter.kt
// NavAdapter.kt
class NavAdapter(
    private var bodyParts: List<BodyPartModel>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<NavAdapter.NavViewHolder>() {

    private var selectedIndex = 0

    fun setData(newBodyParts: List<BodyPartModel>) {
        bodyParts = newBodyParts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {
        val binding = ItemBottomCustomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NavViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
        holder.bind(bodyParts[position], position == selectedIndex)
    }

    override fun getItemCount() = bodyParts.size

    inner class NavViewHolder(private val binding: ItemBottomCustomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bodyPart: BodyPartModel, isSelected: Boolean) {
            binding.vFocus.visibility = if (isSelected) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.apply {
                loadImage(bodyPart.nav, imvImage, onDismissLoading = {
                    sflShimmer.stopShimmer()
                    sflShimmer.gone()
                })
                root.onClick(200) {
                    val old = selectedIndex
                    selectedIndex = adapterPosition
                    notifyItemChanged(old)
                    notifyItemChanged(selectedIndex)
                    onClick(adapterPosition)
                }
            }
        }
    }
    fun setSelectedIndex(index: Int) {
        val oldIndex = selectedIndex
        selectedIndex = index.coerceIn(0, bodyParts.size - 1)
        notifyItemChanged(oldIndex)
        notifyItemChanged(selectedIndex)
    }
}

// LayerAdapter.kt (VariantAdapter)
class LayerAdapter(
    private var imagePaths: List<String>,
    private val onImageSelected: (Int) -> Unit,
    private val onScrollToPosition: (Int) -> Unit
) : RecyclerView.Adapter<LayerAdapter.LayerViewHolder>() {

    var selectedIndex = -1

    fun setDataWithSelection(newPaths: List<String>, newSelectedIndex: Int) {
        imagePaths = newPaths
        // 🔥 KHÔNG coerceIn nữa, giữ nguyên -1 nếu không có selection
        selectedIndex = if (newPaths.isEmpty()) {
            -1
        } else if (newSelectedIndex >= 0 && newSelectedIndex < newPaths.size) {
            newSelectedIndex
        } else {
            -1
        }
        notifyDataSetChanged()
        if (selectedIndex >= 0) {
            onScrollToPosition(selectedIndex)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayerViewHolder {
        val binding = ItemLayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LayerViewHolder, position: Int) {
        holder.bind(imagePaths[position], position == selectedIndex && selectedIndex >= 0)
    }

    override fun getItemCount() = imagePaths.size

    inner class LayerViewHolder(private val binding: ItemLayerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(imagePath: String, isSelected: Boolean) {
            binding.apply {
                // 🔥 Reset shimmer mỗi lần bind
                sflShimmer.visibility = View.VISIBLE
                sflShimmer.startShimmer()

                when {
                    imagePath == "none" -> {
                        // 🔥 Hiển thị icon "none"
                        imvImage.setImageResource(R.drawable.ic_none)
                        sflShimmer.stopShimmer()
                        sflShimmer.gone()
                    }
                    imagePath == "dice" -> {
                        // 🔥 Hiển thị icon "dice" (random)
                        imvImage.setImageResource(R.drawable.ic_random_layer)
                        sflShimmer.stopShimmer()
                        sflShimmer.gone()
                    }
                    imagePath.isBlank() -> {
                        // 🔥 Blank path → cũng hiển thị none
                        imvImage.setImageResource(R.drawable.ic_none)
                        sflShimmer.stopShimmer()
                        sflShimmer.gone()
                    }
                    else -> {
                        // 🔥 Load ảnh bình thường
                        loadImage(imagePath, imvImage, onDismissLoading = {
                            sflShimmer.stopShimmer()
                            sflShimmer.gone()
                        })
                    }
                }

                cardLayer.apply {
                    strokeWidth = 3.dp(context)
                    strokeColor = ContextCompat.getColor(
                        context,
                        if (isSelected) R.color.white else R.color.stroke_layercustom_select
                    )
                }

                root.onClick(200) {
                    if (adapterPosition == RecyclerView.NO_POSITION) return@onClick

                    val oldPosition = selectedIndex
                    selectedIndex = adapterPosition

                    // 🔥 Chỉ notify nếu oldPosition hợp lệ
                    if (oldPosition >= 0 && oldPosition < imagePaths.size) {
                        notifyItemChanged(oldPosition)
                    }
                    notifyItemChanged(selectedIndex)

                    onImageSelected(adapterPosition)
                }
            }
        }
    }
}
// ColorAdapter.kt
class ColorAdapter(
    private var colors: List<ColorModel>,
    private val onColorSelected: (Int) -> Unit,
    private val onScrollToPosition: (Int) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var selectedIndex = -1

    fun setData(newColors: List<ColorModel>) {
        colors = newColors
        notifyDataSetChanged()
    }
    fun setSelectedIndex(index: Int) {
        val old = selectedIndex
        // ✨ FIX: Nếu index = -1, chuyển thành 0 (default first item)
        selectedIndex = if (index == -1 && colors.isNotEmpty()) 0 else index

        // ✨ FIX: Chỉ notify và scroll khi có thay đổi thực sự
        if (old != selectedIndex) {
            // Chỉ notify old nếu nó hợp lệ
            if (old >= 0 && old < colors.size) {
                notifyItemChanged(old)
            }
            // Notify và scroll item mới
            if (selectedIndex >= 0 && selectedIndex < colors.size) {
                notifyItemChanged(selectedIndex)
                onScrollToPosition(selectedIndex)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position], position == selectedIndex)
    }

    override fun getItemCount() = colors.size

    inner class ColorViewHolder(private val binding: ItemColorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(colorModel: ColorModel, isSelected: Boolean) {
            val backgroundColor = if (colorModel.color.isNotEmpty()) {
                try {
                    android.graphics.Color.parseColor("#${colorModel.color}")
                } catch (e: Exception) {
                    android.graphics.Color.LTGRAY
                }
            } else android.graphics.Color.LTGRAY

            binding.ivColorPreview.backgroundTintList = ColorStateList.valueOf(backgroundColor)
            // 🔥 Chỉ hiện selected khi adapterPosition == selectedIndex VÀ selectedIndex >= 0
            binding.viewSelected.visibility = if (isSelected && selectedIndex >= 0) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.root.onClick(200) {
                val old = selectedIndex
                selectedIndex = adapterPosition
                notifyItemChanged(old)
                notifyItemChanged(selectedIndex)
                onColorSelected(adapterPosition)
            }
        }}
}