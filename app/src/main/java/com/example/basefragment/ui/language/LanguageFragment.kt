package com.example.basefragment.ui.language

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.visible
import com.example.basefragment.core.helper.SharedPreferencesManager.isLanguageKey
import com.example.basefragment.core.helper.SharedPreferencesManager.isLanuageScreen
import com.example.basefragment.core.helper.SharedPreferencesManager.sharedPreferences
import com.example.basefragment.databinding.FragmentLanguageBinding
import com.example.basefragment.utils.DataLocal
import com.example.basefragment.utils.key.IntentKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LanguageFragment : BaseFragment<FragmentLanguageBinding, LanguageViewModel>(
    FragmentLanguageBinding::inflate, LanguageViewModel::class.java
) {
    private val languageAdapter by lazy { LanguageAdapter(requireContext()) }
    private var keyLanguage: String? =null
    override fun viewListener() {
        binding.actionBar.btnActionBarRight.onClick {
            findNavController().navigate(R.id.action_language_to_intro)

        }

    }


    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): FragmentLanguageBinding = FragmentLanguageBinding.inflate(inflater, container, false)

    override fun initView() {
        initRcv()
        keyLanguage = isLanguageKey()


        binding.actionBar.apply {
            btnActionBarRight.apply {
                visible()
                setImageResource(R.drawable.select_language)
            }
            if (!isLanuageScreen()) {
                tvStart.visible()
                return
            }
            tvCenter.visible()
        }

//        binding.textView.text = "Home Fragment"
//        binding.btnTest.setOnClickListener {
//            showSnackbar("Xin chào từ Home!")
//        }
    }

    override fun observeData() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                launch {
//                viewModel.isFirstLanguage.collect { isFirst ->
//                    binding.actionBar.tvStart.visible(isFirst)
//                    binding.actionBar.tvCenter.visible(!isFirst)
//                }}

                launch {
                    viewModel.isFirstLanguage.collect { isFirst ->
                        if (isFirst) {
                            binding.actionBar.tvStart.visible()
                        } else {
                            binding.actionBar.btnActionBarLeft.visible()
                            binding.actionBar.tvCenter.visible()
                        }
                    }
                }
                launch {
                    viewModel.languageList.collect { list ->
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
        if (isLanuageScreen()){   viewModel.setFirstLanguage(keyLanguage == null)}
        viewModel.loadLanguages(keyLanguage?:"en")

    }

    private fun initRcv() {
        binding.recycleLanguage.apply {
            adapter = languageAdapter
            itemAnimator = null
        }
    }
}