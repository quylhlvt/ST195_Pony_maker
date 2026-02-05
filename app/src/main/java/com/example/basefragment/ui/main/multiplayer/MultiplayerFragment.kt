package com.example.basefragment.ui.main.multiplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.popBack
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.databinding.FragmentMultiplayerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint

class MultiplayerFragment : BaseFragment<FragmentMultiplayerBinding, MultiplayerViewModel>(FragmentMultiplayerBinding::inflate,
    MultiplayerViewModel::class.java
) {
    private var count =1
    override fun viewListener() {
        binding.apply {
            setupActionBarListeners()
            plus.onClick(requireContext(),100){
                if (count>9) return@onClick
                count += 1
                updateCount(count)
            }
            minus.onClick(requireContext(),100){
                if (count<=1) return@onClick
                count -= 1
                updateCount(count)
            }
            txtPlayAgain.onClick(requireContext()){
                val bundle = Bundle().apply {
                    putInt("count", count)}
                findNavController().navigate(R.id.action_multiplayer_to_playMultiplayer,bundle)
            }
        }
    }
    private fun updateCount( count:Int){
        binding.apply {
            tvMinus.text = count.toString()

            minus.isEnabled = count > 1
            plus.isEnabled = count < 10

            minus.alpha = if (count > 1) 1.0f else 0.5f
            plus.alpha = if (count <10) 1.0f else 0.5f
        }
    }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMultiplayerBinding = FragmentMultiplayerBinding.inflate(inflater, container, false)
    private fun FragmentMultiplayerBinding.setupActionBar() {
        actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
            setImageActionBar(btnActionBarRight, R.drawable.guide)
        }
    }
    private fun FragmentMultiplayerBinding.setupActionBarListeners() {
        actionBar.apply {
            btnActionBarLeft.onClick(requireContext()) {
                lifecycleScope.launch {
                    popBack()
                }
            }
            btnActionBarRight.onClick(requireContext()) {
                val bundle = Bundle().apply {
                    putBoolean("isMuti", true)
                }
                    findNavController().navigate(R.id.action_multiplayer_to_guide, bundle)
            }
        }
    }
    override fun initView() {
        binding.apply {
            minus.alpha = if (count > 1) 1.0f else 0.5f
            setupActionBar()
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

}