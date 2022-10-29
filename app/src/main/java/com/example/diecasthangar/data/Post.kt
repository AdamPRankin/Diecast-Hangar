package com.example.diecasthangar.data

import java.util.Date

class Post(
    var text: String,
    var images: ArrayList<String>,
    var user: String,
    var date: Date = Date(),
    var username: String,
    var avatar: String = ""
)

{
}