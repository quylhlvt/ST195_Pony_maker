package com.example.basefragment.utils

import com.example.basefragment.data.datalocal.manager.AppDataManager
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.data.model.custom.SelectionPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class QuickRandomEngine @Inject constructor(
    private val appDataManager: AppDataManager
) {

    // Cache kết quả
    private var cachedCharacters: List<CustomModel>? = null

    /**
     * Generate nếu chưa có cache
     */
    suspend fun generateIfNeeded(total: Int = 30): List<CustomModel> {
        cachedCharacters?.let { return it }

        val templates = appDataManager.templates.value
        if (templates.isEmpty()) return emptyList()

        val result = withContext(Dispatchers.Default) {
            List(total) {
                val template = templates.random()
                generateSingleCharacter(template)
            }
        }

        cachedCharacters = result
        return result
    }

    /**
     * Lấy lại cache (không generate)
     */
    fun getCached(): List<CustomModel>? = cachedCharacters

    /**
     * Clear cache để regenerate
     */
    fun clear() {
        cachedCharacters = null
    }

    /**
     * Generate 1 character từ template
     */
    private fun generateSingleCharacter(template: CustomModel): CustomModel {

        val randomSelections = template.listPath.mapIndexed { index, bodyPart ->

            // Random color từ 1 → hết
            val colorIndex = if (bodyPart.listPath.size > 1) {
                (1 until bodyPart.listPath.size).random()
            } else {
                -1
            }

            // Random layer từ 1 → hết
            val layerIndex =
                if (colorIndex >= 0 && bodyPart.listPath[colorIndex].listPath.size > 1) {
                    (1 until bodyPart.listPath[colorIndex].listPath.size).random()
                } else {
                    -1
                }

            SelectionPart(
                nav = index,
                color = colorIndex,
                layer = layerIndex
            )
        }

        return template.copy(
            id = UUID.randomUUID().toString(),
            selections = randomSelections as ArrayList<SelectionPart>
        )
    }
}
