package com.example.diecasthangar

import com.example.diecasthangar.data.model.Photo
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class TestPhoto {

    @Test
    fun testPhoto() {
        val date = Date()
        val photo = Photo()

        assertEquals("",photo.remoteUri)
        assertEquals(null, photo.localUri)
        assertEquals("", photo.id)
        photo.date = date
        assertEquals(date,photo.date)


    }
}


