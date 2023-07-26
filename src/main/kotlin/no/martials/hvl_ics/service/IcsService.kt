package no.martials.hvl_ics.service

import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.component.CalendarComponent
import net.fortuna.ical4j.validate.ValidationException
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

    /**
     * Validates if the given URL is a valid HTTPS URL pointing to a timeedit calendar file (ICS format).
     *
     * @param url The URL to be validated.
     * @return `true` if the URL is a valid HTTPS URL pointing to a timeedit calendar file (ICS format),
     *         `false` otherwise.
     */
    fun validate(url: URL): Boolean = url.protocol contentEquals "https" and
            url.host.contains("cloud.timeedit.net") and
            url.path.endsWith("ics")

    /**
     * Creates a Calendar object from the provided URL.
     *
     * @param url the URL pointing to the iCalendar file.
     * @return the Calendar object created from the iCalendar file.
     * @throws ValidationException if the iCalendar file is not valid.
     */
    fun createCalendar(url: URL): Calendar {
        val icsString = readIcsFrom(url)
        val sin = StringReader(icsString)
        val calendar = CalendarBuilder().build(sin)

        calendar.validate()
        replaceSummary(calendar)
        return calendar
    }

    /**
     * Replaces the value of the summary property with a more readable value.
     * The new value is created by the "emne" part of the summary property and optionally the "type"
     * which can be found in the description property.
     *
     * @param calendar The calendar containing events to update.
     *
     * @see Calendar
     * @see CalendarComponent
     * @see Component
     * @see Property
     * @see fixSummary
     */
    fun replaceSummary(calendar: Calendar) {
        calendar.getComponents<CalendarComponent>(Component.VEVENT).forEach { event ->
            val summary = event.getProperty<Property>(Property.SUMMARY)
            val description = event.getProperty<Property>(Property.DESCRIPTION)
            summary.value = fixSummary(summary.value, description.value)
        }
    }

    /**
     * Reads the content of an ICS file from the specified URL.
     *
     * @param url the URL of the ICS file
     * @return the content of the ICS file as a String
     */
    fun readIcsFrom(url: URL): String {
        var icsString: String
        url.openStream().use { input ->
            logger.debug("Reading data {}", input)
            icsString = String(input.readBytes())
        }
        return icsString
    }

    /**
     * Creates an ICS file with the given filename and calendar data.
     *
     * @param filename The name of the ICS file to be created. Must end with ".ics".
     * @param calendar The calendar data to be written to the ICS file.
     * @return The URI of the created ICS file.
     * @throws IllegalArgumentException If the given filename does not end with ".ics".
     */
    fun createIcsFile(filename: String, calendar: Calendar): URI {
        if (!filename.endsWith(".ics")) {
            throw IllegalArgumentException("Filename must end with .ics")
        }
        val fout = FileOutputStream("$savepath/$filename")
        val outputter = CalendarOutputter()
        outputter.output(calendar, fout)
        return URI("$basepath/$getpath/$filename")
    }

    /**
     * Creates an ICS file based on the provided URI.
     *
     * @param uri the URL of the calendar source.
     * @return the URI of the generated ICS file.
     */
    fun createIcs(uri: URL): URI {
        val calendar = createCalendar(uri)
        val filename = uri.path.substringAfterLast("/") // TODO use title found in .ics instead
        return createIcsFile(filename, calendar)
    }

    /**
     * Fixes the summary by combining the summary and description.
     *
     * @param summary the summary of the item
     * @param description the description of the item
     * @return the fixed summary
     */
    private fun fixSummary(summary: String, description: String): String {
        var result = getEmne(summary)
        val type = getType(description)
        if (type != null) {
            result += " $type"
        }
        return result
    }

    /**
     * Retrieves the "Emne" string from the given summary.
     *
     * @param summary the summary from which to extract the "Emne" string
     * @return the extracted "Emne" string
     */
    fun getEmne(summary: String): String {
        return summary.substringAfter("Emne: ").substringBefore(",")
    }

    /**
     * Retrieves the type of an event based on its description.
     *
     * @param description The description of the event.
     * @return The type of the event if it matches certain keywords, null otherwise.
     */
    fun getType(description: String): String? {
        val descriptionRegex = Regex("videokonferanse|forelesning|lab|øving", RegexOption.IGNORE_CASE)
        val result = descriptionRegex.find(description)
        return result?.value?.replaceFirstChar { it.uppercase() }
    }

}