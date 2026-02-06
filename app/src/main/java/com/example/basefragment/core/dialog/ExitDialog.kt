package com.example.basefragment.core.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseDialog
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.databinding.DialogbaseBinding

class ExitDialog : BaseDialog<DialogbaseBinding>() {

    var onExitClick: (() -> Unit)? = null
    var onCancelClick: (() -> Unit)? = null


    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogbaseBinding {
        return DialogbaseBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        binding.apply {
            txtTitle.text = getString(R.string.exit)
            txtDesception.text = getString(R.string.do_you_want_to_exit)
            txtTitle.isSelected = true  // giữ hiệu ứng marquee nếu cần
        }
    }

    override fun initAction() {
        binding.apply {
            btnExit.onClick(requireContext()) {
                onExitClick?.invoke()
                dismiss()
            }

            btnCancel.onClick(requireContext()) {
                onCancelClick?.invoke()
                dismiss()
            }
        }
    }

}