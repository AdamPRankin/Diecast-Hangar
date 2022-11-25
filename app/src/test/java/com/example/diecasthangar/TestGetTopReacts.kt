package com.example.diecasthangar

import com.example.diecasthangar.core.util.getTopReacts
import org.junit.Test
import kotlin.test.assertEquals

class TestGetTopReacts {

    @Test
    fun testGetTopReacts(){
        val reacts =  hashMapOf(
            "plane" to 5,
            "takeoff" to 2,
            "landing" to 0,
            "fire" to 3,
            "walking" to 9
        )

        val expecte = hashMapOf(
            "walking" to 9,
            "plane" to 5,
            "fire" to 3
        )

        val expected = arrayListOf(
            Pair("walking", 9),
            Pair("plane", 5),
            Pair("fire", 3)
        )
        assertEquals(expected, getTopReacts(reacts,3))


    }
}