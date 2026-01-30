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
import com.example.basefragment.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding, SplashViewModel>(
    FragmentSplashBinding::inflate, SplashViewModel::class.java
) {
    override fun viewListener() {

    }

    fun startFakeLoading3s() {
        binding.apply {
            lottieLoading.progress = 0f

            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 3000L // 3 giây
                interpolator = LinearInterpolator()

                addUpdateListener {
                    lottieLoading.progress = it.animatedValue as Float
                }
            }

            animator.start()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): FragmentSplashBinding = FragmentSplashBinding.inflate(inflater, container, false)

    override fun initView() {
        startFakeLoading3s()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            kotlinx.coroutines.delay(4000)
            goToHome()
        }
//        lifecycleScope.

//        binding.textView.text = "Home Fragment"
//        binding.btnTest.setOnClickListener {
//            showSnackbar("Xin chào từ Home!")
//        }
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