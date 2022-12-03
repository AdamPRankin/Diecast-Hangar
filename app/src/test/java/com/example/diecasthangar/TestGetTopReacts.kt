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

        val expected = arrayListOf(
            Pair("walking", 9),
            Pair("plane", 5),
            Pair("fire", 3)
        )
        assertEquals(expected, getTopReacts(reacts,3))

        val reacts2 =  hashMapOf(
            "plane" to 11,
            "takeoff" to 7,
            "landing" to 0,
            "fire" to 7,
            "walking" to 4
        )

        val expected2 = arrayListOf(
            Pair("plane", 11),
            Pair("takeoff", 7),
            Pair("fire", 7)
        )
        assertEquals(expected2, getTopReacts(reacts2,3))
    }
    @Test
    fun testSmallList(){
        val reacts1 =  hashMapOf(
            "takeoff" to 1,
        )
        val expected1 = arrayListOf(
            Pair("takeoff", 1),
            Pair("dummy3", 0),
            Pair("dummy2",0)
        )
        assertEquals(expected1, getTopReacts(reacts1,3))

        val reacts2 =  hashMapOf<String, Int>(

        )
        val expected2 = arrayListOf(
            Pair("dummy3", 0),
            Pair("dummy2",0),
            Pair("dummy1", 0),
        )
        assertEquals(expected2, getTopReacts(reacts2,3))
    }
}