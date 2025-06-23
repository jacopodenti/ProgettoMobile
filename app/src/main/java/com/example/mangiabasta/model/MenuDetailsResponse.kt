package com.example.mangiabasta.model

import java.io.Serializable



data class MenuDetailsResponse(
    val mid: Int,
    val name: String,
    val price: Double,
    val imageBase64: String?, // Stringa Base64 corretta per l'immagine
    val shortDescription: String,
    val deliveryTime: Int,
    val longDescription: String
) : Serializable


