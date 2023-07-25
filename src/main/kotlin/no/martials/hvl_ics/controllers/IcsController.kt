package no.martials.hvl_ics.controllers

import no.martials.hvl_ics.service.IcsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IcsController(val icsService: IcsService) {

    @GetMapping("/ics")
    fun getIcs(): ResponseEntity<Unit> {
        val createdAt = icsService.createIcs()
        return ResponseEntity.created(createdAt).build()
    }

}