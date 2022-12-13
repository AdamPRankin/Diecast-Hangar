package com.pingu.diecasthangar.data.model

open class User (
    val id: String,
    val username: String,
    val avatarUri: String,
    var friend: Boolean = false,
    var requestToken: String? = null
){


    override fun equals(other: Any?): Boolean {
        return (other is User)
                && this.id == other.id
    }
}