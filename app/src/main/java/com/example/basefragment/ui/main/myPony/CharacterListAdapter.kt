package com.example.basefragment.ui.main.myPony

// ✅ ItemCharacterBinding Layout (item_character.xml)
/*
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="12dp">

        <ImageView
            android:id="@+id/imgCharacter"
            android:layout_width="match_parent"
            android:layout_height="200dp"
            android:scaleType="centerCrop"
            android:background="#F0F0F0" />

        <TextView
            android:id="@+id/tvCharacterName"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="Character"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="@android:color/black" />

        <TextView
            android:id="@+id/tvLastUpdated"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:text="Last updated"
            android:textSize="12sp"
            android:textColor="@android:color/darker_gray" />

    </LinearLayout>

</androidx.cardview.widget.CardView>
*/

//





//binding.actionBar.apply {
//    setImageActionBar(btnActionBarLeft, R.drawable.back_app)
//}
//setupRecyclerView()
//
//characterAdapter = CharacterListAdapter(
//onCharacterClick = { character, position ->
//    // ✅ Navigate to edit (tìm index trong combined list)
//    val globalIndex = viewModelActivity.characters.value.indexOfFirst {
//        it.id == character.id
//    }
//
//    if (globalIndex >= 0) {
//        findNavController().navigate(
//            R.id.action_mypony_to_custom,
//            bundleOf("characterIndex" to globalIndex)
//        )
//    }
//},
//onCharacterLongClick = { character, position ->
//    showCharacterOptions(character)
//}
//binding.recycleAvatar.apply {
//    layoutManager = GridLayoutManager(requireContext(), 2)
//    adapter = characterAdapter
//}
//override fun observeData() {
//    // ✅ Chỉ hiển thị customized characters (không có templates)
//    viewLifecycleOwner.lifecycleScope.launch {
//        viewModelActivity.customizedCharacters.collect { customized ->
//            characterAdapter.submitList(customized)
//
//            if (customized.isEmpty()) {
//                binding.noItem.visible()
//                binding.recycleAvatar.gone()
//            } else {
//                binding.noItem.gone()
//                binding.recycleAvatar.visible()
//            }
//        }
//    }
//}
//private fun showCharacterOptions(character: CustomModel) {
//    android.app.AlertDialog.Builder(requireContext())
//        .setTitle("Character Options")
//        .setItems(arrayOf("Edit", "Delete", "Duplicate")) { _, which ->
//            when (which) {
//                0 -> editCharacter(character)
//                1 -> confirmDeleteCharacter(character)
//                2 -> duplicateCharacter(character)
//            }
//        }
//        .setNegativeButton("Cancel", null)
//        .show()
//}
//private fun editCharacter(character: CustomModel) {
//    val globalIndex = viewModelActivity.characters.value.indexOfFirst {
//        it.id == character.id
//    }
//
//    if (globalIndex >= 0) {
//        findNavController().navigate(
//            R.id.action_mypony_to_custom,
//            bundleOf("characterIndex" to globalIndex)
//        )
//    }
//}
//
//private fun confirmDeleteCharacter(character: CustomModel) {
//    android.app.AlertDialog.Builder(requireContext())
//        .setTitle("Delete Character")
//        .setMessage("Are you sure you want to delete this character?")
//        .setPositiveButton("Delete") { _, _ ->
//            viewModelActivity.deleteCharacter(character.id)
//        }
//        .setNegativeButton("Cancel", null)
//        .show()
//}
//
//private fun duplicateCharacter(character: CustomModel) {
//    val duplicated = character.copy(
//        id = java.util.UUID.randomUUID().toString(),
//        updatedAt = System.currentTimeMillis(),
//        imageSave = "" // ✅ Clear image, sẽ capture lại khi save
//    )
//    viewModelActivity.updateOrAddCharacter(duplicated)
//}