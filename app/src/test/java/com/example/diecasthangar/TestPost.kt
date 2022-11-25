package com.example.diecasthangar

import com.example.diecasthangar.data.model.Photo
import com.example.diecasthangar.data.model.Post
import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


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

    @Test
    fun testEquality(){
        val date = Date()
        val post1 = Post(
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
        val post2 = Post(
            text = "jo",
            images = arrayListOf(Photo()),
            user = "s2sd4af372rh35435",
            date = date,
            username = "jimmy",
            avatar = "www.fake-images.org/img2",
            id = "345",
            comments = ArrayList(),
            reactions = hashMapOf(
                "plane" to 4,
                "takeoff" to 3,
                "landing" to 1
            )
        )
        assertEquals(post1,post2)
        val post3 = Post(
            text = "jo",
            images = arrayListOf(Photo()),
            user = "s2sd4af372rh35435",
            date = date,
            username = "jimmy",
            avatar = "www.fake-images.org/img2",
            id = "3456",
            comments = ArrayList(),
            reactions = hashMapOf(
                "plane" to 4,
                "takeoff" to 3,
                "landing" to 1
            )
        )
        assertNotEquals(post3, post2)
        assertNotEquals(post3, post1)
    }
}