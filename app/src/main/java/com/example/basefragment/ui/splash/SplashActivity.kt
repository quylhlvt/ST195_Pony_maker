package com.example.basefragment.ui.splash

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.example.basefragment.base.AbsBaseActivity
import com.example.basefragment.data.repository.ApiRepository
import com.example.basefragment.ui.language.LanguageActivity
import com.example.basefragment.ui.tutorial.TutorialActivity
import com.example.basefragment.utils.CONST
import com.example.basefragment.utils.DataHelper.getData
import com.example.basefragment.utils.SharedPreferenceUtils
import com.example.basefragment.R
import com.example.basefragment.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AbsBaseActivity<ActivitySplashBinding>() {
    @Inject
    lateinit var apiRepository: ApiRepository

    @Inject
    lateinit var sharedPreferenceUtils: SharedPreferenceUtils
    override fun getLayoutId(): Int = R.layout.activity_splash

    override fun initView() {
                lifecycleScope.launch {
                    delay(20000)
                    action()
                }
           }

    override fun initAction() {
        GlobalScope.launch(Dispatchers.IO) {
            getData(apiRepository)
        }
    }

    fun action() {
        if (!sharedPreferenceUtils.getBooleanValue(CONST.LANGUAGE)
        ) {
            startActivity(Intent(this@SplashActivity, LanguageActivity::class.java))
        } else {
            startActivity(Intent(this@SplashActivity, TutorialActivity::class.java))
        }
        finish()
    }


    override fun onBackPressed() {

    }
}