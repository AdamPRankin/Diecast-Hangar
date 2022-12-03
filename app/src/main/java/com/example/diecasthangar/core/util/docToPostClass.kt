package com.example.diecasthangar.core.util

import com.example.diecasthangar.data.model.Comment
import com.example.diecasthangar.data.model.Photo
import com.example.diecasthangar.data.model.Post
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*
import kotlin.collections.ArrayList

fun docToPostClass(doc: DocumentSnapshot): Post {
    val imageUris: ArrayList<String> = doc.get("images") as ArrayList<String>
    val text: String = doc.get("text") as String
    val timestamp: Timestamp = (doc.getTimestamp("date") ?: Timestamp(Date()))
    val user: String = doc.get("user").toString()
    val username: String = doc.get("username").toString()
    val avatar: String = doc.get("avatar").toString()
    val id = doc.id
    val comments: ArrayList<Comment> = ArrayList()
    val reactions: MutableMap<String, Int> =
        doc.get("reactions") as MutableMap<String, Int>

    val photos: List<Photo> = imageUris.map { Photo(remoteUri = it) }

    return Post(
        text, photos as ArrayList<Photo>, user, timestamp.toDate(), username, avatar, id,
        comments, reactions
    )


}

