package com.example.diecasthangar.core.util

import com.example.diecasthangar.R
import com.example.diecasthangar.data.model.Model
import com.example.diecasthangar.data.model.Photo

fun getDummyModel(): Model {

    return Model(
        "123",
        "ng",
        "ng",
        "1:400",
        "b747",
        "air france",
        "regular",
        arrayListOf(),
        "comment",
        1,
        "dummy_model_id"
    )


}



fun Photo.loadDummy() {
    fun returnDummyPhoto(): Int {
        return R.drawable.image_edit
    }
}