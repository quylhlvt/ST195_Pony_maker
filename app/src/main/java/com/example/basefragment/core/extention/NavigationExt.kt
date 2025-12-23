package com.example.basefragment.core.extention

// NavigationExt.kt
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
fun Fragment.nav(actionId: Int) {
    try {
        findNavController().navigate(actionId)
    } catch (e: Exception) {
        // Đã ở đích hoặc action tạm thời không tồn tại → ignore
    }
}

// Dùng trong SplashFragment
fun Fragment.toLanguage() = nav(R.id.action_splash_to_language)
fun Fragment.toIntro()     = nav(R.id.action_splash_to_intro)

// Dùng trong IntroFragment
fun Fragment.toPermission() = nav(R.id.action_intro_to_permission)
fun Fragment.toHome()       = nav(R.id.action_intro_to_home) // hoặc action_permission_to_home đều được
fun Fragment.toSetting()       = nav(R.id.action_home_to_setting) // hoặc action_permission_to_home đều được

// Dùng trong PermissionFragment
fun Fragment.toHomeFromPermission() = nav(R.id.action_permission_to_home)

// Dùng trong LanguageFragment
fun Fragment.toIntroFromLanguage() = nav(R.id.action_language_to_intro)
fun Fragment.toHomeFromLanguage() = nav(R.id.action_language_to_home)