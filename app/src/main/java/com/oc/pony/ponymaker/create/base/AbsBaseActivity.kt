package com.oc.pony.ponymaker.create.base

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.oc.pony.ponymaker.create.utils.showSystemUI

abstract class AbsBaseActivity<V : ViewDataBinding> : androidx.appcompat.app.AppCompatActivity() {
    lateinit var binding: V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _root_ide_package_.com.oc.pony.ponymaker.create.utils.SystemUtils.setLocale(this)
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
        _root_ide_package_.com.oc.pony.ponymaker.create.utils.SystemUtils.setLocale(this)
    }
    abstract fun getLayoutId(): Int
    abstract fun initView()
    abstract fun initAction()

}