package com.example.basefragment.data.repository

import com.example.basefragment.data.datalocal.api.DataCustomApi
import com.example.basefragment.data.model.custom.CustomModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * Repository chịu trách nhiệm gọi API liên quan đến CustomModel (characters)
 */
@Singleton
class ApiRepository @Inject constructor(
    private val dataCustomApi: DataCustomApi) {
    suspend fun getDataCustom() = withContext(Dispatchers.IO) {
        dataCustomApi.getData()
    }
}
