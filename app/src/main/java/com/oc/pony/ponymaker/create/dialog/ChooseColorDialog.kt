package com.oc.pony.ponymaker.create.dialog

import android.app.Activity
import android.graphics.Color
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.DialogColorPickerBinding
import com.oc.pony.ponymaker.create.utils.onSingleClick


class ChooseColorDialog(context: Activity) : com.oc.pony.ponymaker.create.base.BaseDialog<DialogColorPickerBinding>(context, false) {
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