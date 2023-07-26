package no.martials.hvl_ics.controllers

import no.martials.hvl_ics.service.IcsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.URI

@RestController
class IcsController(val icsService: IcsService) {

    private final val logger = LoggerFactory.getLogger(IcsController::class.java)

    @Value("\${app.savepath}")
    private lateinit var savepath: String

    // TODO option to hide demokratitid
    @PutMapping("/create")
    fun createIcs(@RequestBody url: String): ResponseEntity<Unit> {
        logger.info("Received request to create ics from uri: {}", url)

        val urlObj = URI(url).toURL()

        if (!icsService.validate(urlObj)) {
            return ResponseEntity.badRequest().build()
        }

        val createIcs = icsService.createIcs(urlObj)
        logger.info("Create ics at {}", createIcs)
        return ResponseEntity.created(createIcs).build()
    }

    @GetMapping("/\${app.path.get}/{filepath:.+}")
    fun getIcs(@PathVariable filepath: String): ResponseEntity<InputStreamResource> {
        return try {
            val file = FileInputStream("$savepath/$filepath")
            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(InputStreamResource(file))
        } catch (e: FileNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

}