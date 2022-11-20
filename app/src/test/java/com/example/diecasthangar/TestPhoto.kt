package com.example.diecasthangar

import com.example.diecasthangar.data.model.Photo
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class TestPhoto {

    @Test
    fun testPhoto() {
        var date = Date()
        val photo = Photo()

        assertEquals("",photo.remoteUri)
        assertEquals(null, photo.localUri)
        assertEquals(date.time.toDouble(),photo.date.time.toDouble(), 5.0)
        assertEquals("", photo.id)
        photo.date = date
        assertEquals(date,photo.date)


    }
}


