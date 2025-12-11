package com.example.basefragment.ui.language

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.visible
import com.example.basefragment.core.helper.SharedPreferencesManager.isLanguageKey
import com.example.basefragment.core.helper.SharedPreferencesManager.isLanuageScreen
import com.example.basefragment.core.helper.SharedPreferencesManager.sharedPreferences
import com.example.basefragment.databinding.FragmentLanguageBinding
import com.example.basefragment.utils.key.IntentKey
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageFragment : BaseFragment<FragmentLanguageBinding, LanguageViewModel>(
    FragmentLanguageBinding::inflate, LanguageViewModel::class.java
) {
    private val languageAdapter by lazy { LanguageAdapter(requireContext()) }
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
        val screenLanguage = isLanuageScreen()
        val keyLanguage = isLanguageKey()
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
}