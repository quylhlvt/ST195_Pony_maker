package com.example.basefragment.data.datalocal.api
import com.example.basefragment.data.model.api.CharacterResponse
import com.example.basefragment.data.model.api.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface DataCustomApi {
    @GET("api/ST195_PonyOC")
    suspend fun getData(): Response<CharacterResponse>
}