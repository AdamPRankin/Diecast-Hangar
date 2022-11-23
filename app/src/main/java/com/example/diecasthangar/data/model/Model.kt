package com.example.diecasthangar.data.model

open class Model(
    val userID: String,
    val manufacturer: String,
    val mould: String = manufacturer,
    val scale: String,
    val frame: String,
    val airline: String,
    val livery: String = "regular livery",
    var photos: ArrayList<Photo>,
    val comment: String,
    val price: Int = 0,
    val id: String?
)
