package com.example.diecasthangar.data

import android.net.Uri

open class Model(
    val manufacturer: String,
    val mould: String = manufacturer,
    val scale: String,
    val frame: String,
    val airline: String,
    val livery: String = "regular livery",
    val photos: ArrayList<Photo>,
    val comment: String,
    val price: Int,
    val id: String?
){

}
