package com.example.basefragment.ui.main.createPony

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.basefragment.R
import com.example.basefragment.ViewModelActivity
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.data.datalocal.manager.AppDataManager
import com.example.basefragment.databinding.FragmentChoosePonyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChoosePonyFragment : BaseFragment<FragmentChoosePonyBinding, ChoosePonyViewModel>(
    FragmentChoosePonyBinding::inflate,
    ChoosePonyViewModel::class.java
) {
    private val mainViewModel: ViewModelActivity by activityViewModels()
    private lateinit var adapter: ChoosePonyAdapter
    override fun viewListener() {
        binding.actionBar.apply {
            btnActionBarLeft.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }


    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentChoosePonyBinding = FragmentChoosePonyBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
        }
        adapter = ChoosePonyAdapter { character, position ->
            // Click vào 1 character
            // Navigate tới CustomFragment với character đã chọn
            findNavController().navigate(
                R.id.action_createPony_to_custom,
                bundleOf(
                    "characterId" to character.id,
                    "characterIndex" to position
                )
            )
        }

        binding.recycleChoose.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // 2 columns
            adapter = this.adapter
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
}