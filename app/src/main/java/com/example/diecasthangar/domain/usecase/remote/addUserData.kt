package com.example.diecasthangar.domain.usecase.remote

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase


fun addUserData(username: String, photoString: String) {

    val user = Firebase.auth.currentUser
    var userId = user!!.uid
    var userPhotoUri: Uri? = user.photoUrl


    val profileUpdates = userProfileChangeRequest {
        displayName = username
        photoUri = Uri.parse(photoString)
    }
    user.updateProfile(profileUpdates).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d(TAG, "User profile updated.")
        }
    }
}