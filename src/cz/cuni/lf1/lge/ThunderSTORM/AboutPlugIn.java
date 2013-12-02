package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import ij.IJ;
import ij.plugin.BrowserLauncher;
import ij.plugin.PlugIn;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.fit.cssbox.swingbox.BrowserPane;
import org.fit.cssbox.swingbox.SwingBoxEditorKit;

public class AboutPlugIn implements PlugIn {

    private static final String url = "resources/help/about.html";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;

    @Override
    public void run(String arg) {
        GUI.setLookAndFeel();
        JDialog dialog = new JDialog(IJ.getInstance(), "About ThunderSTORM (" + ThunderSTORM.VERSION + ")");
        if(IJ.isJava17()) {
            dialog.setType(Window.Type.UTILITY);
        }
        dialog.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE); //for use within modal dialog
        final BrowserPane htmlBrowser = new BrowserPane();
        htmlBrowser.setEditorKit(new SwingBoxEditorKit());
        htmlBrowser.setBorder(BorderFactory.createEmptyBorder());
        htmlBrowser.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        if("jar".equals(e.getURL().getProtocol())) {
                            htmlBrowser.setPage(e.getURL());
                        } else {
                            BrowserLauncher.openURL(e.getURL().toString());
                        }
                    } catch(Exception ex) {
                        IJ.handleException(ex);
                    }
                } else if(e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    htmlBrowser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else if(e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                    htmlBrowser.setCursor(Cursor.getDefaultCursor());
                }

            }
        });
        JScrollPane scrollPane = new JScrollPane(htmlBrowser);
        scrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        dialog.getContentPane().add(scrollPane);

        try {
            htmlBrowser.setPage(Help.getUrl(url));
        } catch(Exception e) {
            htmlBrowser.setText("Could not load resource file.");
        }

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
