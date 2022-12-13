package com.pingu.diecasthangar.core.util

import com.pingu.diecasthangar.data.model.Post
import java.util.*

fun loadingDummyPost(): Post {

    return Post(
        "Squeek is loading the posts as fast as he can...", arrayListOf(), "123",
        Date(), "Flyin' Squeek The Post Loader",
        "https://firebasestorage.googleapis.com/v0/b/diecast-hangar.appspot.com/o/images%2Fsqueek.png?alt=media&token=1388c527-42b1-4182-8f1a-b796ec4766b7",
        "123", arrayListOf(), hashMapOf()
    )
}