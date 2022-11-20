package com.example.diecasthangar

import com.example.diecasthangar.data.model.User
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TestUser {


    @Test
    fun testUser(){
        val newUser = User(
            id = "sf2gk4sgs4kgd3gsd",
            username = "Jim",
            avatarUri = "",
            friend = false,
            requestToken = "232524726"
        )

        assertEquals("sf2gk4sgs4kgd3gsd", newUser.id)
        assertEquals("Jim", newUser.username)
        assertFalse(newUser.friend)
        assertEquals("232524726", newUser.requestToken)
    }
}


