package com.example.basefragment.dialog

import android.app.Activity
import android.graphics.Color
import com.example.basefragment.R
import com.example.basefragment.base.BaseDialog
import com.example.basefragment.databinding.DialogColorPickerBinding
import com.example.basefragment.utils.onSingleClick


class ChooseColorDialog(context: Activity) : BaseDialog<DialogColorPickerBinding>(context, false) {
    var onDoneEvent: ((Int) -> Unit) = {}
    private var color = Color.WHITE
    override fun getContentView(): Int = R.layout.dialog_color_picker

    override fun initView() {
        binding.apply {
            colorPickerView.apply {
                hueSliderView = hueSlider
            }
        }
    }

    override fun bindView() {
        binding.apply {
            colorPickerView.setOnColorChangedListener { color = it }
            btnClose.onSingleClick { dismiss() }
            btnDone.onSingleClick {
                dismiss()
                onDoneEvent.invoke(color)
            }
        }
    }


}