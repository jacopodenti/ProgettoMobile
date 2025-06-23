package com.example.mangiabasta.model

data class Menu(
    val mid: Int,
    val name: String,
    val price: Double,
    val location: Location,
    val imageVersion: Int,
    val shortDescription: String,
    val deliveryTime: Int,
    var imageBase64: String? = null // Campo per l'immagine
)


data class Location(
    val lat: Double,
    val lng: Double
)
