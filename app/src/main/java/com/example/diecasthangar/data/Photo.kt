package com.example.diecasthangar.data

import android.net.Uri
import java.util.Date

data class Photo(
    var localUri: Uri? = null,
    var remoteUri: String = "",
    var date: Date = Date(),
    var id: String = ""
){

}
