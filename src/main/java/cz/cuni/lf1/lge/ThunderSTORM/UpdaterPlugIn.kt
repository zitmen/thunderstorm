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
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url
import rx.Observable
import rx.Single

import javax.swing.*
import java.awt.*
import java.io.*
import java.util.*

private const val LOG_TAG = "Updater"

class UpdaterPlugIn : PlugIn {

    override fun run(arg: String) {
        GUI.setLookAndFeel()
        IJ.showStatus("Checking the file access rights...")
        val file = File(Menus.getPlugInsPath() + "/" + ThunderSTORM.FILE_NAME)
        if (!file.exists()) {
            IJ.error(LOG_TAG, "File not found: " + file.path)
            return
        }
        if (!file.canWrite()) {
            IJ.error(LOG_TAG, "No write access: " + file.path + (if (IJ.isVista()) Prefs.vistaHint else ""))
            return
        }
        IJ.showStatus("Looking for new versions...")

        getVersionListFromWeb().map { r -> r.map { release -> releaseToVersion(release) }.filterNotNull() }.toSingle()
            .subscribe(/*onSuccess = */{ versions ->
                IJ.showStatus("")
                askUserForVersion(ImmutableSortedSet.reverseOrder<Version>().addAll(versions).build().asList())?.let { version ->
                    IJ.showStatus("Downloading ${ThunderSTORM.FILE_NAME} ...")
                    downloadJar(version.downloadUrl!!)
                            .subscribe(/*onSuccess = */{ data ->
                                IJ.showProgress(1.0)
                                IJ.showStatus("Installing ${ThunderSTORM.FILE_NAME} ...")
                                updateJar(file, data)
                                        .subscribe(/*onSuccess = */{
                                            IJ.showStatus("Done.")
                                            IJ.showMessage(LOG_TAG, "Please restart ImageJ to complete ThunderSTORM update.")
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

    private class Version(val tagName: String, val downloadUrl: String? = null) : Comparable<Version> {
        val version: String
        val year: Int
        val month: Int
        val day: Int
        val buildOfTheDay: Int

        init {
            val tokens = tagName.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            this.version = tokens[0]
            this.year = Integer.parseInt(tokens[1])
            this.month = Integer.parseInt(tokens[2])
            this.day = Integer.parseInt(tokens[3])
            this.buildOfTheDay = if (tokens.size > 4) Integer.parseInt(tokens[4].substring(1)) else 0
        }

        override fun toString() = if (version == "dev") tagName else "%s (%04d-%02d-%02d)".format(version, year, month, day)

        val isStable: Boolean
            get() = "dev" != version

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

        override fun hashCode() = tagName.hashCode()
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

        private fun downloadJar(url: String) =
                Retrofit.Builder()
                        .baseUrl("https://github.com/")
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .build()
                        .create(GitHubService::class.java)
                        .downloadRelease(url)
                        .map { resp -> resp.bytes() }
                        .asObservable()

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

        private fun getVersionListFromWeb() =
                Retrofit.Builder()
                        .baseUrl("https://api.github.com/")
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(GitHubService::class.java)
                        .listReleases("zitmen", "thunderstorm")
                        .asObservable()

        private fun releaseToVersion(release: Release): Version? {
            val tagName = release.tag_name
            val downloadUrl = release.assets?.firstOrNull { a -> a.name == ThunderSTORM.FILE_NAME }?.browser_download_url
            if (tagName != null && downloadUrl != null) {
                val version =
                    if (tagName.startsWith("v")) {
                        // assumming full release
                        val ver = tagName.trimStart {  c -> c.isLetter() }.trimEnd { c -> c.isLetter() };
                        val cal = Calendar.getInstance()
                        cal.time = release.created_at
                        "%s-%04d-%02d-%02d".format(ver, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH))
                    } else {
                        tagName
                    }
                return Version(version, downloadUrl)
            } else {
                return null
            }
        }
    }
}

private interface GitHubService {
    @GET("repos/{user}/{repo}/releases")
    fun listReleases(@Path("user") user: String, @Path("repo") repo: String): Observable<List<Release>>

    @GET
    fun downloadRelease(@Url url: String): Observable<ResponseBody>
}

private class User {
    var login: String? = null
    var id: Int? = null
    var avatar_url: String? = null
    var gravatar_id: String? = null
    var url: String? = null
    var html_url: String? = null
    var followers_url: String? = null
    var following_url: String? = null
    var gists_url: String? = null
    var starred_url: String? = null
    var subscriptions_url: String? = null
    var organizations_url: String? = null
    var repos_url: String? = null
    var events_url: String? = null
    var received_events_url: String? = null
    var type: String? = null
    var site_admin: Boolean? = null
}

private class Asset {
    var url: String? = null
    var browser_download_url: String? = null
    var id: Int? = null
    var name: String? = null
    var label: String? = null
    var state: String? = null
    var content_type: String? = null
    var size: Int? = null
    var download_count: Int? = null
    var created_at: Date? = null
    var updated_at: Date? = null
    var uploader: User? = null
}

private class Release {
    var url: String? = null
    var html_url: String? = null
    var assets_url: String? = null
    var upload_url: String? = null
    var tarball_url: String? = null
    var zipball_url: String? = null
    var id: Int? = null
    var tag_name: String? = null
    var target_commitish: String? = null
    var name: String? = null
    var body: String? = null
    var draft: Boolean? = null
    var prerelease: Boolean? = null
    var created_at: Date? = null
    var published_at: Date? = null
    var author: User? = null
    var assets: List<Asset>? = null
}