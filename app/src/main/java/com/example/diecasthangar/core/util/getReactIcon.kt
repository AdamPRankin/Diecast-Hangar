package com.example.diecasthangar.core.util

import com.example.diecasthangar.R

fun getReactIcon(type: String): Int {

    when (type) {
        "landing" -> {
            return R.drawable.airplane_landing
        }
        "takeoff" -> {
            return R.drawable.airplane_takeoff
        }
        "fire" -> {
            return R.drawable.fire
        }
        "trash" -> {
            return R.drawable.trash_can
        }
        "walking" -> {
            return R.drawable.walk
        }
        "plane" -> {
            return R.drawable.airplane
        }
        else -> {
            return 0
        }
    }

}