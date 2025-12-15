//package com.example.basefragment.ui.main.customize
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.example.basefragment.data.model.custom.BodyPartModel
//
//class LayerAdapter(private val bodyParts: List<BodyPartModel>,
//                   private val onColorSelected: (BodyPartModel, Int) -> Unit) : RecyclerView.Adapter<LayerAdapter.ViewHolder>() {
//
//    private val selectedColorIndices = mutableMapOf<String, Int>()
//
//    init {
//        // Initialize với default colors
//        bodyParts.forEach { bodyPart ->
//            selectedColorIndices[bodyPart.] = bodyPart.defaultColorIndex
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val binding = ItemLayerBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return ViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(bodyParts[position], selectedColorIndices[bodyParts[position].id] ?: 0)
//    }
//
//    override fun getItemCount() = bodyParts.size
//
//    inner class ViewHolder(
//        private val binding: ItemLayerBinding
//    ) : RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(bodyPart: BodyPartModel, selectedColorIndex: Int) {
//            binding.apply {
//                // Set body part name
//                tvLayerName.text = bodyPart.name
//
//                // Setup colors recycler view (horizontal)
//                val colorAdapter = ColorAdapter(
//                    bodyPart.colors,
//                    selectedColorIndex
//                ) { colorIndex ->
//                    // Update selected color
//                    selectedColorIndices[bodyPart.id] = colorIndex
//                    onColorSelected(bodyPart, colorIndex)
//                    notifyItemChanged(adapterPosition)
//                }
//
//                recyclerViewColors.adapter = colorAdapter
//            }
//        }
//    }
//}
//
///**
// * Adapter cho danh sách màu của 1 body part
// */
//class ColorAdapter(
//    private val colors: List<com.example.basefragment.data.model.ColorModel>,
//    private var selectedIndex: Int,
//    private val onColorClick: (Int) -> Unit
//) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
//        val binding = com.example.basefragment.databinding.ItemColorBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return ColorViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
//        holder.bind(colors[position], position == selectedIndex, position)
//    }
//
//    override fun getItemCount() = colors.size
//
//    inner class ColorViewHolder(
//        private val binding: com.example.basefragment.databinding.ItemColorBinding
//    ) : RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(
//            color: com.example.basefragment.data.model.ColorModel,
//            isSelected: Boolean,
//            position: Int
//        ) {
//            binding.apply {
//                // Load color preview
//                Glide.with(itemView.context)
//                    .load(color.imageUrl)
//                    .into(ivColorPreview)
//
//                // Show selection indicator
//                if (isSelected) {
//                    viewSelected.visibility = android.view.View.VISIBLE
//                } else {
//                    viewSelected.visibility = android.view.View.GONE
//                }
//
//                // Click listener
//                root.setOnClickListener {
//                    val oldSelected = selectedIndex
//                    selectedIndex = position
//                    notifyItemChanged(oldSelected)
//                    notifyItemChanged(selectedIndex)
//                    onColorClick(position)
//                }
//            }
//        }
//    }
//}