package no.martials.hvl_ics.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.net.URL

@Service
class FileService {

    private final val logger = LoggerFactory.getLogger(FileService::class.java)

    /**
     * Creates a directory in the specified savepath if it does not already exist.
     */
    fun createDirIfNotExists(savepath: String) {
        val dir = File(savepath)
        if (!dir.exists()) {
            logger.info("Creating directory: {}", savepath)
            dir.mkdirs()
        }
    }

}
