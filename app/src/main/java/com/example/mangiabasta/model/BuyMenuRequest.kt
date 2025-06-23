package com.example.mangiabasta.model

data class BuyMenuRequest(
    val sid: String,
    val deliveryLocation: DeliveryLocation
)

data class DeliveryLocation(
    val lat: Double,
    val lng: Double
)
