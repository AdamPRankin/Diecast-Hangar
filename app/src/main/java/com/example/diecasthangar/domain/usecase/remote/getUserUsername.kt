package com.example.diecasthangar.domain.usecase.remote

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
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