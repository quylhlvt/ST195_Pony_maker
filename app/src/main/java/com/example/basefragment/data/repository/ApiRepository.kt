package com.example.basefragment.data.repository

import com.example.basefragment.core.service.ApiService
import com.example.basefragment.data.model.custom.CustomModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class ApiRepository @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * Lấy danh sách characters từ API
     */
    suspend fun getCharacters(): Result<List<CustomModel>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCharacters()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lấy chi tiết character theo ID
     */
    suspend fun getCharacterById(id: String): Result<CustomModel> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCharacterById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}