package com.example.diecasthangar.core

import com.example.diecasthangar.data.Post
import com.example.diecasthangar.data.User
import com.example.diecasthangar.domain.usecase.remote.getUser
import java.util.Date

class MockPosts(val s: String) {


        fun getPosts(): ArrayList<Post> {

            val user = User("bill",null,"bill@gmail.com")
            val post: Post = Post("title",null, getUser(),id = "1", Date())

            val user2 = User("bill",null,"bill@gmail.com")
            val post2: Post = Post("content content content content content content" +
                    "content content content content content content content content content" +
                    "content content content content content content"
                ,null, getUser(),id = "1", Date())
            val post3: Post = Post("A copypasta is a block of text that is copied and pasted across the Internet by individuals through online forums and social networking websites. Copypastas are said to be similar to spam as they are often used to annoy other users and disrupt online discourse.",null,
                getUser(),id = "1", Date())
            val post4: Post = Post("A copypasta is a block of text that is copied and pasted across the Internet by individuals through online forums and social networking websites. Copypastas are said to be similar to spam as they are often used to annoy other users and disrupt online discourse.",null,
                getUser(),id = "1", Date())
            val post5: Post = Post("A copypasta is a block of text that is copied and pasted across the Internet by individuals through online forums and social networking websites. Copypastas are said to be similar to spam as they are often used to annoy other users and disrupt online discourse.",null,
                getUser(),id = "1", Date())

            val itemList: ArrayList<Post> = ArrayList()
            itemList.add(post)
            itemList.add(post2)
            itemList.add(post3)
            itemList.add(post4)
            itemList.add(post5)

            return itemList
        }
}

