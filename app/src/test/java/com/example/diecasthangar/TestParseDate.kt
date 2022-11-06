package com.example.diecasthangar

import android.icu.text.SimpleDateFormat
import com.example.diecasthangar.core.util.parseDate
import com.google.type.Date
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class TestParseDate {

    private val fakeToday = LocalDateTime.parse("2022-11-05T13:41:05")

    val formatter = SimpleDateFormat("EE MMMM dd y H:mm a")
    val text = "Sat Nov 05 13:41:05 MDT 2022"





    @Test
    fun testParseDate () {

        //assertEquals("MMMM dd y",parseDate(fakeToday,false,fakeToday,),)

    }

}


