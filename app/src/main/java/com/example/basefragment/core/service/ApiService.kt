package com.example.basefragment.core.service

import com.example.basefragment.data.model.custom.CustomModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    /**
 * Lấy danh sách tất cả characters từ server
 */
@GET("characters")
suspend fun getCharacters(): Response<List<CustomModel>>

    /**
     * Lấy chi tiết 1 character theo ID
     */
    @GET("characters/{id}")
    suspend fun getCharacterById(@Path("id") characterId: String): Response<CustomModel>

    /**
     * Lấy danh sách background images
     */
    @GET("backgrounds")
    suspend fun getBackgrounds(): Response<List<String>>

    /**
     * Lấy danh sách stickers/items
     */
    @GET("stickers")
    suspend fun getStickers(): Response<List<String>>
}
