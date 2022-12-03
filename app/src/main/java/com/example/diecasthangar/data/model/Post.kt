package com.example.diecasthangar.data.model

import com.example.diecasthangar.data.remote.getUser
import java.util.Date

data class Post(
    var text: String = "",
    var images: ArrayList<Photo> = arrayListOf(),
    var user: String = getUser()!!.uid,
    var date: Date = Date(),
    var username: String = "",
    var avatar: String = "",
    val id: String = "",
    var comments: ArrayList<Comment>? = arrayListOf(),
    val reactions: MutableMap<String, Int> = mutableMapOf()
){
    fun addReact(name: String, number: Int = 1){
        if (reactions.containsKey(name)) {
            reactions[name] = (reactions[name]!!.plus(number))
        }
    }

    override fun equals(other: Any?): Boolean {
        return (other is Post)
                && this.id == other.id

    }

}

