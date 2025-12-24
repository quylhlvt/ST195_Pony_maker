package com.example.basefragment.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.example.basefragment.utils.SystemUtils
import com.example.basefragment.utils.showSystemUI

abstract class AbsBaseActivity<V : ViewDataBinding> : AppCompatActivity() {
    lateinit var binding: V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemUtils.setLocale(this)
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        initView()
        initAction()
    }

    override fun onResume() {
        super.onResume()
            showSystemUI()
    }

    override fun onRestart() {
        super.onRestart()
        SystemUtils.setLocale(this)
    }
    abstract fun getLayoutId(): Int
    abstract fun initView()
    abstract fun initAction()

}