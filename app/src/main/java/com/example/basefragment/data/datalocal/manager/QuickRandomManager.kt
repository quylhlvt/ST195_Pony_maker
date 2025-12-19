package com.example.basefragment.data.datalocal.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import com.example.basefragment.data.model.custom.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class QuickRandomManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDataManager: AppDataManager,
    private val imageManager: CharacterImageManager
) {
    private val quickRandomFileName = "quick_random.json"

    companion object {
        private const val TAG = "QuickRandomManager"
        private const val CHARACTERS_PER_TEMPLATE = 10 // ✅ 10 nhân vật cho mỗi template
    }

    /**
     * ✅ Load quick random characters từ JSON
     */
    suspend fun loadQuickRandomCharacters(): List<CustomModel> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, quickRandomFileName)
                if (!file.exists()) return@withContext emptyList()

                val json = file.readText()
                val type = object : TypeToken<List<CustomModel>>() {}.type
                val characters = Gson().fromJson<List<CustomModel>>(json, type)

                Log.d(TAG, "✅ Loaded ${characters.size} quick random characters")
                characters
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading quick random: ${e.message}", e)
                emptyList()
            }
        }
    }

    /**
     * ✅ Save quick random characters to JSON (public for external use)
     */
    suspend fun saveQuickRandomToJson(characters: List<CustomModel>) {
        withContext(Dispatchers.IO) {
            try {
                val json = Gson().toJson(characters)
                val file = File(context.filesDir, quickRandomFileName)
                file.writeText(json)
                Log.d(TAG, "✅ Saved ${characters.size} quick random characters")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving quick random: ${e.message}", e)
            }
        }
    }

    /**
     * ✅ Save quick random characters to JSON (private wrapper)
     */
    private suspend fun saveQuickRandomCharacters(characters: List<CustomModel>) {
        saveQuickRandomToJson(characters)
    }

    /**
     * ✅ Generate 10 random characters cho mỗi template
     * @return List of generated characters with images
     */
    suspend fun generateQuickRandomCharacters(
        onProgress: (current: Int, total: Int, templateName: String) -> Unit = { _, _, _ -> }
    ): List<CustomModel> {
        return withContext(Dispatchers.IO) {
            val templates = appDataManager.templates.value
            if (templates.isEmpty()) {
                Log.e(TAG, "❌ No templates available")
                return@withContext emptyList()
            }

            val allGeneratedCharacters = mutableListOf<CustomModel>()
            var currentIndex = 0
            val totalCharacters = templates.size * CHARACTERS_PER_TEMPLATE

            templates.forEach { template ->
                Log.d(TAG, "=== Generating characters for template: ${template.id} ===")

                repeat(CHARACTERS_PER_TEMPLATE) { index ->
                    try {
                        // ✅ Generate random character
                        val randomCharacter = generateRandomCharacter(template)

                        // ✅ Generate random image (without UI rendering)
                        val imagePath = generateRandomImage(randomCharacter)

                        // ✅ Save with image path
                        val finalCharacter = randomCharacter.copy(imageSave = imagePath)
                        allGeneratedCharacters.add(finalCharacter)

                        currentIndex++
                        onProgress(currentIndex, totalCharacters, template.id)

                        Log.d(TAG, "   ✅ Generated character ${index + 1}/$CHARACTERS_PER_TEMPLATE")
                    } catch (e: Exception) {
                        Log.e(TAG, "   ❌ Error generating character: ${e.message}", e)
                    }
                }
            }

            // ✅ Save to JSON
            saveQuickRandomCharacters(allGeneratedCharacters)

            Log.d(TAG, "✅ Generated total ${allGeneratedCharacters.size} quick random characters")
            allGeneratedCharacters
        }
    }

    /**
     * ✅ Generate một random character từ template
     */
    private fun generateRandomCharacter(template: CustomModel): CustomModel {
        val randomSelections = arrayListOf<SelectionPart>()

        template.listPath.forEachIndexed { navIndex, bodyPart ->
            if (bodyPart.listPath.isEmpty()) {
                randomSelections.add(SelectionPart(nav = navIndex, color = -1, layer = -1))
                return@forEachIndexed
            }

            val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

            if (hasColor) {
                // ✅ Random color và layer
                val colorIndex = Random.nextInt(bodyPart.listPath.size)
                val color = bodyPart.listPath[colorIndex]

                // Filter out "none" and "dice"
                val realLayers = color.listPath.filterIndexed { idx, path ->
                    path != "none" && path != "dice" && path.contains("/")
                }

                if (realLayers.isNotEmpty()) {
                    val randomLayerPath = realLayers.random()
                    val layerIndex = color.listPath.indexOf(randomLayerPath)
                    randomSelections.add(SelectionPart(nav = navIndex, color = colorIndex, layer = layerIndex))
                } else {
                    randomSelections.add(SelectionPart(nav = navIndex, color = -1, layer = -1))
                }
            } else {
                // ✅ No color system - random layer
                val realImages = bodyPart.listPath.mapNotNull { cm ->
                    cm.listPath.firstOrNull { it.contains("/") && it != "none" && it != "dice" }
                }

                if (realImages.isNotEmpty()) {
                    val position = bodyPart.position.toIntOrNull() ?: 0
                    val layerImages = mutableListOf<String>()
                    if (position != 1) layerImages.add("none")
                    layerImages.add("dice")
                    layerImages.addAll(realImages)

                    val randomRealImage = realImages.random()
                    val layerIndex = layerImages.indexOf(randomRealImage)
                    randomSelections.add(SelectionPart(nav = navIndex, color = -1, layer = layerIndex))
                } else {
                    randomSelections.add(SelectionPart(nav = navIndex, color = -1, layer = -1))
                }
            }
        }

        return CustomModel(
            id = "quick_${template.id}_${UUID.randomUUID()}",
            avatar = template.avatar,
            listPath = ArrayList(template.listPath.map { bp ->
                bp.copy(
                    listPath = ArrayList(bp.listPath.map { color ->
                        color.copy(listPath = ArrayList(color.listPath))
                    })
                )
            }),
            selections = randomSelections,
            imageSave = "" // Will be filled later
        )
    }

    /**
     * ✅ Generate random image - Render actual character based on selections
     */
    private suspend fun generateRandomImage(character: CustomModel): String {
        return withContext(Dispatchers.IO) {
            try {
                // ✅ Render character bitmap từ selections
                val bitmap = renderCharacterFromSelections(character)

                // ✅ Save bitmap
                val imagePath = imageManager.saveBitmap(bitmap, character.id)
                imagePath ?: ""
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error generating image: ${e.message}", e)
                ""
            }
        }
    }

    /**
     * ✅ Render character bitmap từ selections (giống CustomizeFragment)
     */
    private suspend fun renderCharacterFromSelections(character: CustomModel): Bitmap {
        return withContext(Dispatchers.IO) {
            val width = 800
            val height = 800
            val finalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(finalBitmap)

            // ✅ Draw white background
            canvas.drawColor(android.graphics.Color.WHITE)

            try {
                // ✅ Sort theo z-index (giống updateCharacterPreview)
                val sortedParts = character.listPath.sortedBy { it.zIndex }

                for (bodyPart in sortedParts) {
                    val navIndex = character.listPath.indexOf(bodyPart)
                    if (navIndex == -1) continue

                    // ✅ Lấy imagePath từ selection
                    val imagePath = getImagePathForSelection(character, navIndex)

                    if (imagePath.isNullOrBlank() || imagePath == "none" || imagePath == "dice") {
                        continue
                    }

                    // ✅ Load và draw layer
                    val layerBitmap = loadBitmapFromAssets(imagePath)
                    if (layerBitmap != null) {
                        // Scale to fit canvas
                        val scaledBitmap = Bitmap.createScaledBitmap(
                            layerBitmap,
                            width,
                            height,
                            true
                        )
                        canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
                        scaledBitmap.recycle()
                        layerBitmap.recycle()
                    }
                }

                Log.d(TAG, "✅ Rendered character: ${character.id}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error rendering character: ${e.message}", e)
                // Draw error placeholder
                drawErrorPlaceholder(canvas, character.id)
            }

            finalBitmap
        }
    }

    /**
     * ✅ Load bitmap từ assets path
     */
    private fun loadBitmapFromAssets(assetPath: String): Bitmap? {
        return try {
            // Remove "file:///android_asset/" prefix
            val cleanPath = assetPath.replace("file:///android_asset/", "")
            val inputStream = context.assets.open(cleanPath)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading asset: $assetPath - ${e.message}")
            null
        }
    }

    /**
     * ✅ Get image path từ selection (giống CustomizeViewModel.getImagePathForSelection)
     */
    private fun getImagePathForSelection(character: CustomModel, navIndex: Int): String? {
        val selection = character.selections.getOrNull(navIndex) ?: return null
        val bodyPart = character.listPath.getOrNull(navIndex) ?: return null

        val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

        if (!hasColor) {
            if (selection.layer == -1) return null

            val position = bodyPart.position.toIntOrNull() ?: 0
            val layerImages = mutableListOf<String>()
            if (position != 1) layerImages.add("none")
            layerImages.add("dice")
            layerImages.addAll(
                bodyPart.listPath.mapNotNull { it.listPath.firstOrNull { p -> p.contains("/") } }
            )

            val selectedVariant = layerImages.getOrNull(selection.layer) ?: return null
            return if (selectedVariant == "none") "" else selectedVariant
        } else {
            if (selection.color == -1 || selection.layer == -1) return null
            val color = bodyPart.listPath.getOrNull(selection.color) ?: return null
            return color.listPath.getOrNull(selection.layer)
        }
    }

    /**
     * ✅ Draw error placeholder
     */
    private fun drawErrorPlaceholder(canvas: Canvas, characterId: String) {
        val paint = Paint().apply {
            color = android.graphics.Color.RED
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Render Error",
            canvas.width / 2f,
            canvas.height / 2f - 40f,
            paint
        )
        canvas.drawText(
            characterId.takeLast(8),
            canvas.width / 2f,
            canvas.height / 2f + 40f,
            paint
        )
    }

    /**
     * ✅ Delete all quick random characters and images
     */
    suspend fun clearQuickRandomCharacters() {
        withContext(Dispatchers.IO) {
            try {
                // Delete JSON file
                val file = File(context.filesDir, quickRandomFileName)
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "✅ Deleted quick random JSON")
                }

                // Delete all quick random images
                val characters = loadQuickRandomCharacters()
                characters.forEach { character ->
                    if (character.imageSave.isNotEmpty()) {
                        imageManager.deleteImage(character.imageSave)
                    }
                }

                Log.d(TAG, "✅ Cleared all quick random characters")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error clearing quick random: ${e.message}", e)
            }
        }
    }

    /**
     * ✅ Get character by ID
     */
    suspend fun getQuickRandomById(characterId: String): CustomModel? {
        val characters = loadQuickRandomCharacters()
        return characters.find { it.id == characterId }
    }

    /**
     * ✅ Check if quick random data exists
     */
    suspend fun hasQuickRandomData(): Boolean {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, quickRandomFileName)
            file.exists() && loadQuickRandomCharacters().isNotEmpty()
        }
    }
}