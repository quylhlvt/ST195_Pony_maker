package com.example.basefragment.data.callapi

import com.example.basefragment.data.model.CharacterResponse
import retrofit2.http.GET

interface ApiMermaid {
    @GET("api/api/ST195_PonyOC")
    suspend fun getAllData(): CharacterResponse
}