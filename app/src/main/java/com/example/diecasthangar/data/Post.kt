package com.example.diecasthangar.data

import com.example.diecasthangar.domain.usecase.remote.getUser
import java.util.Date

class Post(
    var text: String = "",
    var images: ArrayList<String> = arrayListOf(),
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

}

