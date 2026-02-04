package com.example.basefragment.ui.main.manual

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.*
import com.example.basefragment.core.helper.SharedPreferencesManager.isHowToClickFirst
import com.example.basefragment.core.helper.SharedPreferencesManager.setHowToClickFirst
import com.example.basefragment.data.model.manual.ManualModel
import com.example.basefragment.databinding.FragmentManualBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManualFragment : BaseFragment<FragmentManualBinding, ManualViewModel>(
    FragmentManualBinding::inflate,
    ManualViewModel::class.java
) {
    // ✅ Sử dụng activityViewModels để share ViewModel
    private val sharedViewModel: ManualViewModel by activityViewModels()

    private val manualAdapter by lazy {
        ManualAdapter(requireContext()).apply {
            onSelectionChanged = {
                binding.howtoclick.visibility = View.GONE
                setHowToClickFirst(true)
                // Update real-time khi user chọn
                sharedViewModel.updatePlayer1List(getItems())
            }
        }
    }

    override fun setupPreViews() {
        super.setupPreViews()

        screenRotation()
    }

    override fun onResume() {
        super.onResume()
        screenRotation()
    }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentManualBinding = FragmentManualBinding.inflate(inflater, container, false)

    override fun initView() {

        binding.apply {
            Glide.with(requireContext())
                .asGif()
                .load(R.raw.touch) // hoặc R.drawable.hand_tap_fixed
                .into(handTapAnimation)
            howtoclick.visibility = if (!isHowToClickFirst()) View.VISIBLE else View.GONE
            setupActionBar()
            setupRecyclerView()
            txtPlayer.text = getString(R.string.player_1)
        }
    }

    private fun FragmentManualBinding.setupActionBar() {
        actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
            setImageActionBar(btnActionBarRight, R.drawable.guide)
        }
    }

    private fun FragmentManualBinding.setupRecyclerView() {
        recycleChoose.apply {
            adapter = manualAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            layoutManager =
                object : androidx.recyclerview.widget.GridLayoutManager(requireContext(), 3) {
                    override fun canScrollVertically(): Boolean = false
                    override fun canScrollHorizontally(): Boolean = false
                }
            itemAnimator = null
        }
    }

    override fun viewListener() {
        binding.apply {
            setupActionBarListeners()
            setupNextButton()
        }
    }

    private fun FragmentManualBinding.setupActionBarListeners() {
        actionBar.apply {
            btnActionBarLeft.onClick(requireContext()) {
                lifecycleScope.launch {
                    popBack()
                    delay(100)
                    sharedViewModel.resetList1()
                }
            }
            btnActionBarRight.onClick(requireContext()) {
                toGuideFromManual()
            }
        }
    }

    private fun FragmentManualBinding.setupNextButton() {
        tvNext.onClick(requireContext()) {
            val selectedItems = manualAdapter.getSelectItems()

            if (selectedItems.size < 3) {
                showToast("Please select 3 items")
                return@onClick
            }

            // ✅ Lưu selection vào ViewModel
            sharedViewModel.updatePlayer1List(manualAdapter.getItems())

            // ✅ Navigate sang Player 2 (KHÔNG cần truyền data qua Bundle)
            findNavController().navigate(R.id.action_manualFragment_to_manualFragment2)
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.player1List.collect { list ->
                    manualAdapter.updateList(list)
                }
            }
        }
    }

    override fun bindViewModel() {}
}