package no.martials.hvl_ics.service

import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Date
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.TimeZoneRegistryFactory
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.TimeZone
import net.fortuna.ical4j.model.component.VTimeZone
import no.martials.hvl_ics.dto.EventDTO
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.jetbrains.annotations.Contract
import org.springframework.stereotype.Service
import java.io.InputStream
import java.time.LocalDateTime
import java.util.*

@Service
final class CsvService(private val icsService: IcsService) {

    @Contract(pure = true)
    fun readCsv(inputStream: InputStream): List<EventDTO> =
        CSVFormat.Builder.create(CSVFormat.DEFAULT).apply {
            setIgnoreSurroundingSpaces(true)
        }.build().parse(inputStream.reader())
            .drop(1) // Drop header
            .map {
                createEvent(it)
            }

    @Contract(pure = true)
    fun createEvent(record: CSVRecord): EventDTO {
        val start = LocalDateTime.parse("${record[0]}:${record[1]}")
        val slutt = LocalDateTime.parse("${record[2]}:${record[3]}")

        return EventDTO(
            start = start,
            slutt = slutt,
            emne = "${record[5]} - ${record[6]}",
            undervisningsform = record[8],
            learer = record[10].split(","),
            studiested = record[11].split(","),
            rom = record[13].split(","),
            antallPlasser = record[14].split(",").map { it.toInt() }
        )
    }

    @Contract(pure = true)
    fun createIcs(events: List<EventDTO>): Calendar {
        val calendar = Calendar()
        val registry = TimeZoneRegistryFactory.getInstance().createRegistry()
        val timezone = registry.getTimeZone(TimeZone.getDefault().id)
        val tz = timezone.vTimeZone

        for (event in events) {
            val start = DateTime(event.start.toString(), timezone)
            val end = DateTime(event.slutt.toString(), timezone)

            val vEvent = VEvent(start, end, event.summary)
            vEvent.properties.add(tz.timeZoneId)
            calendar.components.add(vEvent)
        }
        return calendar
    }

}
