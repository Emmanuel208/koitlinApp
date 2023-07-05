package com.emmanuelobasi.models

data class PlacesItem(
    val gaelic_name: String,
    val id: Int,
    val latitude: Double,
    val location: String,
    val longitude: Double,
    val name: String,
    val place_type_id: Int
)