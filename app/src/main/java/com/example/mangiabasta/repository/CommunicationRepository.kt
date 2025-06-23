package com.example.mangiabasta.repository

import com.example.mangiabasta.model.*
import com.example.mangiabasta.network.ApiService

class CommunicationRepository(private val api: ApiService) {

    suspend fun getSidUid(): SidUidResponse {
        return api.getSidUid()
    }

}
