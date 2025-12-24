package com.example.basefragment.dialog

import android.app.Activity
import com.example.basefragment.base.BaseDialog
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.R
import com.example.basefragment.databinding.DialogExitBinding

class DialogExit(context: Activity, var type: String) :
    BaseDialog<DialogExitBinding>(context, false) {
    var onClick: (() -> Unit)? = null
    override fun getContentView(): Int = R.layout.dialog_exit

    override fun initView() {
        when(type){
            "exit" ->{
                binding.tvTitle.text = context.getString(R.string.exit)
                binding.tvContent.text = context.getString(R.string.haven_t_saved_it_yet_do_you_want_to_exit)

            }
            "reset"->{
                binding.tvTitle.text = context.getString(R.string.reset)
                binding.tvContent.text = context.getString(R.string.do_you_want_to_reset_all)
            }
            "delete"->{
                binding.tvTitle.text = context.getString(R.string.delete)
                binding.tvContent.text = context.getString(R.string.do_you_want_to_delete_this_item)
            }
        }
    }

    override fun bindView() {
        binding.apply {
            btnYes.onSingleClick {
                onClick?.invoke()
                dismiss()
            }
            btnNo.onSingleClick {
                dismiss()
            }
        }
    }
}