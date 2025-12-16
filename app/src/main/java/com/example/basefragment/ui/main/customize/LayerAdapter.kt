package com.example.basefragment.ui.main.customize

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.core.extention.gone
import com.example.basefragment.core.extention.loadImage
import com.example.basefragment.data.model.custom.BodyPartModel
import com.example.basefragment.data.model.custom.ColorModel
import com.example.basefragment.databinding.ItemBottomCustomBinding
import com.example.basefragment.databinding.ItemColorBinding
import com.example.basefragment.databinding.ItemLayerBinding

class NavAdapter(
    private  var context: Context,
    private var bodyParts: List<BodyPartModel>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<NavAdapter.NavViewHolder>() {

    private var selectedIndex = 0

    fun setData(newBodyParts: List<BodyPartModel>) {
        bodyParts = newBodyParts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {
        val binding = ItemBottomCustomBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NavViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
        holder.bind(bodyParts[position], position == selectedIndex)
    }

    override fun getItemCount() = bodyParts.size

    inner class NavViewHolder(private val binding: ItemBottomCustomBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bodyPart: BodyPartModel, isSelected: Boolean) {
            binding.apply {
                loadImage(bodyPart.nav, imvImage, onDismissLoading = {
                    sflShimmer.stopShimmer()
                    sflShimmer.gone()
                })
            }
//            loadImage(context, bodyPart.nav, binding.imvImage ,false)
            binding.root.alpha = if (isSelected) 1f else 0.5f
            binding.root.setOnClickListener {
                val oldIndex = selectedIndex
                selectedIndex = adapterPosition
                notifyItemChanged(oldIndex)
                notifyItemChanged(selectedIndex)
                onClick(adapterPosition)
            }
        }
    }
}
class LayerAdapter(
    private var context: Context,
    private var imagePaths: List<String>,
    private val onImageSelected: (Int) -> Unit
) : RecyclerView.Adapter<LayerAdapter.LayerViewHolder>() {

    private var selectedIndex = 0

    fun setData(newPaths: List<String>) {
        Log.d("LayerAdapter", "setData: ${newPaths.size} image paths")
        imagePaths = newPaths
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayerViewHolder {
        val binding = ItemLayerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LayerViewHolder, position: Int) {
        holder.bind(imagePaths[position], position == selectedIndex)
    }

    override fun getItemCount() = imagePaths.size

    inner class LayerViewHolder(private val binding: ItemLayerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(imagePath: String, isSelected: Boolean) {
            Log.d("LayerAdapter", "Bind position=$adapterPosition: path=$imagePath")
//            binding.imvImage.loadImage(imagePath)
            // Load ảnh layer
//            loadImage(context, imagePath, binding.imvImage, false)
           binding.apply {
               loadImage(imagePath, imvImage, onDismissLoading = {
                   sflShimmer.stopShimmer()
                   sflShimmer.gone()
               })
           }
            binding.root.alpha = if (isSelected) 1f else 0.5f

            binding.root.setOnClickListener {
                val oldIndex = selectedIndex
                selectedIndex = adapterPosition
                notifyItemChanged(oldIndex)
                notifyItemChanged(selectedIndex)
                onImageSelected(adapterPosition)
            }
        }
    }
}

class ColorAdapter(
    private var colors: List<ColorModel>,
    private val onColorSelected: (Int) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var selectedIndex = 0

    fun setData(newColors: List<ColorModel>) {
        Log.d("ColorAdapter", "setData: ${newColors.size} colors")
        colors = newColors
        notifyDataSetChanged()
    }

    fun setSelectedIndex(index: Int) {
        val oldIndex = selectedIndex
        selectedIndex = index
        notifyItemChanged(oldIndex)
        notifyItemChanged(selectedIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position], position == selectedIndex)
    }

    override fun getItemCount() = colors.size

    inner class ColorViewHolder(private val binding: ItemColorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(colorModel: ColorModel, isSelected: Boolean) {
            // Hiển thị màu
            val backgroundColor = if (colorModel.color.isNotEmpty()) {
                try {
                    android.graphics.Color.parseColor("#${colorModel.color}")
                } catch (e: IllegalArgumentException) {
                    Log.w("ColorAdapter", "Invalid hex: ${colorModel.color}")
                    android.graphics.Color.LTGRAY
                }
            } else {
                android.graphics.Color.LTGRAY
            }

            binding.ivColorPreview.setBackgroundColor(backgroundColor)
            binding.viewSelected.visibility = if (isSelected) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                val oldIndex = selectedIndex
                selectedIndex = adapterPosition
                notifyItemChanged(oldIndex)
                notifyItemChanged(selectedIndex)
                onColorSelected(adapterPosition)
            }
        }
    }
}