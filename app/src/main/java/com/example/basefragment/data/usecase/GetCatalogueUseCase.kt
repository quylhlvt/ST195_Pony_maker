package com.example.basefragment.data.usecase

import com.example.basefragment.data.repository.ApiRepository
import com.example.basefragment.utils.Resource
import javax.inject.Inject

class GetDataCustomUseCase @Inject constructor(private val datacustomRepository: ApiRepository) {
    suspend operator fun invoke() = try {
        Resource.success(datacustomRepository.getDataCustom().body())
    } catch (e: Exception) {
        Resource.error(null, e.message)
    }
}