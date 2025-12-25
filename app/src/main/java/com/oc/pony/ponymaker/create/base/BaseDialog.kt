package com.oc.pony.ponymaker.create.base

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.utils.showSystemUI

abstract class BaseDialog <DB : ViewDataBinding>(var context : Activity, var canAble: Boolean) :
    android.app.Dialog(context, R.style.BaseDialog) {

    lateinit var binding: DB

    abstract fun getContentView(): Int
    abstract fun initView()
    abstract fun bindView()
    override fun onStart() {
        super.onStart()
        window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        _root_ide_package_.com.oc.pony.ponymaker.create.utils.SystemUtils.setLocale(context)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), getContentView(), null, false)
        setContentView(binding.root)
        setCancelable(canAble)
        initView()
        bindView()
        setOnDismissListener {
            context.showSystemUI()
        }
    }
}