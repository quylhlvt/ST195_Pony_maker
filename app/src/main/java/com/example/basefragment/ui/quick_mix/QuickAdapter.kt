package com.example.basefragment.ui.quick_mix

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import com.example.basefragment.base.AbsBaseAdapter
import com.example.basefragment.base.AbsBaseDiffCallBack
import com.example.basefragment.data.model.CustomModel
import com.example.basefragment.utils.DataHelper
import com.example.basefragment.utils.hide
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.utils.show
import com.example.basefragment.utils.showToast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.basefragment.R
import com.example.basefragment.databinding.ItemMixBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuickAdapter : AbsBaseAdapter<CustomModel, ItemMixBinding>(
    R.layout.item_mix, DiffCallBack()
) {
    var arrListImageSortView = arrayListOf<ArrayList<String>>()
    val arrBitmap = hashMapOf<Int, Bitmap>()
    var onCLick: ((Int) -> Unit)? = null
    var listArrayInt = arrayListOf<ArrayList<ArrayList<Int>>>()
    override fun bind(
        binding: ItemMixBinding,
        position: Int,
        data: CustomModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.shimmer.startShimmer()
        binding.shimmer.show()
        if (!arrBitmap.containsKey(position)){
            binding.shimmer.onSingleClick {
                showToast(binding.root.context, R.string.wait_a_few_second)
            }
            val coordSet = listArrayInt[position]
            mergeImages(binding.root.context, "", data, arrListImageSortView[position % DataHelper.arrBlackCentered.size], coordSet) { mergedBitmap ->
                binding.shimmer.stopShimmer()
                binding.shimmer.hide()
                binding.imv.setImageBitmap(mergedBitmap)
                binding.root.onSingleClick { onCLick?.invoke(position) }
                arrBitmap.put(position,mergedBitmap)
            }
        }else{
            binding.shimmer.stopShimmer()
            binding.shimmer.hide()
            binding.imv.setImageBitmap(arrBitmap[position])
        }

        binding.imv.onSingleClick {
            onCLick?.invoke(position)
        }
    }

    class DiffCallBack : AbsBaseDiffCallBack<CustomModel>() {
        override fun itemsTheSame(
            oldItem: CustomModel, newItem: CustomModel
        ): Boolean {
            return oldItem.avt == newItem.avt
        }

        override fun contentsTheSame(
            oldItem: CustomModel, newItem: CustomModel
        ): Boolean {
            return oldItem.avt != newItem.avt
        }
    }
    private fun mergeImages(
        context: Context,
        bgRes: String,
        blackCentered: CustomModel,
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