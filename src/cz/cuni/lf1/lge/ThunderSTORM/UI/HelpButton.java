package cz.cuni.lf1.lge.ThunderSTORM.UI;

import ij.IJ;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

public class HelpButton extends JButton {

    private static JFrame textWindow = constructFrame();
    private JScrollPane content;
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 400;
    URL url;

    public HelpButton(URL helpUrl) {
        //TODO icon licence: CC 3.0, attribution required: http://p.yusukekamiyamane.com/icons/attribution/
        super(new ImageIcon(IJ.getClassLoader().getResource("resources/help/images/question-button-icon.png")));
        if(helpUrl == null) {
            throw new NullPointerException("Help file not specified.");
        }
        setBorder(BorderFactory.createEmptyBorder());
        setBorderPainted(false);
        setIconTextGap(0);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.url = helpUrl;
        addActionListener(new HelpButtonActionListener());
        content = createTextWindowContent(helpUrl);
    }

    private static JFrame constructFrame() {
        final JFrame frame = new JFrame();
        frame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                frame.setVisible(false);
            }
        });
        frame.setType(Window.Type.UTILITY);
        return frame;
    }

    private JScrollPane createTextWindowContent(URL url) {
        try {
            JEditorPane editor = new JEditorPane(url);
            Border border = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
            editor.setBorder(border);
            JScrollPane scrollPane = new JScrollPane(editor);
            scrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
            return scrollPane;
        } catch(IOException ex) {
            IJ.handleException(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * show the content in the static window, sizes and positions the window
     * accordingly
     */
    private void showInTextWindow(JScrollPane content) {
        textWindow.setVisible(false);
        textWindow.getContentPane().removeAll();
        textWindow.getContentPane().add(content);

        Window ancestor = SwingUtilities.getWindowAncestor(this);
        int screenEnd = ancestor.getGraphicsConfiguration().getBounds().width + ancestor.getGraphicsConfiguration().getBounds().x;
        Point ancestorLocation = ancestor.getLocationOnScreen();
        if(ancestorLocation.x + ancestor.getWidth() + textWindow.getPreferredSize().width < screenEnd) {
            textWindow.setLocation(ancestorLocation.x + ancestor.getWidth(), ancestorLocation.y);
        } else {
            textWindow.setLocation(ancestorLocation.x - textWindow.getPreferredSize().width, ancestorLocation.y);
        }
        textWindow.pack();
        textWindow.setVisible(true);
    }

    class HelpButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            showInTextWindow(content);
        }
    }
}