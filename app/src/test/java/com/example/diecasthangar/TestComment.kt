package com.example.diecasthangar

import com.example.diecasthangar.data.Comment
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class TestComment {

    @Test
    fun testComment() {
        val date = Date()
        val comment = Comment(
            text = "hello",
            user = "s2sd4af372rh35435",
            username = "jimmy",
            date = date,
            post = "12345",
            avatarUri = "www.fake-image.com/img",
            id = "345",
            reactions = hashMapOf(
                "plane" to 0,
                "takeoff" to 0,
                "landing" to 0
            ),
            totalReactions = 0
        )

        assertEquals("hello", comment.text)
        assertEquals("s2sd4af372rh35435", comment.user)
        assertEquals(date, comment.date)
        assertEquals("jimmy", comment.username)
        assertEquals("www.fake-image.com/img", comment.avatarUri)
        assertEquals("345", comment.id)
        assertEquals(
            hashMapOf(
                "plane" to 0,
                "takeoff" to 0,
                "landing" to 0
            ), comment.reactions
        )
    }

    @Test
    fun testAddReaction(){
        val date = Date()
        val comment = Comment(
            text = "hello",
            user = "s2sd4af372rh35435",
            username = "jimmy",
            date = date,
            post = "12345",
            avatarUri = "www.fake-image.com/img",
            id = "345",
            reactions = hashMapOf(
                "plane" to 0,
                "takeoff" to 0,
                "landing" to 0
            ),
            totalReactions = 0
        )
        comment.addReact("plane")
        assertEquals(1,comment.reactions["plane"])
        assertEquals(1,comment.totalReactions)
        comment.addReact("takeoff", 7)
        assertEquals(7,comment.reactions["takeoff"])
        assertEquals(8,comment.totalReactions)
    }
}