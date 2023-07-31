package no.martials.hvl_ics.controllers

import jakarta.servlet.http.HttpServletRequest
import no.martials.hvl_ics.service.IcsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.BodyBuilder
import org.springframework.web.bind.annotation.*
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.URI

@RestController
class IcsController(val icsService: IcsService) {

    private final val logger = LoggerFactory.getLogger(IcsController::class.java)

    @Value("\${app.savepath}")
    private lateinit var savepath: String

    @Value("\${app.basepath}")
    private lateinit var basepath: String

    /**
     * Creates an ICS (iCalendar) file from the given URL.
     *
     * @param url The URL from which the ICS file should be created.
     * @return A ResponseEntity with a status code of 201 (Created) and the URI of the newly created ICS file in the "Location" header, or
     *         a ResponseEntity with a status code of 400 (Bad Request) if the provided URL is not valid or supported.
     */
    @PutMapping("/create")
    fun createIcs(@RequestBody url: String): ResponseEntity<Unit> {
        logger.info("Received request to create ics from url: {}", url)

        val urlObj = URI(url).toURL()

        if (!icsService.validate(urlObj)) {
            return ResponseEntity.badRequest().build()
        }

        val pathToIcs = icsService.createIcs(urlObj)

        return ResponseEntity.created(URI("$basepath/ics/$pathToIcs")).build()
    }

    /**
     * Creates and retrieves an .ics file based on the URI provided in the request.
     *
     * @param request The HttpServletRequest object containing the request information.
     * @param demokratitid A boolean parameter, defaulting to false, indicating whether the .ics file should include Demokratitid data or not.
     * @return A ResponseEntity object with the .ics file as an InputStreamResource, or a bad request response if the URI is invalid.
     */
    @GetMapping("/**")
    fun createAndGetIcs(
        request: HttpServletRequest,
        @RequestParam(defaultValue = "false") demokratitid: Boolean
    ): ResponseEntity<InputStreamResource> {

        val uri = request.servletPath

        logger.info("Received request to create ics from uri: {}", uri)

        val urlObj = URI(toAbsoluteUri(uri)).toURL()

        if (!icsService.validate(urlObj)) {
            return ResponseEntity.badRequest().build()
        }

        val pathToIcs = icsService.createIcs(urlObj, demokratitid)

        return getFile(pathToIcs) { ResponseEntity.created(URI("$basepath/ics/$pathToIcs")) }
    }

    /**
     * Retrieves a saved file based on the given filename.
     *
     * @param filename The filename of the saved file to retrieve.
     * @return The ResponseEntity containing the InputStreamResource of the file,
     *         or a not found response if the file does not exist.
     */
    @GetMapping("/ics/{filename:.+}")
    fun getSavedFile(@PathVariable filename: String): ResponseEntity<InputStreamResource> {
        logger.info("Received request to get file: {}", filename)
        return getFile(URI(filename))
    }

    // TODO replcae with more robust solution
    private fun toAbsoluteUri(uri: String): String =
        "https://${uri.substring(if (uri.startsWith("/https")) 8 else 1)}"

    /**
     * Retrieves the file from the given URI.
     *
     * @param uri The URI of the file to retrieve.
     * @param response The optional response function used to customize the HTTP response. The default response is an OK status.
     * @return The ResponseEntity containing the file as an InputStreamResource if it exists, or a Not Found status if the file does not exist.
     */
    private fun getFile(
        uri: URI,
        response: () -> BodyBuilder = { ResponseEntity.ok() }
    ): ResponseEntity<InputStreamResource> =
        try {
            val file = FileInputStream("$savepath/$uri")
            response()
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(InputStreamResource(file))
        } catch (e: FileNotFoundException) {
            logger.warn("File not found: {}", e.message)
            ResponseEntity.notFound().build()
        }

}