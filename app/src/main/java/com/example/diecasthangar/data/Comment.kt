package com.example.diecasthangar.data

import java.util.Date


class Comment(
    val text: String,
    val user: String,
    val username: String,
    val avatarUri: String,
    val id: String,
    val date: Date = Date(),
    val post: String,
    val reactions: MutableMap<String, Int>,
    var totalReactions: Int = 0
){
    fun addReact(name: String, number: Int = 1){
        if (reactions.containsKey(name)) {
            reactions[name] = (reactions[name]!!.plus(number))
            totalReactions += number
        }
    }
}

