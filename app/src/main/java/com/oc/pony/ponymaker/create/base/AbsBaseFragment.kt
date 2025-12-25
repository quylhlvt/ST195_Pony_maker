package com.oc.pony.ponymaker.create.base

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.oc.pony.ponymaker.create.utils.showSystemUI


abstract class AbsBaseFragment <V: ViewDataBinding, G: Activity>: androidx.fragment.app.Fragment() {
    lateinit var binding : V
    private var mView: View? = null
    lateinit var mActivity : Activity


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (mView != null){
            mView
        } else{
            _root_ide_package_.com.oc.pony.ponymaker.create.utils.SystemUtils.setLocale(requireContext())
            binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
            binding.lifecycleOwner = this
            mView = binding.root
            mActivity = activity as G
            mActivity.showSystemUI()
            initView()
            setClick()
            binding.root
        }

    }

    abstract fun getLayout(): Int
    abstract fun initView()
    abstract fun setClick()
}