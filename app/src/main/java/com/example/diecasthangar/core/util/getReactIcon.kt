package com.example.diecasthangar.core.util

import com.example.diecasthangar.R

fun getReactIcon(type: String): Int {

    if (type == "landing"){
        return R.drawable.airplane_landing
    }
    else if (type == "takeoff"){
        return R.drawable.airplane_takeoff
    }
    else if (type == "fire"){
        return R.drawable.fire
    }
    else if (type == "trash"){
        return R.drawable.trash_can
    }
    else if (type == "walking"){
        return R.drawable.walk
    }
    else if (type == "plane"){
        return R.drawable.airplane
    }
    else {
        return 0
    }

}