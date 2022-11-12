package com.example.diecasthangar.core.util

import com.example.diecasthangar.data.Post
import java.util.*

fun loadingDummyPost(): Post {

    return Post(
        "Squeek is loading the posts as fast as he can...", arrayListOf(), "Mr. Loading",
        Date(), "Flyin' Squeek The Post Loader",
        "https://i.gyazo.com/02b2c623a812f221477160f3041f486a.png", "123", arrayListOf(), hashMapOf()
    )
}