package no.martials.hvl_ics.service

import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.component.CalendarComponent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.FileOutputStream
import java.io.StringReader
import java.net.URI
import java.net.URL

@Service
class IcsService {

    private final val logger = LoggerFactory.getLogger(IcsService::class.java)

    @Value("\${app.savepath}")
    private lateinit var savepath: String

    @Value("\${app.path.get}")
    private lateinit var getpath: String

    @Value("\${app.basepath}")
    private lateinit var basepath: String

    fun validate(url: URL): Boolean = url.protocol contentEquals "https" and
            url.host.contains("cloud.timeedit.net") and
            url.path.endsWith("ics")

    fun createCalendar(url: URL): Calendar {
        val icsString = readIcsFrom(url)
        val sin = StringReader(icsString)
        val calendar = CalendarBuilder().build(sin)

        calendar.validate()

        calendar.getComponents<CalendarComponent>(Component.VEVENT).forEach { event ->
            val summary = event.getProperty<Property>(Property.SUMMARY)
            val description = event.getProperty<Property>(Property.DESCRIPTION)
            summary.value = fixSummary(summary.value, description.value)
        }
        return calendar
    }

    fun readIcsFrom(url: URL): String {
        var icsString: String
        url.openStream().use { input ->
            logger.debug("Reading data {}", input)
            icsString = String(input.readBytes())
        }
        return icsString
    }

    fun createIcsFile(filename: String, calendar: Calendar): URI {
        if (!filename.endsWith(".ics")) {
            throw IllegalArgumentException("Filename must end with .ics")
        }
        val fout = FileOutputStream("$savepath/$filename")
        val outputter = CalendarOutputter()
        outputter.output(calendar, fout)
        return URI("$basepath/$getpath/$filename")
    }

    fun createIcs(uri: URL): URI {
        val calendar = createCalendar(uri)
        val filename = uri.path.substringAfterLast("/")
        return createIcsFile(filename, calendar)
    }

    private fun fixSummary(summary: String, description: String): String {
        var result = getEmne(summary)
        val type = getType(description)
        if (type != null) {
            result += " $type"
        }
        return result
    }

    fun getEmne(summary: String): String {
        return summary.substringAfter("Emne: ").substringBefore(",")
    }

    fun getType(description: String): String? {
        val descriptionRegex = Regex("videokonferanse|forelesning|lab|øving", RegexOption.IGNORE_CASE)
        val result = descriptionRegex.find(description)
        return result?.value?.replaceFirstChar { it.uppercase() }
    }

}