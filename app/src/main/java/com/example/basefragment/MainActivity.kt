package com.example.basefragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.basefragment.core.extention.hideNavigation
import com.example.basefragment.core.helper.LanguageHelper
import com.example.basefragment.core.helper.SharedPreferencesManager
import com.example.basefragment.utils.music.MusicLocal
import com.example.basefragment.utils.music.SoundEffect
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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

    // ❌ XÓA onBackPressed() - Nó conflict với Fragment's OnBackPressedDispatcher
     override fun onBackPressed() {
         if (!navController.popBackStack()) {
             super.onBackPressed()
         }
     }

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