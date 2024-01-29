package no.martials.hvl_ics.service

import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.component.CalendarComponent
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.validate.ValidationException
import org.jetbrains.annotations.Contract
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.FileOutputStream
import java.io.StringReader
import java.net.URI
import java.net.URL

@Service
final class IcsService(private val fileService: FileService) {

    private final val logger = LoggerFactory.getLogger(IcsService::class.java)
    private final val typeRegex = Regex("videokonferanse|forelesning|datalab|øving", RegexOption.IGNORE_CASE)

    @Value("\${app.savepath}")
    private lateinit var savepath: String

    /**
     * Validates if the given URL is a valid HTTPS URL pointing to a timeedit calendar file (ICS format).
     *
     * @param url The URL to be validated.
     * @return `true` if the URL is a valid HTTPS URL pointing to a timeedit calendar file (ICS format),
     *         `false` otherwise.
     */
    @Contract(pure = true)
    fun validate(url: URL): Boolean =
        url.protocol contentEquals "https" &&
                url.host.contains("cloud.timeedit.net") &&
                url.path.endsWith(".ics")

    /**
     * Creates a Calendar object from the provided URL.
     *
     * @param url the URL pointing to the iCalendar file.
     * @return the Calendar object created from the iCalendar file.
     * @throws ValidationException if the iCalendar file is not valid.
     */
    fun createCalendar(url: URL, demokratitid: Boolean = false): Calendar {
        val icsString = readIcsFrom(url)
        val sin = StringReader(icsString)
        val calendar = CalendarBuilder().build(sin)

        calendar.validate()

        if (!demokratitid) {
            removeDemokratitid(calendar)
        }

        replaceSummary(calendar)
        return calendar
    }

    /**
     * Removes all calendar events with the summary containing "demokratitid" (case-insensitive) from the given calendar.
     *
     * @param calendar The calendar from which to remove the events.
     */
    fun removeDemokratitid(calendar: Calendar) =
        calendar.components
            .filterIsInstance<VEvent>()
            .filter { it.summary.value.contains("demokratitid", true) }
            .toMutableList() // Avoid concurrent modification
            .forEach { calendar.components.remove(it) }

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
    fun replaceSummary(calendar: Calendar) = // TODO test
        calendar.components
            .filterIsInstance<VEvent>()
            .forEach { it.getProperty<Property>(Property.SUMMARY).value = fixSummary(it) }

    /**
     * Reads the content of an ICS file from the specified URL.
     *
     * @param url the URL of the ICS file
     * @return the content of the ICS file as a String
     */
    private fun readIcsFrom(url: URL): String {
        var icsString: String
        url.openStream().use { input ->
            icsString = String(input.readBytes())
            logger.debug("Reading data {}", icsString)
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
    fun createIcsFile(filename: String, calendar: Calendar) {
        if (!filename.endsWith(".ics")) {
            throw IllegalArgumentException("Filename must end with .ics")
        }

        fileService.createDirIfNotExists(savepath)

        val fout = FileOutputStream("$savepath/$filename")
        val outputter = CalendarOutputter()
        outputter.output(calendar, fout)
    }

    /**
     * Creates an ICS file based on the provided URI.
     *
     * @param uri the URL of the calendar source.
     * @return the URI of the generated ICS file.
     */
    fun createIcs(uri: URL, demokratitid: Boolean = false): URI {
        val calendar = createCalendar(uri, demokratitid)
        val filename = uri.path.substringAfterLast("/")
        createIcsFile(filename, calendar)
        return URI(filename)
    }

    /**
     * Returns a fixed summary for the given calendar component.
     *
     * @param calendar The calendar component for which to fix the summary.
     * @return The fixed summary as a string.
     */
    private fun fixSummary(calendar: CalendarComponent): String {
        val summary = calendar.getProperty<Property>(Property.SUMMARY)
        val description = calendar.getProperty<Property>(Property.DESCRIPTION)
        val location = calendar.getProperty<Property>(Property.LOCATION)

        var result = getEmne(summary.value)
        val type = getType(description.value) ?: getType(location.value)
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
    @Contract(pure = true)
    fun getEmne(summary: String) =
        summary.substringAfter("Emne: ").substringBefore(",")

    /**
     * Retrieves the type of an event based on the property.
     *
     * @param property A property of the event.
     * @return The type of the event if it matches certain keywords, null otherwise.
     */
    fun getType(property: String): String? {
        val result = typeRegex.find(property)
        if (result?.value.equals("datalab", true)) {
            return "Lab"
        }
        return result?.value?.replaceFirstChar { it.uppercase() }
    }

    /**
     * Converts a URI string to an absolute URI.
     *
     * @param uri the URI string to convert to absolute URI
     * @return the absolute URI object
     */
    @Contract(pure = true)
    fun toAbsoluteUri(uri: String): URI =
        if (uri.startsWith("https://")) {
            URI(uri)
        } else {
            URI("https://${uri.substring(uri.indexOf("cloud.timeedit.net"))}")
        }

}
