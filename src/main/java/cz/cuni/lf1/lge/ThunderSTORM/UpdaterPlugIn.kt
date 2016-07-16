package cz.cuni.lf1.lge.ThunderSTORM

import com.google.common.collect.ImmutableList
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
import rx.Observable
import rx.Single

import javax.swing.*
import java.awt.*
import java.io.*
import java.net.URL

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

        getVersionListFromWeb(ThunderSTORM.URL_DAILY + "/list.txt")
                .concatWith(getVersionListFromWeb(ThunderSTORM.URL_STABLE + "/list.txt"))
                .toSortedList()
                .toSingle()
                .subscribe(/*onSuccess = */{ versions ->
                    IJ.showStatus("")
                    askUserForVersion(ImmutableSortedSet.reverseOrder<Version>().addAll(versions).build().asList())?.let { version ->
                        IJ.showStatus("Downloading Thunder_STORM.jar ...")
                        downloadJar(version.url)
                                .subscribe(/*onSuccess = */{ data ->
                                    IJ.showProgress(1.0)
                                    IJ.showStatus("Installing Thunder_STORM.jar ...")
                                    updateJar(file, data)
                                            .subscribe(/*onSuccess = */{
                                                IJ.showStatus("Done.")
                                                IJ.showMessage(LOG_TAG, "Please restart ImageJ to complete ThunderSTORM update.")
                                                ModuleLoader.setUseCaching(false)
                                                Menus.updateImageJMenus()
                                            }, /*onError = */{ ex ->
                                                IJ.showStatus("Update failed.")
                                                IJ.handleException(ex)
                                            })
                                }, /*onError = */{ ex ->
                                    IJ.showStatus("Download failed.")
                                    IJ.handleException(ex)
                                })
                    }
                }, /*onError = */{ ex ->
                    IJ.showMessage("Error!", "Connection problem! Check you connection to the Internet or your firewall settings.")
                    IJ.handleException(ex)
                })
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
            for (i in 0..tokens1.size-1) {
                (tokens1[i].compareTo(tokens2[i])).let  { cmp -> if (cmp != 0) return cmp }
            }
            return 0
        }

        override fun equals(other: Any?) = other is Version && compareTo(other) == 0

        override fun hashCode() = fileName.hashCode()
    }

    // GUI
    private class UpdaterDialog internal constructor(private val mVersions: ImmutableList<Version>)
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

        private fun askUserForVersion(versions: ImmutableList<Version>): Version? {
            val dialog = UpdaterDialog(versions)
            if (!MacroParser.isRanFromMacro()) {
                if (dialog.showAndGetResult() != JOptionPane.OK_OPTION) {
                    return null
                }
            }
            return dialog.selectedVersion
        }

        private fun downloadJar(urlAddress: String) =
            Single.create<ByteArray> { subscriber ->
                try {
                    URL(urlAddress).openConnection().inputStream.use { inStream ->
                        var n = 0
                        var len = 1024 * 1024    // 1 MiB
                        var data = ByteArray(len)
                        while (true) {
                            IJ.showStatus("Downloading Thunder_STORM.jar (" + IJ.d2s(n.toDouble() / 1024.0 / 1024.0, 1) + "MB)")
                            val count = inStream.read(data, n, len - n)
                            if (count < 0) break
                            n += count
                            if (len - n <= 0) {
                                val tmp = data
                                len *= 2
                                data = ByteArray(len)
                                System.arraycopy(tmp, 0, data, 0, tmp.size)
                            }
                        }

                        val tmp = data
                        data = ByteArray(n)
                        System.arraycopy(tmp, 0, data, 0, n)

                        if (!subscriber.isUnsubscribed) {
                            subscriber.onSuccess(data)
                        }
                    }
                } catch (ex: IOException) {
                    if (!subscriber.isUnsubscribed) {
                        subscriber.onError(ex)
                    }
                }
            }

        private fun updateJar(f: File, data: ByteArray) =
            Single.create<Nothing> { subscriber ->
                try {
                    FileOutputStream(f).use { out -> out.write(data, 0, data.size) }
                    if (!subscriber.isUnsubscribed) {
                        subscriber.onSuccess(null)
                    }
                } catch (ex: IOException) {
                    if (!subscriber.isUnsubscribed) {
                        subscriber.onError(ex)
                    }
                }
            }

        private fun getVersionListFromWeb(urlAddress: String) =
            Observable.create<Version> { subscriber ->
                try {
                    BufferedReader(InputStreamReader(URL(urlAddress).openStream())).use { br ->
                        while (true) {
                            val line = br.readLine()
                            if (line == null && !subscriber.isUnsubscribed) {
                                subscriber.onCompleted()
                                break
                            }
                            if (line != "" && !subscriber.isUnsubscribed) {
                                subscriber.onNext(Version(line))
                            }
                        }
                    }
                } catch (ex: IOException) {
                    if (!subscriber.isUnsubscribed) {
                        subscriber.onError(ex)
                    }
                }
            }

    }
}
