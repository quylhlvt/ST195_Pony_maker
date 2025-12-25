package com.oc.pony.ponymaker.create.dialog

import android.app.Activity
import android.widget.Toast
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.DialogCreateNameBinding
import com.oc.pony.ponymaker.create.utils.onClick


class CreateNameDialog(val context: Activity) :
    com.oc.pony.ponymaker.create.base.BaseDialog1<DialogCreateNameBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_create_name
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onNoClick: (() -> Unit) = {}
    var onYesClick: ((String) -> Unit) = {}
    var onDismissClick: (() -> Unit) = {}

    override fun initView() {
    }

    override fun initAction() {
        binding.apply {
            tvNo.onClick {
                onNoClick.invoke()
            }
            tvYes.onClick {
                val input = edtName.text.toString().trim()

                when {
                    input == "" -> {
                        Toast.makeText(context, context.getString(R.string.please_enter_your_package_name), Toast.LENGTH_SHORT).show()
                    }


                    else -> {
                        onYesClick.invoke(input)
                    }
                }
            }
            flOutSide.onClick {
                onDismissClick.invoke()
            }
        }
    }

    override fun onDismissListener() {

    }
}