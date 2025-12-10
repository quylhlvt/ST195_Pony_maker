package com.example.basefragment.core.dialog

import android.app.Activity
import android.content.Context
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseDialog
import com.example.basefragment.core.extention.gone
import com.example.basefragment.core.extention.hideNavigation
import com.example.basefragment.core.extention.setClick
import com.example.basefragment.core.extention.strings
import com.example.basefragment.core.extention.visible
import com.example.basefragment.databinding.CustomDialogBinding
import com.lvt.ads.util.Admob

class CustomDialog (
    val context: Context, val title: Int, val description: Int, val isError: Boolean = false, var checkAds: Boolean?=false
) : BaseDialog<CustomDialogBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.custom_dialog
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onNoClick: (() -> Unit) = {}
    var onYesClick: (() -> Unit) = {}
    var onDismissClick: (() -> Unit) = {}

    override fun initView() {
        if (checkAds == true){
            binding.flNative.visible()
//            Admob.getInstance().loadNativeAd(
//                context,
//                context.getString(R.string.native_dialog),
//                binding.flNative,
//                R.layout.ads_native_medium_btn_bottom_2
//            )
        }
        initText()

        if (isError) {
            binding.btnNo.gone()
        }
    }

    override fun initAction() {
        binding.apply {
            btnNo.setClick { onNoClick.invoke() }
            btnYes.setClick { onYesClick.invoke() }
            flOutSide.setClick { onDismissClick.invoke() }
        }
    }

    override fun onDismissListener() {

    }

    private fun initText() {
        binding.apply {
            btnNo.isSelected= true
            btnYes.isSelected=true
            tvTitle.text = context.strings(title)
            tvDescription.text = context.strings(description)
        }
    }
}