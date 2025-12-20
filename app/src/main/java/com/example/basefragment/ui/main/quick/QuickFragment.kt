package com.example.basefragment.ui.main.quick

import android.app.AlertDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class QuickFragment : BaseFragment<FragmentQuickBinding, QuickViewModel>(
    FragmentQuickBinding::inflate,
    QuickViewModel::class.java
) {
    var check = false

    private lateinit var quickRandomAdapter: CharacterListAdapter

    override fun initView() {
        showLoadingSafe()
        setImageActionBar(binding.actionBar.btnActionBarLeft, R.drawable.back_app)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        quickRandomAdapter = CharacterListAdapter(
            onCharacterClick = { character, position ->
                navigateToCustomize(character)
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
        // ✅ 🔥 Observe real-time characters - Update ngay khi có item mới
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.quickRandomCharacters.collect { characters ->
                quickRandomAdapter.submitList(characters)

                Log.d("QuickFragment", "📱 UI Updated: ${characters.size} characters")

                // Update empty state
                if (characters.isEmpty()) {
                    if (!check){
                        hideLoadingSafe()
                    }
                    check=true

                    // Show empty/loading state
                    binding.recyclerViewQuick.visibility = android.view.View.GONE
                    // binding.emptyStateLayout.visibility = android.view.View.VISIBLE
                } else {
                    // Show list
                    binding.recyclerViewQuick.visibility = android.view.View.VISIBLE
                    // binding.emptyStateLayout.visibility = android.view.View.GONE
                }
            }
        }

        // ✅ Observe progress từ ViewModelActivity
        lifecycleScope.launch {
            viewModelActivity.quickRandomProgress.collect { progress ->
                if (progress.current > 0) {
                    // Show progress nếu cần
                    Log.d("QuickFragment", "📊 Progress: ${progress.current}/${progress.total}")
                }
            }
        }
    }

    override fun viewListener() {
        // ✅ Back button
        binding.actionBar.btnActionBarLeft.onClick {
            findNavController().navigateUp()
        }
    }

    /**
     * ✅ Navigate to customize screen
     */
    private fun navigateToCustomize(character: CustomModel) {
        lifecycleScope.launch {
            try {
                // 🔥 TÌM TEMPLATE GỐC để lấy TOÀN BỘ dữ liệu (all colors, all layers)
                val templateId = character.id
                    .removePrefix("quick_")
                    .substringBeforeLast("_")  // "template_xxx"

                Log.d("QuickFragment", "🔍 Finding template for quick character:")
                Log.d("QuickFragment", "   - Quick ID: ${character.id}")
                Log.d("QuickFragment", "   - Template ID: $templateId")

                val template = viewModelActivity.getCharacterById(templateId)

                if (template == null) {
                    Log.e("QuickFragment", "❌ Template not found: $templateId")
                    Toast.makeText(requireContext(), "Template not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 🔥 TẠO CHARACTER TẠM với:
                // - ID tạm thời (để xóa khi back mà không save)
                // - TOÀN BỘ listPath từ template (all colors, all layers)
                // - SELECTIONS từ quick random (để user thấy kết quả random)
                val newCharacter = template.copy(
                    id = "temp_from_quick_${java.util.UUID.randomUUID()}",  // 🔥 ID tạm
                    listPath = ArrayList(template.listPath.map { bp ->
                        bp.copy(
                            listPath = ArrayList(bp.listPath.map { color ->
                                color.copy(listPath = ArrayList(color.listPath))
                            })
                        )
                    }),
                    selections = ArrayList(character.selections),  // 🔥 GIỮ selections từ quick
                    imageSave = "",  // 🔥 Chưa có ảnh (sẽ generate mới khi save)
                    updatedAt = System.currentTimeMillis()
                )

                Log.d("QuickFragment", "🚀 Creating temp character from quick random:")
                Log.d("QuickFragment", "   - Template ID: $templateId")
                Log.d("QuickFragment", "   - Temp ID: ${newCharacter.id}")
                Log.d("QuickFragment", "   - Selections: ${newCharacter.selections.size} (from quick)")
                Log.d("QuickFragment", "   - Body parts: ${newCharacter.listPath.size}")

                // ✅ Add to customized list (tạm thời)
                viewModelActivity.updateOrAddCharacter(newCharacter)

                // ✅ Wait for confirmation
                viewModelActivity.characters
                    .first { characters ->
                        characters.any { it.id == newCharacter.id }
                    }

                // ✅ Get index
                val newIndex = viewModelActivity.characters.value.indexOfFirst {
                    it.id == newCharacter.id
                }

                if (newIndex >= 0) {
                    Log.d("QuickFragment", "✅ Navigate to customize with index: $newIndex")
                    findNavController().navigate(
                        R.id.action_quick_to_custom,
                        bundleOf(
                            "characterIndex" to newIndex,
                            "isQuickRandom" to true  // 🔥 TRUE để biết cần xóa temp khi back
                        )
                    )
                } else {
                    throw Exception("Character not found in list")
                }

            } catch (e: Exception) {
                Log.e("QuickFragment", "❌ Error navigating: ${e.message}", e)
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * ✅ Show character options menu
     */
    private fun showCharacterOptions(character: CustomModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Character Options")
            .setItems(arrayOf("View/Edit", "Save to My Pony", "Delete")) { _, which ->
                when (which) {
                    0 -> navigateToCustomize(character)
                    1 -> saveToMyPony(character)
                    2 -> confirmDeleteCharacter(character)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * ✅ Save to My Pony (create new customized character)
     */
    private fun saveToMyPony(character: CustomModel) {
        lifecycleScope.launch {
            try {
                // 🔥 TÌM TEMPLATE GỐC
                val templateId = character.id
                    .removePrefix("quick_")
                    .substringBeforeLast("_")

                val template = viewModelActivity.getCharacterById(templateId)

                if (template == null) {
                    Toast.makeText(requireContext(), "Template not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 🔥 TẠO CHARACTER MỚI với selections từ quick random
                val newCharacter = template.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    listPath = ArrayList(template.listPath.map { bp ->
                        bp.copy(
                            listPath = ArrayList(bp.listPath.map { color ->
                                color.copy(listPath = ArrayList(color.listPath))
                            })
                        )
                    }),
                    selections = ArrayList(character.selections),  // 🔥 Copy selections
                    imageSave = "",  // 🔥 Sẽ copy ảnh bên dưới
                    updatedAt = System.currentTimeMillis()
                )

                // 🔥 Copy ảnh từ quick sang character mới
                if (character.imageSave.isNotEmpty()) {
                    val quickImageFile = File(character.imageSave)
                    if (quickImageFile.exists()) {
                        val newImagePath = File(
                            quickImageFile.parent,
                            "character_${newCharacter.id}.png"
                        ).absolutePath

                        quickImageFile.copyTo(File(newImagePath), overwrite = true)

                        val finalCharacter = newCharacter.copy(imageSave = newImagePath)
                        viewModelActivity.updateOrAddCharacter(finalCharacter)

                        Log.d("QuickFragment", "✅ Saved to My Pony with image: ${finalCharacter.id}")
                        Toast.makeText(requireContext(), "Saved to My Pony!", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }

                // Fallback: save without image
                viewModelActivity.updateOrAddCharacter(newCharacter)
                Toast.makeText(requireContext(), "Saved to My Pony!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Log.e("QuickFragment", "❌ Error saving to My Pony: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * ✅ Delete quick random character
     */
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

    override fun onResume() {
        super.onResume()
        hideLoadingSafe()
    }
}