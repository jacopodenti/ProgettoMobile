package com.example.mangiabasta.model

data class BuyMenuResponse(
    val oid: Int,
    val mid: Int,
    val uid: Int,
    val creationTimestamp: String,
    val status: String,
    val deliveryLocation: Location1,
    val currentPosition: Location1,
    val expectedDeliveryTimestamp: String
)


data class Location1(
    val lat: Double,
    val lng: Double
)
