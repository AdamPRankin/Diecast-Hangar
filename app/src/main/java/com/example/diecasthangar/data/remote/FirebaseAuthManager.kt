package com.example.diecasthangar.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun registerUser(email: String, password: String,username: String): Response<String> {
        return try {
            val uid = mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                val user = it.result.user
                initializeUserData(user!!.uid, username)
            }.await().user!!.uid
            Response.Success(uid)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    private fun initializeUserData(userId: String, username: String) {
        val userFriendsRef = db.collection("userfriends").document(userId)
        val newUserFriends = hashMapOf(
            "friends" to arrayListOf<String>()
        )
        userFriendsRef.set(newUserFriends)

        val userDataRef = db.collection("userdata").document(userId)
        val newUserData = hashMapOf(
            "avatar" to "",
            "username" to username
        )
        userDataRef.set(newUserData)
    }
}