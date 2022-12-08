package com.example.diecasthangar.data.remote

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

fun getUser(): FirebaseUser? {

    return Firebase.auth.currentUser
}