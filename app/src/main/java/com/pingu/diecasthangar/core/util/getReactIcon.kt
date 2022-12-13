package com.pingu.diecasthangar.core.util

import com.pingu.diecasthangar.R

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

fun getReactName(resId: Int): String {
    when (resId) {
        R.drawable.airplane_landing -> {
            return "landing"
        }
        R.drawable.airplane_takeoff -> {
            return "takeoff"
        }
        R.drawable.fire -> {
            return "fire"
        }
        R.drawable.trash_can -> {
            return "trash"
        }
        R.drawable.walk -> {
            return "walking"
        }
        R.drawable.airplane -> {
            return "plane"
        }
        else -> {
            return ""
        }
    }
}