package org.multiground.mglinker

import de.leonhard.storage.Json
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

class ConfHandler {
    fun loadConfig(path: Path): Json {
        val folder: File = path.toFile()
        val file = File(folder, "config.json")
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        if (!file.exists()) {
            try {
                val input: InputStream? = javaClass.getResourceAsStream("/" + file.name)
                if (input != null) {
                    Files.copy(input, file.toPath())
                } else {
                    file.createNewFile()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return Json("config.json", folder.toPath().toString())
    }
}