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

    @GetMapping("/ics/{filename:.+}")
    fun getSavedFile(@PathVariable filename: String): ResponseEntity<InputStreamResource> {
        logger.info("Received request to get file: {}", filename)
        return getFile(URI(filename))
    }

    // TODO replcae with more robust solution
    private fun toAbsoluteUri(uri: String): String =
        "https://${uri.substring(if (uri.startsWith("/https")) 8 else 1)}"

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