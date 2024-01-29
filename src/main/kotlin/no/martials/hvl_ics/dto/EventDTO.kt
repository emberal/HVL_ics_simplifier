package no.martials.hvl_ics.dto

import net.fortuna.ical4j.model.Calendar
import org.jetbrains.annotations.Contract
import java.time.LocalDateTime

data class EventDTO(
    val start: LocalDateTime,
    val slutt: LocalDateTime,
    val emne: String,
    val undervisningsform: String,
    val learer: List<String>,
    val studiested: List<String>,
    val rom: List<String>,
    val antallPlasser: List<Int>
) {
    
    @Contract(pure = true)
    fun toCSV(): Calendar {
        throw Exception("TODO")
    }
}
