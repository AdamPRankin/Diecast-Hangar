package com.example.diecasthangar.domain.usecase.remote

import android.content.ContentValues
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

fun registerUser(email: String, password: String,username: String): Boolean {
    val auth = Firebase.auth
    var complete: Boolean = false
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener() { task ->
        if (task.isSuccessful) {
            complete = true
            Log.d(ContentValues.TAG, "createUserWithEmail:success")
            val user = Firebase.auth.currentUser
            val profileUpdates = userProfileChangeRequest {
                displayName = username
                }
                user!!.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(ContentValues.TAG, "username updated")
                        complete = true
                    }
                }
        } else {
            // If sign in fails, display a message to the user.
            Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)

                }
            }
    return complete




}