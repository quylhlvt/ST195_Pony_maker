package com.example.basefragment.ui.main.view

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.loadImage
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.core.extention.visible
import com.example.basefragment.databinding.FragmentQuickBinding
import com.example.basefragment.databinding.FragmentQuickBinding.inflate
import com.example.basefragment.databinding.FragmentViewBinding
import com.example.basefragment.ui.main.quick.QuickViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewFragment : BaseFragment<FragmentViewBinding, ViewViewModel>(
    FragmentViewBinding::inflate,
    ViewViewModel::class.java
) {
    private val imagePath: String by lazy { arguments?.getString("imagePath") ?: "" }
    private val imageType: Int by lazy { arguments?.getInt("imageType", 0) ?: 0 }
    private val idEdit: String by lazy { arguments?.getString("idEdit") ?: "" }
    override fun viewListener() {
        binding.apply {
        actionBar.btnActionBarLeft.onClick {
            findNavController().navigateUp()
        }

        when (imageType) {
            0 -> {
               actionBar.btnActionBarNextToRight.onClick {
                }
                actionBar.btnActionBarRight.onClick {
                }
                btnBottomLeft.onClick {  }
                btnBottomRight.onClick {  }

            }

            1 -> {
               actionBar.btnActionBarNextToRight.onClick {
                }
               actionBar.btnActionBarRight.onClick {
                }
                btnBottomLeft.onClick {  }
                btnBottomRight.onClick {  }
            }
            2 -> {
               actionBar.btnActionBarRight.onClick {

                }
                btnBottomLeft.onClick {  }
                btnBottomRight.onClick {  }
            }
        }

    }
    }


    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentViewBinding = FragmentViewBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.apply {
            setImageActionBar(binding.actionBar.btnActionBarLeft, R.drawable.back_app)
           loadImage(requireContext(),imagePath, imvImage)
            when (imageType) {
                0 -> {
                    tvSuccess.visible()
                    setImageActionBar(binding.actionBar.btnActionBarRight, R.drawable.ic_home)


                }

                1 -> {
                    btnBottomLeft.text= getString(R.string.edit)
                    setImageActionBar(binding.actionBar.btnActionBarRight, R.drawable.ic_delete)
                    setImageActionBar(binding.actionBar.btnActionBarNextToRight, R.drawable.ic_share)

                }
                2 -> {
                    btnBottomLeft.text= getString(R.string.share)
                    setImageActionBar(binding.actionBar.btnActionBarRight, R.drawable.ic_delete)

                }
            }
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