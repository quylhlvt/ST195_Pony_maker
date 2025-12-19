package com.example.basefragment.ui.main.myPony

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.basefragment.R
import com.example.basefragment.core.base.BaseFragment
import com.example.basefragment.core.extention.gone
import com.example.basefragment.core.extention.setImageActionBar
import com.example.basefragment.core.extention.visible
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.databinding.FragmentMyPonyBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPonyFragment : BaseFragment<FragmentMyPonyBinding, MyPonyViewModel>(
    FragmentMyPonyBinding::inflate,
    MyPonyViewModel::class.java
) {
    private lateinit var characterAdapter: CharacterListAdapter

    override fun initView() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.back_app)
        }
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        characterAdapter = CharacterListAdapter(
            onCharacterClick = { character, position ->
                // ✅ Navigate to edit (tìm index trong combined list)
                val globalIndex = viewModelActivity.characters.value.indexOfFirst {
                    it.id == character.id
                }

                if (globalIndex >= 0) {
                    findNavController().navigate(
                        R.id.action_mypony_to_custom,
                        bundleOf("characterIndex" to globalIndex)
                    )
                }
            },
            onCharacterLongClick = { character, position ->
                showCharacterOptions(character)
            }
        )

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = characterAdapter
        }
    }

    override fun observeData() {
        // ✅ Chỉ hiển thị customized characters (không có templates)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModelActivity.customizedCharacters.collect { customized ->
                characterAdapter.submitList(customized)

                if (customized.isEmpty()) {
                    binding.noItem.visible()
                    binding.recyclerView.gone()
                } else {
                    binding.noItem.gone()
                    binding.recyclerView.visible()
                }
            }
        }
    }

    private fun showCharacterOptions(character: CustomModel) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Character Options")
            .setItems(arrayOf("Edit", "Delete", "Duplicate")) { _, which ->
                when (which) {
                    0 -> editCharacter(character)
                    1 -> confirmDeleteCharacter(character)
                    2 -> duplicateCharacter(character)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editCharacter(character: CustomModel) {
        val globalIndex = viewModelActivity.characters.value.indexOfFirst {
            it.id == character.id
        }

        if (globalIndex >= 0) {
            findNavController().navigate(
                R.id.action_mypony_to_custom,
                bundleOf("characterIndex" to globalIndex)
            )
        }
    }

    private fun confirmDeleteCharacter(character: CustomModel) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Character")
            .setMessage("Are you sure you want to delete this character?")
            .setPositiveButton("Delete") { _, _ ->
                viewModelActivity.deleteCharacter(character.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun duplicateCharacter(character: CustomModel) {
        val duplicated = character.copy(
            id = java.util.UUID.randomUUID().toString(),
            updatedAt = System.currentTimeMillis(),
            imageSave = "" // ✅ Clear image, sẽ capture lại khi save
        )
        viewModelActivity.updateOrAddCharacter(duplicated)
    }

    override fun viewListener() {
        binding.actionBar.apply {
            btnActionBarLeft.setOnClickListener {
                findNavController().navigateUp()
            }
        }
        // TODO: Add button to navigate to template selector
    }

    override fun bindViewModel() {}

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMyPonyBinding = FragmentMyPonyBinding.inflate(inflater, container, false)
}