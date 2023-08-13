package no.martials.hvl_ics.service

import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import net.fortuna.ical4j.model.Calendar
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.FileInputStream
import java.net.URI
import java.net.URL

class IcsServiceTests {

    private val icsPath = (System.getenv("TEST_ICS_PATH") ?: "src/test/resources/files") +
            "/TimeEdit_INF_2021_H2023.ics"
    private val validUrl =
        "https://cloud.timeedit.net/hvl/web/studbergen/ri6305Q64k59u6QZQtQn270QZQ8QY43dZ6317Z0y6580CwtZ00AZ87D9690F55D7EAEBF27863FFDA6.ics"

    private lateinit var icsService: IcsService
    private lateinit var calendar: Calendar
    private lateinit var server: MockWebServer

    @BeforeEach
    fun setUp() {
        val url = setupMockServer()
        icsService = IcsService()
        calendar = icsService.createCalendar(url, true)
    }

    private fun setupMockServer(): URL {
        server = MockWebServer()
        server.start()

        FileInputStream(icsPath).use { file ->
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(String(file.readAllBytes()))
            )
        }
        return server.getUrl("/")
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `test valid url`() {
        val url = URI(validUrl).toURL()
        assertTrue(icsService.validate(url))
    }

    @Test
    fun `test getEmne easy`() {
        val summary = "SUMMARY:AUTF_2021_HØST, INF-F_2021_HØST, Emne: ING303, Emne: ING303"
        val emne = icsService.getEmne(summary)
        assertEquals("ING303", emne)
    }

    @Test
    fun `test getEmne medium`() {
        val summary = "SUMMARY:DATA_2021_HØST, INF_2021_HØST, Emne: DAT158, Emne: DAT158"
        val emne = icsService.getEmne(summary)
        assertEquals("DAT158", emne)
    }

    @Test
    fun `test getEmne hard`() {
        val summary =
            "SUMMARY:AUTB_2021_HØST, AUTF_2021_HØST, DATA_2021_HØST, EEL_2021_HØST, ELK_2021_HØST, INF-F_2021_HØST, INF_2021_HØST, Emne: ING303, Emne: ING303, Emne: ING303, Emne: ING303, Emne: ING303, Emne: ING303, Emne: ING303"
        val emne = icsService.getEmne(summary)
        assertEquals("ING303", emne)
    }

    @Test
    fun `test getType Videokonferanse`() {
        val description =
            "Videokonferanse \nLærer: Høyland Sven-Olai \nLærer: Lundervold Alexander Selvikvåg\nID 1154 688"
        val type = icsService.getType(description)
        assertEquals("Videokonferanse", type)
    }

    @Test
    fun `test getType not ID`() {
        val description = "ID 1181613"
        val type = icsService.getType(description)
        assertNull(type)
    }

    @Test
    fun `test getType missing type`() {
        val description = "DESCRIPTION:Lærer: Høyland Sven-Olai \nLærer: Lundervold Alexander Selvikvåg\nID 1154 703"
        val type = icsService.getType(description)
        assertNull(type)
    }

    @Test
    fun `test getType datalab`() {
        val location = "LOCATION:E403 Datalab \nE443 Datalab"
        val type = icsService.getType(location)
        assertEquals("Lab", type)
    }

    @Test
    fun `test hide demokratitid`() {
        icsService.removeDemokratitid(calendar)
        assertFalse(calendar.toString().contains("demokratitid", true))
    }

    @Test
    fun `test show demokratitid`() {
        assertTrue(calendar.toString().contains("demokratitid", true))
    }

    @Test
    fun `test to absolute URL when absolute`() {
        val absoluteUrl = icsService.toAbsoluteUri(validUrl)
        assertEquals(validUrl, absoluteUrl.toString())
        assertTrue(absoluteUrl.scheme == "https")
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["/cloud.timeedit.net/hvl/web/studbergen/ri6305Q64k59u6QZQtQn270QZQ8QY43dZ6317Z0y6580CwtZ00AZ87D9690F55D7EAEBF27863FFDA6.ics", "cloud.timeedit.net/hvl/web/studbergen/ri6305Q64k59u6QZQtQn270QZQ8QY43dZ6317Z0y6580CwtZ00AZ87D9690F55D7EAEBF27863FFDA6.ics"]
    )
    fun `test to absolute URL when relative`(uriString: String) {
        val absoluteUri = icsService.toAbsoluteUri(uriString)
        assertEquals(
            URI("https://cloud.timeedit.net/hvl/web/studbergen/ri6305Q64k59u6QZQtQn270QZQ8QY43dZ6317Z0y6580CwtZ00AZ87D9690F55D7EAEBF27863FFDA6.ics"),
            absoluteUri
        )
        assertTrue(absoluteUri.scheme == "https")
    }

}