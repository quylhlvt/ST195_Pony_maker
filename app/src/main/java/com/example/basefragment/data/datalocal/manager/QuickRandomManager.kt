package com.example.basefragment.data.datalocal.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import com.example.basefragment.data.model.custom.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        private const val CHARACTERS_PER_TEMPLATE = 10
    }

    // ✅ Real-time list của characters đang được generate
    private val _generatingCharacters = MutableStateFlow<List<CustomModel>>(emptyList())
    val generatingCharacters: StateFlow<List<CustomModel>> = _generatingCharacters.asStateFlow()

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
     * ✅ Generate 10 random characters cho mỗi template
     * Emit từng character ngay khi generate xong
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

            // ✅ Reset list trước khi generate
            _generatingCharacters.value = emptyList()
            val allGeneratedCharacters = mutableListOf<CustomModel>()

            var currentIndex = 0
            val totalCharacters = templates.size * CHARACTERS_PER_TEMPLATE

            templates.forEach { template ->
                Log.d(TAG, "=== Generating characters for template: ${template.id} ===")

                repeat(CHARACTERS_PER_TEMPLATE) { index ->
                    try {
                        // ✅ Generate random character
                        val randomCharacter = generateRandomCharacter(template)

                        // ✅ Generate random image
                        val imagePath = generateRandomImage(randomCharacter)

                        // ✅ Save with image path
                        val finalCharacter = randomCharacter.copy(imageSave = imagePath)

                        // ✅ Add to lists
                        allGeneratedCharacters.add(finalCharacter)

                        // ✅ 🔥 Emit ngay lập tức để UI update
                        _generatingCharacters.value = allGeneratedCharacters.toList()

                        currentIndex++
                        onProgress(currentIndex, totalCharacters, template.id)

                        Log.d(TAG, "   ✅ Generated character ${index + 1}/$CHARACTERS_PER_TEMPLATE")
                    } catch (e: Exception) {
                        Log.e(TAG, "   ❌ Error generating character: ${e.message}", e)
                    }
                }
            }

            // ✅ Save to JSON khi hoàn thành
            saveQuickRandomToJson(allGeneratedCharacters)

            Log.d(TAG, "✅ Generated total ${allGeneratedCharacters.size} quick random characters")
            allGeneratedCharacters
        }
    }

    /**
     * ✅ Generate random character với selections ngẫu nhiên
     */
    private fun generateRandomCharacter(template: CustomModel): CustomModel {
        val randomSelections = arrayListOf<SelectionPart>()

        template.listPath.forEachIndexed { navIndex, bodyPart ->
            if (bodyPart.listPath.isEmpty()) {
                randomSelections.add(SelectionPart(nav = navIndex, color = -1, layer = -1))
                return@forEachIndexed
            }

            val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }
            val position = bodyPart.position ?: 0

            if (hasColor) {
                // ✅ Có color: chọn random color và layer
                val colorIndex = Random.nextInt(bodyPart.listPath.size)
                val color = bodyPart.listPath[colorIndex]

                val realLayers = color.listPath.filter { path ->
                    path != "none" && path != "dice" && path.contains("/")
                }

                if (realLayers.isNotEmpty()) {
                    val randomLayerPath = realLayers.random()
                    val layerIndex = color.listPath.indexOf(randomLayerPath)
                    randomSelections.add(SelectionPart(nav = navIndex, color = colorIndex, layer = layerIndex))
                } else {
                    randomSelections.add(SelectionPart(nav = navIndex, color = colorIndex, layer = 0))
                }
            } else {
                // ✅ Không có color: chọn random layer
                val realImages = bodyPart.listPath.flatMap { it.listPath }.filter {
                    it.contains("/") && it != "none" && it != "dice"
                }

                if (realImages.isNotEmpty()) {
                    val layerImages = mutableListOf<String>()

                    // ✅ NAV 0 (position = 1): CHỈ có "dice"
                    // ✅ NAV khác: Có "none" và "dice"
                    if (position != 1) {
                        layerImages.add("none")
                    }
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
            imageSave = "",
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * ✅ Generate image từ character selections
     */
    private suspend fun generateRandomImage(character: CustomModel): String {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = renderCharacterFromSelections(character)
                val imagePath = imageManager.saveBitmap(bitmap, character.id)
                imagePath ?: ""
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error generating image: ${e.message}", e)
                ""
            }
        }
    }

    /**
     * ✅ Render character thành bitmap
     */
    private suspend fun renderCharacterFromSelections(character: CustomModel): Bitmap {
        return withContext(Dispatchers.IO) {
            val width = 800
            val height = 800
            val finalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(finalBitmap)
            canvas.drawColor(android.graphics.Color.TRANSPARENT)

            try {
                // Sort theo zIndex để render đúng thứ tự
                val sortedParts = character.listPath.sortedBy { it.zIndex }

                for (bodyPart in sortedParts) {
                    val navIndex = character.listPath.indexOfFirst { it.position == bodyPart.position }
                    if (navIndex == -1) continue

                    val imagePath = getImagePathForSelection(character, navIndex)
                    if (imagePath.isNullOrBlank() || imagePath in listOf("none", "dice")) continue

                    val layerBitmap = loadBitmapFromAssets(imagePath) ?: continue
                    val scaledBitmap = Bitmap.createScaledBitmap(layerBitmap, width, height, true)
                    canvas.drawBitmap(scaledBitmap, 0f, 0f, null)

                    scaledBitmap.recycle()
                    layerBitmap.recycle()
                }

                Log.d(TAG, "✅ Rendered character: ${character.id}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error rendering character: ${e.message}", e)
                drawErrorPlaceholder(canvas, character.id)
            }

            finalBitmap
        }
    }

    private fun loadBitmapFromAssets(assetPath: String): Bitmap? {
        return try {
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

    private fun getImagePathForSelection(character: CustomModel, navIndex: Int): String? {
        val selection = character.selections.getOrNull(navIndex) ?: return null
        val bodyPart = character.listPath.getOrNull(navIndex) ?: return null

        val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

        if (!hasColor) {
            if (selection.layer == -1) return null

            val position = bodyPart.position ?: 0
            val realImages = bodyPart.listPath.flatMap { colorModel ->
                colorModel.listPath.filter { path ->
                    path.contains("/") && path != "none" && path != "dice"
                }
            }

            val layerImages = mutableListOf<String>()
            if (position != 1) layerImages.add("none")
            layerImages.add("dice")
            layerImages.addAll(realImages)

            val selectedVariant = layerImages.getOrNull(selection.layer) ?: return null
            return if (selectedVariant == "none") "" else selectedVariant

        } else {
            if (selection.color == -1 || selection.layer == -1) return null
            val color = bodyPart.listPath.getOrNull(selection.color) ?: return null
            return color.listPath.getOrNull(selection.layer)
        }
    }

    private fun drawErrorPlaceholder(canvas: Canvas, characterId: String) {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.RED
            textSize = 40f
            textAlign = android.graphics.Paint.Align.CENTER
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

    suspend fun clearQuickRandomCharacters() {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, quickRandomFileName)
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "✅ Deleted quick random JSON")
                }

                val characters = loadQuickRandomCharacters()
                characters.forEach { character ->
                    if (character.imageSave.isNotEmpty()) {
                        imageManager.deleteImage(character.imageSave)
                    }
                }

                _generatingCharacters.value = emptyList()
                Log.d(TAG, "✅ Cleared all quick random characters")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error clearing quick random: ${e.message}", e)
            }
        }
    }

    suspend fun getQuickRandomById(characterId: String): CustomModel? {
        val characters = loadQuickRandomCharacters()
        return characters.find { it.id == characterId }
    }

    suspend fun hasQuickRandomData(): Boolean {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, quickRandomFileName)
            file.exists() && loadQuickRandomCharacters().isNotEmpty()
        }
    }
}