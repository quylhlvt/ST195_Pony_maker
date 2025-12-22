package com.example.basefragment.ui.main.quick

import android.app.AlertDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.onClick
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.databinding.FragmentQuickBinding
import com.example.basefragment.ui.main.myPony.CharacterListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuickFragment : BaseFragment<FragmentQuickBinding, QuickViewModel>(
    FragmentQuickBinding::inflate,
    QuickViewModel::class.java
) {

    private lateinit var quickRandomAdapter: CharacterListAdapter
    private var progressDialog: AlertDialog? = null

    override fun initView() {
        showLoadingSafe()
        setImageActionBar(binding.actionBar.btnActionBarLeft, R.drawable.back_app)
        setupRecyclerView()
        viewLifecycleOwner.lifecycleScope.launch {
            val hasData = viewModel.hasQuickRandomData()  // ← Đây mới là gọi thật
            if (!hasData) {
                startGeneration()
            }
        }
    }


    private fun setupRecyclerView() {
        quickRandomAdapter = CharacterListAdapter(
            onCharacterClick = { character, position ->
                // ✅ Navigate to CustomizeFragment để edit
                val globalIndex = viewModelActivity.characters.value.indexOfFirst {
                    it.id == character.id
                }

                if (globalIndex >= 0) {
                    findNavController().navigate(
                        R.id.action_quick_to_custom,
                        bundleOf("characterIndex" to globalIndex)
                    )
                } else {
                    // ✅ Character chưa có trong combined list, thêm vào
                    viewModelActivity.updateOrAddCharacter(character)

                    // Wait một chút để combined list update
                    lifecycleScope.launch {
                        kotlinx.coroutines.delay(100)
                        val newIndex = viewModelActivity.characters.value.indexOfFirst {
                            it.id == character.id
                        }
                        if (newIndex >= 0) {
                            findNavController().navigate(
                                R.id.action_quick_to_custom,
                                bundleOf("characterIndex" to newIndex)
                            )
                        }
                    }
                }
            }
        )

        binding.recyclerViewQuick.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = quickRandomAdapter
        }
    }

    override fun observeData() {
        // ✅ Observe quick random characters
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.quickRandomCharacters.collect { characters ->
                quickRandomAdapter.submitList(characters)
            }
        }




    }

    override fun viewListener() {
        // ✅ Back button
        binding.actionBar.btnActionBarLeft.onClick {
            findNavController().navigateUp()
        }

    }

    private fun startGeneration() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.generateQuickRandomCharacters()
                hideLoadingSafe()
            } catch (e: Exception) {

            }
        }
    }
    override fun bindViewModel() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentQuickBinding {
        return FragmentQuickBinding.inflate(inflater, container, false)
    }
}