package com.example.diecasthangar

import com.example.diecasthangar.data.Comment
import com.example.diecasthangar.data.Post
import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList
import kotlin.test.assertEquals


class TestPost {
    @Test
    fun testPost() {
        val date = Date()
        val post = Post(
            text = "hello",
            images = ArrayList(),
            user = "s2sd4af372rh35435",
            date = date,
            username = "jimmy",
            avatar = "www.fake-image.com/img",
            id = "345",
            comments = ArrayList(),
            reactions = hashMapOf(
                "plane" to 0,
                "takeoff" to 0,
                "landing" to 0
            )

        )

        assertEquals("hello", post.text)
        assertEquals("s2sd4af372rh35435", post.user)
        assertEquals(date, post.date)
        assertEquals("jimmy", post.username)
        assertEquals("www.fake-image.com/img", post.avatar)
        assertEquals("345", post.id)
        assertEquals(
            hashMapOf(
                "plane" to 0,
                "takeoff" to 0,
                "landing" to 0
            ), post.reactions
        )
    }

    @Test
    fun testAddReaction(){
        val date = Date()
        val post = Post(
            text = "hello",
            images = ArrayList(),
            user = "s2sd4af372rh35435",
            date = date,
            username = "jimmy",
            avatar = "www.fake-image.com/img",
            id = "345",
            comments = ArrayList(),
            reactions = hashMapOf(
                "plane" to 0,
                "takeoff" to 0,
                "landing" to 0
            )
        )
        post.addReact("plane")
        assertEquals(1,post.reactions["plane"])
        post.addReact("takeoff", 7)
        assertEquals(7,post.reactions["takeoff"])
    }
}