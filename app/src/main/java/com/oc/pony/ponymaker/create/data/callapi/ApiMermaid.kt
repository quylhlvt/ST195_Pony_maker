package com.oc.pony.ponymaker.create.data.callapi

import retrofit2.http.GET

interface ApiMermaid {
    @GET("api/ST195_PonyOC")
    suspend fun getAllData(): com.oc.pony.ponymaker.create.data.model.CharacterResponse
}