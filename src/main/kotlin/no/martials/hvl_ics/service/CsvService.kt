package no.martials.hvl_ics.service

import no.martials.hvl_ics.dto.EventDTO
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.jetbrains.annotations.Contract
import org.springframework.stereotype.Service
import java.io.InputStream
import java.time.LocalDateTime

@Service
final class CsvService {

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

}
