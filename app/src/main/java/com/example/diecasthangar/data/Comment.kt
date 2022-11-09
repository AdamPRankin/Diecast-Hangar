package com.example.diecasthangar.data

import java.time.LocalDate
import java.util.*

class Comment(
    val text: String,
    val user: String,
    val username: String,
    val avatarUri: String,
    val id: String,
    val Date: Date = Date(),
    val post: String,
    val reactions: MutableMap<String, Int>
){
    fun addReact(name: String, number: Int = 1){
        if (reactions.containsKey(name)) {
            reactions[name] = (reactions[name]!!.plus(number))
        }
    }
}

