@file:Suppress("EqualsOrHashCode")

package com.example.diecasthangar.data.model

@Suppress("EqualsOrHashCode")
data class Model(
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
    val id: String?,
    val reg: String


){

    override fun equals(other: Any?): Boolean {
        return (other is Model)
                && this.id == other.id
                && this.photos == other.photos
                && this.mould == other.mould
                && this.scale == other.scale
                && this.livery == other.livery
                && this.comment == other.comment
                && this.airline  == other.airline
                && this.frame == other.frame
                && this.comment == other.comment
                && this.reg == other.reg


    }
}


