package com.example.mangiabasta.model

data class OrderStatusResponse(
    val oid: Int,
    val status: String,
    val deliveryLocation: DeliveryLocation,
    val currentPosition: DeliveryLocation,
    val expectedDeliveryTimestamp: String,
    val creationTimestamp: String
)
