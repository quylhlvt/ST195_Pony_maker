package com.example.basefragment.core.extention

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R

/**
 * Safe navigation helper
 */
fun Fragment.nav(actionId: Int) {
    try {
        findNavController().navigate(actionId)
    } catch (e: Exception) {
        android.util.Log.e("Navigation", "Navigation error: ${e.message}")
    }
}

// ============ SPLASH FRAGMENT ============
fun Fragment.toLanguage() = nav(R.id.action_splash_to_language)
fun Fragment.toIntro() = nav(R.id.action_splash_to_intro)

// ============ LANGUAGE FRAGMENT (in Onboarding) ============
fun Fragment.toIntroFromLanguage() = nav(R.id.action_language_to_intro)

// ============ INTRO FRAGMENT ============
fun Fragment.toHome() = nav(R.id.action_intro_to_home)

// ============ HOME FRAGMENT ============
fun Fragment.toSettingFromHome() = nav(R.id.action_home_to_setting)
fun Fragment.toGuideFromManual() = nav(R.id.action_manual_to_guide)

// ============ SETTING FRAGMENT ============
fun Fragment.toLangFromSetting() = nav(R.id.action_setting_to_language)

/**
 * QUAN TRỌNG: Setting back về Home dùng popBackStack thay vì action
 */
// ============ LANGUAGE FRAGMENT (in Setting) ============
fun Fragment.toSettingFromLang() {
    try {
        // Pop back về Setting
        findNavController().popBackStack()
    } catch (e: Exception) {
        android.util.Log.e("Navigation", "Error popping back to setting: ${e.message}")
    }
}

/**
 * Dùng khi muốn về Home từ Language trong Setting
 */
fun Fragment.toHomeFromLanguage() {
    try {
        findNavController().navigate(R.id.action_lang_to_home)
    } catch (e: Exception) {
        android.util.Log.e("Navigation", "Error navigating to home: ${e.message}")
    }
}
// ============ GENERIC POP BACK ============
fun Fragment.popBack(): Boolean {
    return try {
        findNavController().popBackStack()
    } catch (e: Exception) {
        android.util.Log.e("Navigation", "Error popping back: ${e.message}")
        false
    }
}