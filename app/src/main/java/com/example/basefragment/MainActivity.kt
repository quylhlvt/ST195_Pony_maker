package com.example.basefragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.basefragment.core.base.BackPressHandler
import com.example.basefragment.core.extention.hideNavigation
import com.example.basefragment.core.helper.LanguageHelper
import com.example.basefragment.core.helper.SharedPreferencesManager
import com.example.basefragment.ui.main.manual.ManualViewModel
import com.example.basefragment.utils.music.MusicLocal
import com.example.basefragment.utils.music.SoundEffect
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val manualViewModel: ManualViewModel by viewModels()

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        hideNavigation(true)
        super.onCreate(savedInstanceState)

        // QUAN TRỌNG: Khởi tạo SharedPreferences TRƯỚC khi dùng
        initSharedPreferences()
        // SAU ĐÓ mới apply language (hoặc bỏ qua vì attachBaseContext đã apply rồi)
        // applyLanguage()
        setContentView(R.layout.activity_main)
        // Lấy NavController từ NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Kiểm tra xem Fragment hiện tại có xử lý back không
                val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()

                if (currentFragment is BackPressHandler) {
                    val handled = currentFragment.onBackPressed()
                    if (handled) {
                        // Fragment đã xử lý, không làm gì
                        return
                    }
                }

                // Fragment không xử lý hoặc không implement interface
                if (!navController.popBackStack()) {
                    finish()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (!MusicLocal.isInSplashOrTutorial &&MusicLocal.home)
            MusicLocal.play(this)
        SoundEffect.init(this)
    }
    /**
     * QUAN TRỌNG: Khởi tạo SharedPreferences
     */
    private fun initSharedPreferences() {
        val sharedPrefs = getSharedPreferences("DEFAULT", Context.MODE_PRIVATE)
        SharedPreferencesManager.sharedPreferences = sharedPrefs
        SharedPreferencesManager.editor = sharedPrefs.edit()
    }

    override fun attachBaseContext(newBase: Context) {
        // attachBaseContext được gọi TRƯỚC onCreate
        // Apply language ở đây (không cần SharedPreferencesManager)
        val sharedPrefs = newBase.getSharedPreferences("DEFAULT", Context.MODE_PRIVATE)
        val savedLanguage = sharedPrefs.getString("language_key", "en") ?: "en"

        val locale = Locale(savedLanguage)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

//     override fun onBackPressed() {
//         if (!navController.popBackStack()) {
//             super.onBackPressed()
//
//         }
//     }

    override fun onStop() {
        super.onStop()
        MusicLocal.pause()
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        hideNavigation(true)
    }
}