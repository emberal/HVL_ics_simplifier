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
            dir.mkdirs()
        }
    }

    /**
     * Reads the content of a file from the specified URL.
     *
     * @param url the URL of the file
     * @return the content of the file as an InputStream
     */
    fun readFrom(url: URL): InputStream {
        url.openStream().use { input ->
            logger.debug("Reading data {}", input)
            return input
        }
    }

}
