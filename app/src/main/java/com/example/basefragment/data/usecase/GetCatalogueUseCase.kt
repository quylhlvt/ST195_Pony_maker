package com.example.basefragment.data.usecase

import com.example.basefragment.data.repository.ApiRepository
import com.example.basefragment.utils.Resource
import javax.inject.Inject

class GetCatalogueUseCase @Inject constructor(private val catalogueRepository: ApiRepository) {
    suspend operator fun invoke() = try {
        Resource.success(catalogueRepository.getCatalogue().body())
    } catch (e: Exception) {
        Resource.error(null, e.message)
    }
}