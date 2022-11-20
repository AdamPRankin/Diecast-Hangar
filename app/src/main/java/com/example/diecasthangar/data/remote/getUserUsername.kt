package com.example.diecasthangar.domain.remote

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

fun getUserUsername(): String? {
    var name: String? = "User"
    val user = Firebase.auth.currentUser
    user?.let {
        for (profile in it.providerData) {
            name = profile.displayName
        }
    }
    return name
}