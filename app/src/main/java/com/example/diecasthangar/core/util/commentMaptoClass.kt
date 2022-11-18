package com.example.diecasthangar.core.util

import com.example.diecasthangar.data.Comment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot

fun commentMapToClass(comment: QueryDocumentSnapshot): Comment {
    val text: String = comment["text"] as String
    val timestamp: Timestamp = comment["date"] as Timestamp
    val user: String = comment["user"].toString()
    val username: String = comment["username"].toString()
    val avatar: String = comment["avatar"].toString()
    val id = comment["id"].toString()
    val postId: String = comment["post"] as String
    val reactions: MutableMap<String, Int> = comment["reactions"]
            as MutableMap<String, Int>

    return Comment(
        text,
        user,
        username,
        avatar,
        id,
        timestamp.toDate(),
        postId,
        reactions
    )


}

