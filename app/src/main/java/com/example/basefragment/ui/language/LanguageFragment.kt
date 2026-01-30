package com.example.basefragment.ui.language

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.popBack
import com.example.basefragment.core.extention.toHomeFromLanguage
import com.example.basefragment.core.extention.toIntroFromLanguage
import com.example.basefragment.core.extention.toSettingFromLang
import com.example.basefragment.core.extention.visible
import com.example.basefragment.core.helper.LanguageHelper
import com.example.basefragment.core.helper.SharedPreferencesManager.isLanguageKey
import com.example.basefragment.core.helper.SharedPreferencesManager.isLanuageScreen
import com.example.basefragment.databinding.FragmentLanguageBinding
import com.example.basefragment.utils.LanguageManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LanguageFragment : BaseFragment<FragmentLanguageBinding, LanguageViewModel>(
    FragmentLanguageBinding::inflate, LanguageViewModel::class.java
) {
    private val languageAdapter by lazy { LanguageAdapter(requireContext()) }
    private var isFromSetting = false

    override fun viewListener() {
        binding.apply {

            actionBar.btnActionBarRight.onClick {
                handleDone()
            }
            actionBar.btnActionBarLeft.onClick(500) {
                when {
                isFromSetting -> {
                    // Từ Setting -> Back về Setting
                    toSettingFromLang()
                }
                else -> {
                    // Từ Onboarding -> Back về Splash/Language
                    popBack()
                }
            }}
        }
        handleRcv()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            toSettingFromLang()
        }
    }
    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): FragmentLanguageBinding = FragmentLanguageBinding.inflate(inflater, container, false)

    override fun initView() {
        isFromSetting = (findNavController().currentDestination?.id == R.id.languageInSetting)

        binding.actionBar.apply {
            btnActionBarLeft.setImageResource(R.drawable.back_app)
        }
        initRcv()

        val checkFirst = isLanuageScreen()
        val keyLanguage = isLanguageKey()
        val currentLang = keyLanguage

        viewModel.setFirstLanguage(isFirst = !checkFirst)
        viewModel.loadLanguages(currentLang)
//            btnActionBarRight.apply {
//                visible()
//                setImageResource(R.drawable.select_language)
//            }
//            if (!isLanuageScreen()) {
//                tvStart.visible()
//                return
//            }
//            tvCenter.visible()


//        binding.textView.text = "Home Fragment"
//        binding.btnTest.setOnClickListener {
//            showSnackbar("Xin chào từ Home!")
//
    }

    override fun observeData() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {


                launch {
                    viewModel.isFirstLanguage.collect { isFirst ->
                        if (isFirst) {
                            binding.actionBar.apply {
                                tvStart.visible()
                                btnActionBarRight.visible()
                                btnActionBarRight.setImageResource(R.drawable.select_language)

                            }
                        } else {
                            binding.actionBar.apply {
                            btnActionBarLeft.visible()
                             tvCenter.visible()
                            btnActionBarRight.setImageResource(R.drawable.select_language)}

                        }
                    }
                }
                launch {
                    viewModel.languageList.collect { list ->
                        Log.d("LANG", "Updating adapter with list size=${list.size}") // check log
                        languageAdapter.submitList(list)
                    }
                }
                launch {
                    viewModel.codeLang.collect { code ->
                        if (code.isNotEmpty()) {
                            binding.actionBar.btnActionBarRight.visible()
                        }
                    }
                }
            }
        }
//        viewModel.data.observe(viewLifecycleOwner) { text ->
//            binding.textView.text = text
//        }
    }

    override fun bindViewModel() {


    }

    private fun initRcv() {
        binding.recycleLanguage.apply {
            adapter = languageAdapter
            itemAnimator = null
        }
    }

    private fun handleRcv() {
        binding.apply {
            languageAdapter.onItemClick = { code ->
                binding.actionBar.btnActionBarRight.visible()
                viewModel.selectLanguage(code)
            }
        }
    }

    private fun handleDone() {
        val code = viewModel.codeLang.value
        if (code.isEmpty()) {
            showToast(R.string.not_select_lang)
            return
        }

        // Save language
        sharedPreferences.setLanguageKey(code)

        // QUAN TRỌNG: Update ngôn ngữ ngay lập tức
        LanguageHelper.setLocale(requireContext(), code)
        LanguageManager.updateLanguage(code)
        if (viewModel.isFirstLanguage.value) {
            // Onboarding flow
            sharedPreferences.setLanuageScreen(true)
            Log.d("LANG", "Navigating to Intro")
            toIntroFromLanguage()
        } else {
            // Setting flow - Navigate về Home
            Log.d("LANG", "Navigating to Home")
            toHomeFromLanguage()
        }
    }
}