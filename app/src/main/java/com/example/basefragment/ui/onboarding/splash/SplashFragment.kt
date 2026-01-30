package com.example.basefragment.ui.onboarding.splash

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.lifecycle.lifecycleScope
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.toIntro
import com.example.basefragment.core.extention.toLanguage
import com.example.basefragment.core.helper.SharedPreferencesManager.isLanuageScreen
import com.example.basefragment.core.helper.StripeProgressHelper
import com.example.basefragment.databinding.FragmentSplashBinding
import com.example.basefragment.utils.music.MusicLocal.isInSplashOrTutorial
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding, SplashViewModel>(
    FragmentSplashBinding::inflate, SplashViewModel::class.java
) {

    override fun viewListener() {

    }

    fun startFakeLoading3s() {
        StripeProgressHelper.applyStripe(binding.progressBar)
        StripeProgressHelper.animateStripe(binding.progressBar)
        StripeProgressHelper.animateProgress(binding.bgBar, binding.progressBar)
        StripeProgressHelper.animateCharacter(binding.bgBar, binding.progressBar, binding.imgCharacter)
    }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): FragmentSplashBinding = FragmentSplashBinding.inflate(inflater, container, false)

    override fun initView() {
        isInSplashOrTutorial = true
        startFakeLoading3s()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            kotlinx.coroutines.delay(2800)
            goToHome()
        }

    }

    override fun observeData() {
//        viewModel.data.observe(viewLifecycleOwner) { text ->
//            binding.textView.text = text
//        }
    }

    override fun bindViewModel() {
        /// load data local và api
//        lifecycleScope.launch {
//            viewModel.loadLocalData()
//
//        }
    }

    private fun goToHome() {
        if (!isLanuageScreen()) {
            toLanguage()
            return
        }
        toIntro()
    }
}