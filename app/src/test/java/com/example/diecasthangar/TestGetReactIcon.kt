package com.pingu.diecasthangar

import com.pingu.diecasthangar.core.util.getReactIcon
import org.junit.Test
import kotlin.test.assertEquals

class TestGetReactIcon {


    @Test
    fun testGetReactIcon(){
        assertEquals(R.drawable.airplane_landing, getReactIcon("landing"))
        assertEquals(R.drawable.trash_can, getReactIcon("trash"))
        assertEquals(R.drawable.walk, getReactIcon("walking"))
        assertEquals(R.drawable.fire, getReactIcon("fire"))
        assertEquals(R.drawable.airplane, getReactIcon("plane"))
        assertEquals(0, getReactIcon("dummy1"))
        assertEquals(0, getReactIcon("hd47777jj"))
    }
}