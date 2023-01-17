package security

import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.Properties

object FicheroProperties {

    private val fs = File.separator

    fun loadProperties(): Properties {
        val workingDir: String = System.getProperty("user.dir")

        val ficheroProperties =
            Paths.get(workingDir + fs + "src" + fs + "main" + fs + "resources" + fs + "config.properties")

        val properties = Properties()

        properties.load(FileInputStream(ficheroProperties.toString()))

        return properties
    }
}