package cz.cuni.lf1.lge.ThunderSTORM

import ij.IJ
import java.io.IOException
import java.util.*

object ThunderSTORM {
    const val URL_DAILY = "https://googledrive.com/host/0BzOGc-xMFyDYR1JaelZYQmJsaUE/builds/daily"
    const val URL_STABLE = "https://googledrive.com/host/0BzOGc-xMFyDYR1JaelZYQmJsaUE/builds/stable"
    const val FILE_NAME = "Thunder_STORM"

    val VERSION: String
    init {
        try {
            val p = Properties()
            p.load(IJ.getClassLoader().getResourceAsStream("thunderstorm.properties"))
            VERSION = p.getProperty("version")
        } catch (e: IOException) {
            throw RuntimeException("Can't read build properties! The package is probably corrupted.")
        }
    }
}
