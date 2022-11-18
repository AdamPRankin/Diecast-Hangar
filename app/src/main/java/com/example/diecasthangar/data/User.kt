package com.example.diecasthangar.data

open class User (
    val id: String,
    val username: String,
    val avatarUri: String,
    var friend: Boolean = false,
    var requestToken: String? = null
    ){

    fun getRequest(): String? {
        if (!friend){
            return requestToken
        }
        return ""
    }
}