package com.example.basefragment.data.repository

import android.util.Log
import com.example.basefragment.data.callapi.ApiHelper
import com.example.basefragment.data.model.CharacterResponse
import com.example.basefragment.utils.CONST
import com.example.basefragment.utils.DataHelper
import javax.inject.Inject

class ApiRepository @Inject constructor(private val apiHelper: ApiHelper) {
    suspend fun getFigure(): CharacterResponse? {
        try {
            CONST.BASE_URL = CONST.BASE_URL_1
            return apiHelper.apiMermaid1.getAllData()
        } catch (e: Exception) {
            Log.d(DataHelper.TAG, "getFigure: $e")
            try {
                CONST.BASE_URL = CONST.BASE_URL_2
                return apiHelper.apiMermaid2.getAllData()
            } catch (e: Exception) {
                Log.d(DataHelper.TAG, "getFigure: $e")
                return null
            }
        }
    }

}