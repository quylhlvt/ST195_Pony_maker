package com.example.basefragment.utils


import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import androidx.core.graphics.toColorInt
import com.example.basefragment.data.model.LanguageModel
import com.example.basefragment.R

@BindingAdapter("setBGCV")
fun ConstraintLayout.setBGCV(check: LanguageModel) {
    if (check.active) {
        this.setBackgroundResource(R.drawable.bg_card_border_16)
    } else {
        this.setBackgroundResource(R.drawable.bg_card_border_100_false)
    }
}

@BindingAdapter("setSrcCheckLanguage")
fun AppCompatImageView.setSrcCheckLanguage(check: Boolean) {
    if (check) {
        this.setImageResource(R.drawable.ic_check_language_true)
    } else {
        this.setImageResource(R.drawable.ic_check_language_false)
    }
}
@BindingAdapter("setTextColor")
fun TextView.setTextColor(check: Boolean) {
    if (check) {
        this.setTextColor("#ffffff".toColorInt())
    } else {
        this.setTextColor("#1f2f4f".toColorInt())
    }
}
@BindingAdapter("setBG")
fun AppCompatImageView.setBG(id: Int) {
    Glide.with(this).load(id).into(this)
}
@BindingAdapter("setImg")
fun AppCompatImageView.setImg(data : Int){
    Glide.with(this).load(data).into(this)
}