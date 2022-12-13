package com.pingu.diecasthangar.core.util

import com.pingu.diecasthangar.data.model.Comment
import com.pingu.diecasthangar.data.model.Photo
import com.pingu.diecasthangar.data.model.Post
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*
import kotlin.collections.ArrayList
/**
 * Given a document snapshot return the contained data in class form
 *
 *
 * @param doc document snapshot with the data
 * @return Post model object
 */
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
    val announcement = doc.get("announcement")
    val isAnnouncement: Boolean = if (announcement != null){
        announcement as Boolean
    } else {
        false
    }


    val photos: List<Photo> = imageUris.map { Photo(remoteUri = it) }

    return Post(
        text, photos as ArrayList<Photo>, user, timestamp.toDate(), username, avatar, id,
        comments, reactions, isAnnouncement
    )


}

