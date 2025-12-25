package com.oc.pony.ponymaker.create.ui.quick_mix

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ItemMixBinding
import com.oc.pony.ponymaker.create.utils.hide
import com.oc.pony.ponymaker.create.utils.onSingleClick
import com.oc.pony.ponymaker.create.utils.show
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuickAdapter : com.oc.pony.ponymaker.create.base.AbsBaseAdapter<com.oc.pony.ponymaker.create.data.model.CustomModel, ItemMixBinding>(
    R.layout.item_mix, DiffCallBack()
) {
    var arrListImageSortView = arrayListOf<ArrayList<String>>()
    val arrBitmap = hashMapOf<Int, Bitmap>()
    var onCLick: ((Int) -> Unit)? = null
    var listArrayInt = arrayListOf<ArrayList<ArrayList<Int>>>()
    override fun bind(
        binding: ItemMixBinding,
        position: Int,
        data: com.oc.pony.ponymaker.create.data.model.CustomModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.shimmer.startShimmer()
        binding.shimmer.show()
        if (!arrBitmap.containsKey(position)){
            binding.shimmer.onSingleClick {
                _root_ide_package_.com.oc.pony.ponymaker.create.utils.showToast(
                    binding.root.context,
                    R.string.wait_a_few_second
                )
            }
            val coordSet = listArrayInt[position]
            mergeImages(binding.root.context, "", data, arrListImageSortView[position % _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.arrBlackCentered.size], coordSet) { mergedBitmap ->
                binding.shimmer.stopShimmer()
                binding.shimmer.hide()
                binding.imvImage.setImageBitmap(mergedBitmap)
                binding.root.onSingleClick { onCLick?.invoke(position) }
                arrBitmap.put(position,mergedBitmap)
            }
        }else{
            binding.shimmer.stopShimmer()
            binding.shimmer.hide()
            binding.imvImage.setImageBitmap(arrBitmap[position])
        }

        binding.imvImage.onSingleClick {
            onCLick?.invoke(position)
        }
    }

    class DiffCallBack : com.oc.pony.ponymaker.create.base.AbsBaseDiffCallBack<com.oc.pony.ponymaker.create.data.model.CustomModel>() {
        override fun itemsTheSame(
            oldItem: com.oc.pony.ponymaker.create.data.model.CustomModel, newItem: com.oc.pony.ponymaker.create.data.model.CustomModel
        ): Boolean {
            return oldItem.avt == newItem.avt
        }

        override fun contentsTheSame(
            oldItem: com.oc.pony.ponymaker.create.data.model.CustomModel, newItem: com.oc.pony.ponymaker.create.data.model.CustomModel
        ): Boolean {
            return oldItem.avt != newItem.avt
        }
    }
    private fun mergeImages(
        context: Context,
        bgRes: String,
        blackCentered: com.oc.pony.ponymaker.create.data.model.CustomModel,
        listImageSortView: List<String>,
        coordSet: ArrayList<ArrayList<Int>>,
        onDone: (Bitmap) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1️⃣ Load ảnh nền
//                val bgBitmap = Glide.with(context)
//                    .asBitmap()
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .load(bgRes)
//                    .submit()
//                    .get()

                // 2️⃣ Tạo bitmap gộp mới
                val merged = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(merged)
//                canvas.drawBitmap(bgBitmap, 0f, 0f, null)

                // 3️⃣ Duyệt từng layer
                listImageSortView.forEachIndexed { index, icon ->
                    val coord = coordSet[index]
                    if (coord[0] > 0) {
                        val targetPath = blackCentered.bodyPart
                            .find { it.icon == icon }
                            ?.listPath?.getOrNull(coord[1])
                            ?.listPath?.getOrNull(coord[0])

                        if (!targetPath.isNullOrEmpty()) {
                            val layerBitmap = Glide.with(context)
                                .asBitmap()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .load(targetPath)
                                .submit()
                                .get()
                            // vẽ đè layer
                            canvas.drawBitmap(layerBitmap, 0f, 0f, null)
//                            layerBitmap.recycle()
                        }
                    }
                }

//                bgBitmap.recycle()

                withContext(Dispatchers.Main) {
                    onDone(merged)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}