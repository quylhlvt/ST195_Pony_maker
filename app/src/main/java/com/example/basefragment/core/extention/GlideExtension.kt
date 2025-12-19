package com.example.basefragment.core.extention

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import coil.load
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.example.basefragment.R
import com.example.basefragment.utils.DataLocal
import com.facebook.shimmer.ShimmerDrawable
import java.io.File

fun ImageView.loadImage(imgResource: Any? = null) {
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(DataLocal.shimmer)
    }
    this.load(imgResource) {
        placeholder(shimmerDrawable)
        error(shimmerDrawable)
    }
}
fun loadImage(context: Context, path: String, imageView: ImageView, isLoadShimmer: Boolean = true) {
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(DataLocal.shimmer)
    }
    if (isLoadShimmer){
        Glide.with(context).load(path).placeholder(shimmerDrawable).error(shimmerDrawable).into(imageView)
    }else{
        Glide.with(context).load(path).placeholder(shimmerDrawable).error(shimmerDrawable).into(imageView)
    }

}

fun loadImage(viewGroup: ViewGroup, path: String, imageView: ImageView, isLoadShimmer: Boolean = true) {
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(DataLocal.shimmer)
    }
    if (isLoadShimmer){
        Glide.with(viewGroup).load(path).placeholder(shimmerDrawable).error(shimmerDrawable).into(imageView)
    }else{
        Glide.with(viewGroup).load(path).placeholder(shimmerDrawable).error(shimmerDrawable).into(imageView)
    }
}

fun loadImage(viewGroup: ViewGroup, path: Int, imageView: ImageView, isLoadShimmer: Boolean = true) {
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(DataLocal.shimmer)
    }
    if (isLoadShimmer){
        Glide.with(viewGroup).load(path).placeholder(shimmerDrawable).error(shimmerDrawable).into(imageView)
    }else{
        Glide.with(viewGroup).load(path).into(imageView)
    }
}

fun loadImage(path: Any, imageView: ImageView, onShowLoading: (() -> Unit)? = null, onDismissLoading: (() -> Unit)? = null){
    onShowLoading?.invoke()
    Glide.with(imageView.context).load(path).listener(object : RequestListener<Drawable>{
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
            onDismissLoading?.invoke()
            return false
        }

        override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable?>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
            onDismissLoading?.invoke()
            return false
        }
    }).into(imageView)
}

@SuppressLint("CheckResult")
fun ImageView.loadImageFromFile(path: String) {
    val file = File(path)
    val request = Glide.with(context)
        .load(file)

    request.signature(ObjectKey(file.lastModified()))

    request.into(this)
}

fun loadThumbnail(view: ImageView, url: String){
    val file = File(url)

    Glide.with(view.context)
        .asBitmap()
        .load(file)
        .frame(1000000)
        .into(view)
}
fun ImageView.loadFromAsset(assetPath: String) {  // assetPath = "bg/1.png"
    Glide.with(this)
        .load(Uri.parse("$assetPath"))
        .placeholder(ShimmerDrawable().apply { setShimmer(DataLocal.shimmer) })
        .error(R.drawable.logo_app)  // Để debug nếu path sai
        .into(this)
}