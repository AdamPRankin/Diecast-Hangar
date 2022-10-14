package com.example.diecasthangar.data

import android.media.Image
import java.util.Date

class Post(
    var text: String,
    var images: List<Image>?,
    var title: String,
    var user: User,
    var id: Int,
    var date: Date?
)
{


}