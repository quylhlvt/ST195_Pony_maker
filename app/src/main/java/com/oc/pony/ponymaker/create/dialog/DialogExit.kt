package com.oc.pony.ponymaker.create.dialog

import android.app.Activity
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.DialogExitBinding
import com.oc.pony.ponymaker.create.utils.onSingleClick

class DialogExit(context: Activity, var type: String) :
    com.oc.pony.ponymaker.create.base.BaseDialog<DialogExitBinding>(context, false) {
    var onClick: (() -> Unit)? = null
    override fun getContentView(): Int = R.layout.dialog_exit

    override fun initView() {
        when(type){
            "exit" ->{
                binding.txtTitle.text = context.getString(R.string.exit)
                binding.txtContent.text = context.getString(R.string.haven_t_saved_it_yet_do_you_want_to_exit)
//                binding.nativeAds.show()
//                Admob.getInstance().loadNativeAd(
//                    context,
//                    context.getString(R.string.native_dialog),
//                    binding.nativeAds,
//                    com.lvt.ads.R.layout.ads_native_avg2
//                )
            }
            "reset"->{
                binding.txtTitle.text = context.getString(R.string.reset)
                binding.txtContent.text = context.getString(R.string.do_you_want_to_reset_all)
            }
            "delete"->{
                binding.txtTitle.text = context.getString(R.string.delete)
                binding.txtContent.text = context.getString(R.string.do_you_want_to_delete_this_item)
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