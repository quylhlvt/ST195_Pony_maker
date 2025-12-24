package com.example.basefragment.ui.setting

import android.view.View
import com.example.basefragment.base.AbsBaseActivity
import com.example.basefragment.ui.language.LanguageActivity
import com.example.basefragment.utils.RATE
import com.example.basefragment.utils.SharedPreferenceUtils
import com.example.basefragment.utils.newIntent
import com.example.basefragment.utils.onSingleClick
import com.example.basefragment.utils.policy
import com.example.basefragment.utils.rateUs
import com.example.basefragment.utils.shareApp
import com.example.basefragment.utils.unItem
import com.example.basefragment.R
import com.example.basefragment.databinding.ActivitySettingBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : AbsBaseActivity<ActivitySettingBinding>() {
    @Inject
    lateinit var sharedPreferences: SharedPreferenceUtils
    override fun getLayoutId(): Int = R.layout.activity_setting

    override fun initView() {
        if (sharedPreferences.getBooleanValue(RATE)) {
            binding.llRateUs.visibility = View.GONE
        }
        unItem = {
            binding.llRateUs.visibility = View.GONE
        }
    }

    override fun onStop() {
        super.onStop()
    }
    override fun initAction() {
        binding.apply {
            llLanguage.onSingleClick {
                startActivity(
                    newIntent(
                        applicationContext,
                        LanguageActivity::class.java
                    )
                )
            }
            llRateUs.onSingleClick {
                rateUs(0)
            }
            llShareApp.onSingleClick {
                shareApp()
            }
            llPrivacy.onSingleClick {
                policy()
            }
            imvBack.onSingleClick {
                finish()
            }
        }
    }
}