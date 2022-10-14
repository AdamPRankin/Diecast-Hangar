package com.example.diecasthangar.core

import com.example.diecasthangar.data.Post
import com.example.diecasthangar.data.User

class MockPosts(val s: String) {


        fun getPosts(): ArrayList<Post> {

            val user = User("bill",null,3)
            val post: Post = Post("title",null,"title",user,3, null)

            val user2 = User("bill",null,3)
            val post2: Post = Post("content content content content content content" +
                    "content content content content content content content content content" +
                    "content content content content content content"

                ,null,"title2",user2,3, null)
            val post3: Post = Post("content content content",null,"title3",user,3, null)
            val post4: Post = Post("content content content",null,"title4",user2,3, null)
            val post5: Post = Post("content content content ",null,"title5",user,3, null)

            val itemList: ArrayList<Post> = ArrayList()
            itemList.add(post)
            itemList.add(post2)
            itemList.add(post3)
            itemList.add(post4)
            itemList.add(post5)

            return itemList
        }
}

