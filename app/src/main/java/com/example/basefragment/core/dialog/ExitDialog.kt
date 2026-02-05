package com.example.basefragment.core.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.basefragment.R
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.databinding.DialogbaseBinding

class ExitDialog : DialogFragment() {

    private var _binding: DialogbaseBinding? = null
    private val binding get() = _binding!!

    var onExitClick: (() -> Unit)? = null
    var onCancelClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogbaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isCancelable = true

        initView()
        initAction()
    }

    private fun initView() {
        binding.apply {
            txtTitle.text = getString(R.string.exit)
            txtDesception.text = getString(R.string.do_you_want_to_exit)
        }
    }

    private fun initAction() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}