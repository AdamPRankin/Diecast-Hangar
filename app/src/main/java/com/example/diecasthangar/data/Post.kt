package com.example.diecasthangar.data

import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import java.util.Date

class Post(
    var text: String,
    var images: List<Uri>?,
    var user: FirebaseUser?,
    var id: String,
    var date: Date = Date(),
)
{


}