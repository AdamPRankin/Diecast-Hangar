package com.example.diecasthangar.core.util

import com.example.diecasthangar.data.model.Model
import com.example.diecasthangar.data.model.Photo
import com.google.firebase.firestore.DocumentSnapshot

fun modelMapToClass(model: DocumentSnapshot): Model {
    val user: String = model["user"] as String
    val manufacturer: String = model["manufacturer"] as String
    val mould: String = model["mould"].toString() ?: manufacturer
    val scale: String = model["scale"] as String
    val frame: String = model["frame"] as String
    val airline: String = model["airline"] as String
    val photoStrings: ArrayList<String> = model["photos"] as ArrayList<String>
    val livery: String = model["livery"] as String
    val comment: String = model["comment"] as String
    val price: Int = 0
    val id = model.id

    val photos = ArrayList<Photo>()
    for (string in photoStrings){
        photos.add(Photo(remoteUri = string))
    }

    return Model(
        manufacturer,
        mould,
        scale,
        frame,
        airline,
        livery,
        photos,
        comment,
        price,
        id
    )
}



