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
        setImageActionBar(binding.actionBar.btnActionBarLeft, R.drawable.back_app)
        setupRecyclerView()
        // Vào màn → load + nếu chưa có data thì tự động tạo luôn
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
            },
            onCharacterLongClick = { character, position ->
                showCharacterOptions(character)
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

        // ✅ Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isGenerating.collect { isGenerating ->
                if (isGenerating) {
//                    binding.progressBar.visible()
//                    binding.btnGenerate.isEnabled = false
                } else {
//                    binding.progressBar.gone()
//                    binding.btnGenerate.isEnabled = true
                    progressDialog?.dismiss()
                }
            }
        }

        // ✅ Observe generation progress
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.generationProgress.collect { progress ->
                updateProgressDialog(
                    progress.current,
                    progress.total,
                    progress.templateName,
                    progress.step
                )
            }
        }
    }

    override fun viewListener() {
        // ✅ Back button
        binding.actionBar.btnActionBarLeft.onClick {
            findNavController().navigateUp()
        }

        // ✅ Generate button
//        binding.btnGenerate.setOnClickListener {
//            confirmGenerate()
//        }
//
//        // ✅ Clear all button
//        binding.btnClearAll.setOnClickListener {
//            confirmClearAll()
//        }
//
//        // ✅ Empty state button
//        binding.btnGenerateEmpty.setOnClickListener {
//            confirmGenerate()
//        }
    }

//    private fun confirmGenerate() {
//        AlertDialog.Builder(requireContext())
//            .setTitle("Generate Quick Random")
//            .setMessage("Generate 10 random characters for each template? This may take a while.")
//            .setPositiveButton("Generate") { _, _ ->
//                startGeneration()
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }

    private fun startGeneration() {
        showProgressDialog()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.generateQuickRandomCharacters()
                Toast.makeText(
                    requireContext(),
                    "Quick random characters generated!",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showProgressDialog() {
        progressDialog = AlertDialog.Builder(requireContext())
            .setTitle("Generating...")
            .setMessage("0 / 0")
            .setCancelable(false)
            .create()
        progressDialog?.show()
    }

    private fun updateProgressDialog(current: Int, total: Int, templateName: String, step: String) {
        progressDialog?.setMessage(
            "Progress: $current / $total\n" +
                    "Template: ${templateName.substringAfter("template_")}\n" +
                    "Step: $step"
        )
    }

    private fun confirmClearAll() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear All")
            .setMessage("Delete all quick random characters and their images?")
            .setPositiveButton("Delete") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.clearQuickRandomCharacters()
                    Toast.makeText(
                        requireContext(),
                        "Quick random cleared!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCharacterOptions(character: CustomModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Character Options")
            .setItems(arrayOf("View/Edit", "Save to My Pony", "Delete")) { _, which ->
                when (which) {
                    0 -> {
                        // View/Edit
                        quickRandomAdapter.currentList.indexOf(character).let { position ->
                            if (position >= 0) {
                                quickRandomAdapter.submitList(quickRandomAdapter.currentList)
                                // Trigger click
                            }
                        }
                    }

                    1 -> {
                        // Save to My Pony
                        saveToMyPony(character)
                    }

                    2 -> {
                        // Delete
                        confirmDeleteCharacter(character)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveToMyPony(character: CustomModel) {
        val newCharacter = character.copy(
            id = java.util.UUID.randomUUID().toString(),
            updatedAt = System.currentTimeMillis()
        )
        viewModelActivity.updateOrAddCharacter(newCharacter)

        Toast.makeText(
            requireContext(),
            "Saved to My Pony!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun confirmDeleteCharacter(character: CustomModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Character")
            .setMessage("Delete this quick random character?")
            .setPositiveButton("Delete") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.deleteQuickRandomCharacter(character.id)
                    Toast.makeText(
                        requireContext(),
                        "Character deleted!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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