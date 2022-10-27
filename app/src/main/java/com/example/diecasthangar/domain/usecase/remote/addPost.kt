package com.example.diecasthangar.domain.usecase.remote

import android.media.Image
import com.example.diecasthangar.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.*

fun addPost(text: String,
            images: List<Image>?,
            title: String,
            user: FirebaseUser,
            id: Int,
            date: Date?
) {
    val auth = FirebaseAuth.getInstance()




}