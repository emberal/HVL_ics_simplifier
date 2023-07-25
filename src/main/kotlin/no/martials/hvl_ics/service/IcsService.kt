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

@Service
class IcsService {

    private final val logger = LoggerFactory.getLogger(IcsService::class.java)

    // TODO fetch at regular intervals
    private final val url =
        "https://cloud.timeedit.net/hvl/web/studbergen/ri6305Q64k59u6QZQtQn270QZQ8QY43dZ6317Z0y6580CwtZ00AZ87D9690F55D7EAEBF27863FFDA6.ics"

    private lateinit var ics: Calendar

    @Value("\${app.savepath}")
    private lateinit var savepath: String

    init {
        var icsString: String
        logger.debug("Reading ics from url: {}", url)
        URI(url).toURL().openStream().use { input ->
            logger.debug("Reading data {}", input)
            icsString = String(input.readBytes())
        }
        val sin = StringReader(icsString)
        val builder = CalendarBuilder()
        val calendar = builder.build(sin)

        calendar.validate()

        calendar.getComponents<CalendarComponent>(Component.VEVENT).forEach { event ->
            val summary = event.getProperty<Property>(Property.SUMMARY)
            val description = event.getProperty<Property>(Property.DESCRIPTION)
            summary.value = fixSummary(summary.value, description.value)
        }

        logger.debug("Created ics calendar: {}", calendar)
        ics = calendar
    }

    fun createIcs(filename: String): URI {
        val fout = FileOutputStream("$savepath/$filename.ics")
        val outputter = CalendarOutputter()
        outputter.output(ics, fout)
        return URI("/$filename.ics")
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
        val descriptionRegex: Regex = Regex("videokonferanse|forelesning|lab|øving", RegexOption.IGNORE_CASE)
        val result = descriptionRegex.find(description)
        return result?.value?.replaceFirstChar { it.uppercase() }
    }

}