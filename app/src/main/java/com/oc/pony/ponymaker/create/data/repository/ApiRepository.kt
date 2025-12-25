package com.oc.pony.ponymaker.create.data.repository

import android.util.Log
import com.oc.pony.ponymaker.create.data.callapi.ApiHelper
import com.oc.pony.ponymaker.create.data.model.CharacterResponse
import com.oc.pony.ponymaker.create.utils.CONST.BASE_URL
import com.oc.pony.ponymaker.create.utils.CONST.BASE_URL_1
import com.oc.pony.ponymaker.create.utils.CONST.BASE_URL_2
import com.oc.pony.ponymaker.create.utils.DataHelper.TAG
import javax.inject.Inject

class ApiRepository @Inject constructor(private val apiHelper: ApiHelper) {
    suspend fun getFigure(): CharacterResponse? {
        try {
            BASE_URL = BASE_URL_1
            return apiHelper.apiMermaid1.getAllData()
        } catch (e: Exception) {
            Log.d(TAG, "getFigure: $e")
            try {
                BASE_URL = BASE_URL_2
                return apiHelper.apiMermaid2.getAllData()
            } catch (e: Exception) {
                Log.d(TAG, "getFigure: $e")
                return null
            }
        }
    }

}
