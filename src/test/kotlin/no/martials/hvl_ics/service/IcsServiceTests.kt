package no.martials.hvl_ics.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IcsServiceTests {

    private lateinit var icsService: IcsService

    @BeforeEach
    fun setUp() {
        icsService = IcsService()
    }

    @Test
    fun `test getEmne easy`() {
        val summary = "SUMMARY:AUTF_2021_HØST, INF-F_2021_HØST, Emne: ING303, Emne: ING303"
        val emne = icsService.getEmne(summary)
        assert(emne == "ING303")
    }

    @Test
    fun `test getEmne medium`() {
        val summary = "SUMMARY:DATA_2021_HØST, INF_2021_HØST, Emne: DAT158, Emne: DAT158"
        val emne = icsService.getEmne(summary)
        assert(emne == "DAT158")
    }

    @Test
    fun `test getEmne hard`() {
        val summary =
            "SUMMARY:AUTB_2021_HØST, AUTF_2021_HØST, DATA_2021_HØST, EEL_2021_HØST, ELK_2021_HØST, INF-F_2021_HØST, INF_2021_HØST, Emne: ING303, Emne: ING303, Emne: ING303, Emne: ING303, Emne: ING303, Emne: ING303, Emne: ING303"
        val emne = icsService.getEmne(summary)
        assert(emne == "ING303")
    }

    @Test
    fun `test getType Videokonferanse`() {
        val description =
            "Videokonferanse \nLærer: Høyland Sven-Olai \nLærer: Lundervold Alexander Selvikvåg\nID 1154 688"
        val type = icsService.getType(description)
        assert(type == "Videokonferanse")
    }

    @Test
    fun `test getType not ID`() {
        val description = "ID 1181613"
        val type = icsService.getType(description)
        assert(type == null)
    }

    @Test
    fun `test getType missing type`() {
        val description = "DESCRIPTION:Lærer: Høyland Sven-Olai \nLærer: Lundervold Alexander Selvikvåg\nID 1154 703"
        val type = icsService.getType(description)
        assert(type == null)
    }

}