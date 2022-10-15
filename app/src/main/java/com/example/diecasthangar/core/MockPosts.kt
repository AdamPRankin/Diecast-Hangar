package com.example.diecasthangar.core

import com.example.diecasthangar.data.Post
import com.example.diecasthangar.data.User

class MockPosts(val s: String) {


        fun getPosts(): ArrayList<Post> {

            val user = User("bill",null,"bill@gmail.com")
            val post: Post = Post("title",null,"title",user,3, null)

            val user2 = User("bill",null,"bill@gmail.com")
            val post2: Post = Post("content content content content content content" +
                    "content content content content content content content content content" +
                    "content content content content content content"

                ,null,"title2",user2,3, null)
            val post3: Post = Post("A copypasta is a block of text that is copied and pasted across the Internet by individuals through online forums and social networking websites. Copypastas are said to be similar to spam as they are often used to annoy other users and disrupt online discourse.",null,"title3",user,3, null)
            val post4: Post = Post("A copypasta is a block of text that is copied and pasted across the Internet by individuals through online forums and social networking websites. Copypastas are said to be similar to spam as they are often used to annoy other users and disrupt online discourse.",null,"title4",user2,3, null)
            val post5: Post = Post("A copypasta is a block of text that is copied and pasted across the Internet by individuals through online forums and social networking websites. Copypastas are said to be similar to spam as they are often used to annoy other users and disrupt online discourse.",null,"title5",user,3, null)

            val itemList: ArrayList<Post> = ArrayList()
            itemList.add(post)
            itemList.add(post2)
            itemList.add(post3)
            itemList.add(post4)
            itemList.add(post5)

            return itemList
        }
}

