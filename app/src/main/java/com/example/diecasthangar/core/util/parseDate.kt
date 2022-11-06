package com.example.diecasthangar.core.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun parseDate(date: Date, fullDateFlag: Boolean = false,now: LocalDateTime = LocalDateTime.now()): String? {

    val localDateTime: LocalDateTime =
        date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

    if (fullDateFlag){
        return localDateTime.format(DateTimeFormatter.ofPattern("EE MMMM dd y H:mm a"))
    }

    else if ((localDateTime.dayOfYear == now.dayOfYear)&&(localDateTime.year==now.year)){
       return localDateTime.format(DateTimeFormatter.ofPattern("H:mm a"))
    }
    else if ((localDateTime.month == now.month)&&(localDateTime.year==now.year)) {
        return localDateTime.format(DateTimeFormatter.ofPattern("EE MM dd"))
    }
    else if (localDateTime.year == now.year) {
        return localDateTime.format(DateTimeFormatter.ofPattern("MMMM dd"))
    }

    else if (localDateTime.year < now.year){
        return localDateTime.format(DateTimeFormatter.ofPattern("MMMM dd y"))
    }
    return localDateTime.format(DateTimeFormatter.ofPattern("MMMM dd y"))
}


