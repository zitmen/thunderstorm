package cz.cuni.lf1.lge.ThunderSTORM

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help
import cz.cuni.lf1.lge.ThunderSTORM.UI.HelpButton
import ij.IJ
import ij.plugin.BrowserLauncher
import ij.plugin.PlugIn
import java.awt.Cursor
import java.awt.Dialog
import java.awt.Dimension
import java.awt.Window
import javax.swing.BorderFactory
import javax.swing.JDialog
import javax.swing.JScrollPane
import javax.swing.event.HyperlinkEvent

private const val URL = "resources/help/about.html"
private const val WINDOW_WIDTH = 600
private const val WINDOW_HEIGHT = 600

class AboutPlugIn : PlugIn {
    override fun run(arg: String) {
        try {
            GUI.setLookAndFeel()
            val dialog = JDialog(IJ.getInstance(), "About ThunderSTORM (" + ThunderSTORM.VERSION + ")")
            if(IJ.isJava17()) {
                dialog.type = Window.Type.UTILITY
            }
            dialog.modalExclusionType = Dialog.ModalExclusionType.APPLICATION_EXCLUDE //for use within modal dialog
            val htmlBrowser = HelpButton.createEditorUsingOurClassLoader()
            htmlBrowser.border = BorderFactory.createEmptyBorder()
            htmlBrowser.addHyperlinkListener({ e ->
                    if(e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                        try {
                            if("jar".equals(e.url.protocol)) {
                                htmlBrowser.page = e.url
                            } else {
                                BrowserLauncher.openURL(e.url.toString())
                            }
                        } catch(ex: Exception) {
                            IJ.handleException(ex)
                        }
                    } else if(e.eventType == HyperlinkEvent.EventType.ENTERED) {
                        htmlBrowser.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    } else if(e.eventType == HyperlinkEvent.EventType.EXITED) {
                        htmlBrowser.cursor = Cursor.getDefaultCursor()
                    }
            })
            val scrollPane = JScrollPane(htmlBrowser)
            scrollPane.preferredSize = Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)
            dialog.contentPane.add(scrollPane)

            htmlBrowser.page = Help.getUrl(URL)

            dialog.pack()
            dialog.setLocationRelativeTo(null)
            dialog.isVisible = true
        } catch(e: Exception) {
            IJ.handleException(e)
        }
    }
}
