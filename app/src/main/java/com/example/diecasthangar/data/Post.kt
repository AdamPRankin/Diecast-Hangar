package com.example.diecasthangar.data

import java.util.Date

class Post(
    var text: String,
    var images: ArrayList<String>,
    var user: String,
    var date: Date = Date(),
    var username: String,
    var avatar: String = "",
    val id: String = "",
    val comments: ArrayList<Comment>,
    val reactions: MutableMap<String, Int>
){
    fun addReact(name: String, number: Int = 1){
        if (reactions.containsKey(name)) {
            reactions[name] = (reactions[name]!!.plus(number))
        }
    }

}

