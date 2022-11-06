package com.example.diecasthangar

import android.net.Uri
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.domain.Response
import org.junit.Test
import kotlin.test.assertEquals

class TestResponse {

    @Test
    fun testSuccess(){
        val boolResponse = Response.Success(true)
        assertEquals(Response.Success(true),boolResponse)
    }

    @Test
    fun testFailure(){
        val nullError = NullPointerException()
        val failResponse = Response.Failure(nullError)
        assertEquals(Response.Failure(nullError),failResponse)
    }

    @Test
    fun getSuccessData(){
        val boolResponse = Response.Success(true)
        assertEquals(boolResponse.data,true)
        val stringResponse = Response.Success("test")
        assertEquals(stringResponse.data,"test")
        val posts = ArrayList<Post>()
        val postListResponse = Response.Success(posts)
        assertEquals(posts, postListResponse.data)
    }

    @Test
    fun testFailureData(){
        val error = java.lang.IndexOutOfBoundsException()
        val outOfBoundsFailure = Response.Failure(error)
        assertEquals(error,outOfBoundsFailure.e)
        val nullError = NullPointerException()
        val nullFailure = Response.Failure(nullError)
        assertEquals(nullError,nullFailure.e)
    }


}