package com.oc.pony.ponymaker.create.ui.language

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oc.pony.ponymaker.create.R
import com.oc.pony.ponymaker.create.databinding.ActivityLanguageBinding
import com.oc.pony.ponymaker.create.utils.onSingleClick
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LanguageActivity : com.oc.pony.ponymaker.create.base.AbsBaseActivity<ActivityLanguageBinding>() {
    lateinit var adapter: LanguageAdapter
    var codeLang: String? = null

    @Inject
    lateinit var providerSharedPreference: com.oc.pony.ponymaker.create.utils.SharedPreferenceUtils


    override fun getLayoutId(): Int = R.layout.activity_language
    override fun initView() {

        codeLang = providerSharedPreference.getStringValue("language")
        if (codeLang.equals("")) {
            binding.icBack.visibility = View.GONE
            binding.tvTitle2.visibility = View.GONE
//            binding.imvDone.setImageResource(R.drawable.ic_tick_2)
        }else{
//            binding.imvDone.setImageResource(R.drawable.ic_tick)
            binding.tvTitle1.visibility = View.GONE
        }
        binding.rclLanguage.itemAnimator = null
        adapter = LanguageAdapter()
        setRecycleView()
    }

    override fun initAction() {

        binding.icBack.onSingleClick {
            finish()
        }
        binding.imvDone.onSingleClick {
            if (codeLang.equals("")) {
                Toast.makeText(
                    this,
                    getString(R.string.you_have_not_selected_anything_yet),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                _root_ide_package_.com.oc.pony.ponymaker.create.utils.SystemUtils.setPreLanguage(applicationContext, codeLang)
                providerSharedPreference.putStringValue("language", codeLang)
                if (_root_ide_package_.com.oc.pony.ponymaker.create.utils.SharedPreferenceUtils.Companion.getInstance(applicationContext).getBooleanValue(
                        _root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.LANGUAGE
                    )) {
                    var intent = Intent(
                        applicationContext,
                        _root_ide_package_.com.oc.pony.ponymaker.create.ui.main.MainActivity::class.java
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    finishAffinity()
                    startActivity(intent)
                } else {
                    _root_ide_package_.com.oc.pony.ponymaker.create.utils.SharedPreferenceUtils.Companion.getInstance(applicationContext)
                        .putBooleanValue(_root_ide_package_.com.oc.pony.ponymaker.create.utils.CONST.LANGUAGE, true)
                    var intent = Intent(applicationContext, _root_ide_package_.com.oc.pony.ponymaker.create.ui.tutorial.TutorialActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }


    private fun setRecycleView() {
        var i = 0
        lateinit var x: com.oc.pony.ponymaker.create.data.model.LanguageModel
        if (!codeLang.equals("")) {
            _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.listLanguage.forEach {
                _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.listLanguage[i].active = false
                if (codeLang.equals(it.code)) {
                    x = _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.listLanguage[i]
                    x.active = true
                }
                i++
            }

            _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.listLanguage.remove(x)
            _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.listLanguage.add(0, x)
        }
        adapter.getData(_root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.listLanguage)
        binding.rclLanguage.adapter = adapter
        val manager = GridLayoutManager(applicationContext, 1, RecyclerView.VERTICAL, false)
        binding.rclLanguage.layoutManager = manager

        adapter.onClick = {
            codeLang = _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.listLanguage[it].code
        }
    }

    override fun onBackPressed() {
        _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.listLanguage[_root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.positionLanguageOld].active = false
        _root_ide_package_.com.oc.pony.ponymaker.create.utils.DataHelper.positionLanguageOld = 0
        super.onBackPressed()
    }
}