package com.example.basefragment.ui.main.manual

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.toIntro
import com.example.basefragment.core.extention.toLanguage
import com.example.basefragment.core.helper.SharedPreferencesManager.isLanuageScreen
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.FragmentManualBinding
import com.example.basefragment.databinding.FragmentSplashBinding
import com.example.basefragment.databinding.FragmentSplashBinding.inflate
import com.example.basefragment.ui.onboarding.splash.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class ManualFragment2 : BaseFragment<FragmentManualBinding, ManualViewModel>(FragmentManualBinding::inflate,
    ManualViewModel::class.java
) {
    private lateinit var player1List: List<ManualModel>

    private val manualAdapter by lazy {
        ManualAdapter(requireContext())
    }
    override fun viewListener() {

        binding.tvNext.onClick(requireContext()){
            val player2List = manualAdapter.getItems()

            if (manualAdapter.getSelectItems().isEmpty()) {
                showToast("Please select 3 items")
                return@onClick
            }

            val bundle = Bundle().apply {
                putParcelableArray(
                    "player1List",
                    player1List.toTypedArray()
                )
                putParcelableArray(
                    "player2List",
                    player2List.toTypedArray()
                )
            }

            findNavController()
                .navigate(R.id.action_manual_to_play, bundle)
        }

    }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentManualBinding = FragmentManualBinding.inflate(inflater, container, false)

    override fun initView() {
        player1List = arguments
            ?.getParcelableArray("selectedList")
            ?.map { it as ManualModel }
            ?: emptyList()
        binding.apply {
            txtPlayer.text = getString(R.string.player_2)
            val data = List(9) { ManualModel(false) }
            recycleChoose.apply {
                adapter = manualAdapter
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
            }
            manualAdapter.submitList(data)
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

}