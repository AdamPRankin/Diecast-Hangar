package com.example.diecasthangar.data.remote.postpag

import com.example.diecasthangar.data.model.Post

class Operation(post: Post, type: String) {
    var post: Post
    var type: String

    init {
        this.post = post
        this.type = type
    }
}