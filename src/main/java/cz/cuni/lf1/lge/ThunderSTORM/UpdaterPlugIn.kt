package cz.cuni.lf1.lge.ThunderSTORM

import com.google.common.collect.ImmutableSortedSet
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.DialogStub
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterTracker
import ij.IJ
import ij.Menus
import ij.Prefs
import ij.plugin.PlugIn

import javax.swing.*
import java.awt.*
import java.io.*
import java.net.URL
import java.util.ArrayList

private const val LOG_TAG = "Updater"

class UpdaterPlugIn : PlugIn {

    override fun run(arg: String) {
        GUI.setLookAndFeel()
        IJ.showStatus("Checking the file access rights...")
        val file = File(Menus.getPlugInsPath() + "/" + ThunderSTORM.FILE_NAME + ".jar")
        if (!file.exists()) {
            IJ.error(LOG_TAG, "File not found: " + file.path)
            return
        }
        if (!file.canWrite()) {
            IJ.error(LOG_TAG, "No write access: " + file.path + (if (IJ.isVista()) Prefs.vistaHint else ""))
            return
        }
        IJ.showStatus("Looking for new versions...")
        val version = askUserForVersion(
                ImmutableSortedSet.reverseOrder<Version>()
                        .addAll(getVersionListFromWeb(ThunderSTORM.URL_DAILY + "/list.txt"))
                        .addAll(getVersionListFromWeb(ThunderSTORM.URL_STABLE + "/list.txt"))
                        .build()) ?: return
        downloadAndSaveJar(file, version.url)
        IJ.showMessage(LOG_TAG, "Please restart ImageJ to complete ThunderSTORM update.")
        ModuleLoader.setUseCaching(false)
        Menus.updateImageJMenus()
    }

    private class Version(val fileName: String) : Comparable<Version> {
        val version: String
        val year: Int
        val month: Int
        val day: Int
        val buildOfTheDay: Int

        init {
            val tokens = fileName.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            this.version = tokens[0]
            this.year = Integer.parseInt(tokens[1])
            this.month = Integer.parseInt(tokens[2])
            this.day = Integer.parseInt(tokens[3])
            this.buildOfTheDay = if (tokens.size > 4) Integer.parseInt(tokens[4].substring(1)) else 0
        }

        override fun toString() = if (version == "dev") fileName else "$version ($year-$month-$day)"

        val isStable: Boolean
            get() = "dev" != version

        val url: String
            get() = (if (isStable) ThunderSTORM.URL_STABLE else ThunderSTORM.URL_DAILY) + "/" + fileName + ".jar"

        override fun compareTo(other: Version): Int {
            (year - other.year).let { cmp -> if (cmp != 0) return cmp }
            (month - other.month).let  { cmp -> if (cmp != 0) return cmp }
            (day - other.day).let  { cmp -> if (cmp != 0) return cmp }
            (buildOfTheDay - other.buildOfTheDay).let  { cmp -> if (cmp != 0) return cmp }
            if (isStable && !other.isStable) return 1
            if (!isStable && other.isStable) return -1
            if (!isStable && !other.isStable) return 0

            val tokens1 = version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.map { tok -> tok.toInt() }
            val tokens2 = other.version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.map { tok -> tok.toInt() }
            (tokens1.size - tokens2.size).let  { cmp -> if (cmp != 0) return cmp }
            for (i in 0..tokens1.size) {
                (tokens1[i].compareTo(tokens2[i])).let  { cmp -> if (cmp != 0) return cmp }
            }
            return 0
        }
    }

    // GUI
    private class UpdaterDialog internal constructor(private val mVersions: ImmutableSortedSet<Version>)
        : DialogStub(ParameterTracker("thunderstorm.updater"), IJ.getInstance(), "ThunderSTORM Updater") {

        private val versionsComboBox = JComboBox<Version>(mVersions.toTypedArray())

        val selectedVersion: Version
            get() = versionsComboBox.selectedItem as Version

        private val newestStableVersion: Version?
            get() = mVersions.firstOrNull { v -> v.isStable }

        private val newestDevVersion: Version?
            get() = mVersions.firstOrNull { v -> !v.isStable }

        override fun layoutComponents() {
            val current = Version(ThunderSTORM.VERSION)
            val branch = if (current.isStable) "stable" else "development"
            val newestVersion = if (current.isStable) newestStableVersion else newestDevVersion

            if (newestVersion == null || current.compareTo(newestVersion) >= 0) {
                add(JLabel("ThunderSTORM is up to date! ($branch)").apply { foreground = Color(0, 128, 0) }, GridBagHelper.twoCols())
            } else {
                add(JLabel("New $branch version of ThunderSTORM is available!").apply { foreground = Color(128, 0, 0) }, GridBagHelper.twoCols())
            }

            add(Box.createVerticalStrut(10), GridBagHelper.twoCols())
            add(JLabel("Available versions:"), GridBagHelper.leftCol())
            newestVersion?.let { v ->  versionsComboBox.selectedItem = v }
            add(versionsComboBox, GridBagHelper.rightCol())

            add(Box.createVerticalStrut(10), GridBagHelper.twoCols())
            add(JLabel(
                    "<html>"
                    + "You are currently running version " + current.toString() + ".<br><br>"
                    + "If you click \"OK\", ImageJ will download the selected<br>"
                    + "version and reload ThunderSTORM.<br>"
                    + "</html>"),
                    GridBagHelper.twoCols())

            add(Box.createVerticalStrut(10), GridBagHelper.twoCols())
            add(JPanel(GridBagLayout()).apply {
                    add(Box.createHorizontalGlue(), GridBagConstraints().apply { fill = GridBagConstraints.HORIZONTAL; weightx = 1.0 })
                    add(createOKButton())
                    add(createCancelButton())
                }, GridBagHelper.twoCols())

            params.loadPrefs()
            getRootPane().border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
            setLocationRelativeTo(null)
            isModal = true
        }
    }

    // Static helpers
    companion object {

        private fun askUserForVersion(versions: ImmutableSortedSet<Version>): Version? {
            val dialog = UpdaterDialog(versions)
            if (!MacroParser.isRanFromMacro()) {
                if (dialog.showAndGetResult() != JOptionPane.OK_OPTION) {
                    return null
                }
            }
            return dialog.selectedVersion
        }

        @Throws(IOException::class)
        private fun downloadJar(urlAddress: String): ByteArray {
            IJ.showStatus("Downloading Thunder_STORM.jar ...")
            URL(urlAddress).openConnection().inputStream.use { `in` ->
                var n = 0
                var len = 1024 * 1024    // 1 MiB
                var data = ByteArray(len)
                while (true) {
                    IJ.showStatus("Downloading Thunder_STORM.jar (" + IJ.d2s(n.toDouble() / 1024.0 / 1024.0, 1) + "MB)")
                    val count = `in`.read(data, n, len - n)
                    if (count < 0) {
                        break
                    }
                    n += count
                    if (len - n <= 0) {
                        val tmp = data
                        len *= 2
                        data = ByteArray(len)
                        System.arraycopy(tmp, 0, data, 0, tmp.size)
                    }
                }
                //
                val tmp = data
                data = ByteArray(n)
                System.arraycopy(tmp, 0, data, 0, n)
                //
                IJ.showStatus("Done.")
                IJ.showProgress(1.0)
                return data
            }
        }

        private fun downloadAndSaveJar(f: File, urlAddress: String) {
            // download the update
            val data = try {
                downloadJar(urlAddress)
            } catch (e: IOException) {
                IJ.showStatus("Download failed.")
                IJ.handleException(e)
                return
            }
            // apply the update
            try {
                FileOutputStream(f).use { out ->
                    IJ.showStatus("Installing Thunder_STORM.jar ...")
                    out.write(data, 0, data.size)
                    IJ.showStatus("Done.")
                }
            } catch (e: IOException) {
                IJ.showStatus("Update failed.")
                IJ.handleException(e)
            }

        }

        private fun getVersionListFromWeb(urlAddress: String): List<Version> {
            val v = ArrayList<Version>()
            try {
                BufferedReader(InputStreamReader(URL(urlAddress).openStream())).use { br ->
                    var line: String?
                    while (true) {
                        line = br.readLine()
                        if (line == null) break
                        if (line != "") v.add(Version(line))
                    }
                }
            } catch (ex: IOException) {
                IJ.showMessage("Error!", "Connection problem! Check you connection to the Internet or your firewall settings.")
                IJ.handleException(ex)
            }

            return v
        }

    }
}
