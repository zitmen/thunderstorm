package cz.cuni.lf1.lge.ThunderSTORM.util;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusCalibration;
import ij.IJ;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public final class UI {

    public static void showAnotherLocationDialog(IOException ex, final DefocusCalibration calibration) {
        final JDialog dialog = new JDialog(IJ.getInstance(), "Error");
        dialog.getContentPane().setLayout(new BorderLayout(0, 10));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.add(new JLabel("Could not save calibration file. " + ex.getMessage(), SwingConstants.CENTER));
        JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton ok = new JButton("OK");
        dialog.getRootPane().setDefaultButton(ok);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        JButton newLocation = new JButton("Save to other path");
        newLocation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(IJ.getDirectory("image"));
                jfc.showSaveDialog(null);
                File f = jfc.getSelectedFile();
                if(f != null) {
                    try {
                        calibration.saveToFile(f.getAbsolutePath());
                    } catch(IOException ex) {
                        showAnotherLocationDialog(ex, calibration);
                    }
                }
                dialog.dispose();
            }
        });
        buttonsPane.add(newLocation);
        buttonsPane.add(ok);
        dialog.getContentPane().add(buttonsPane, BorderLayout.SOUTH);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.getRootPane().setDefaultButton(ok);
        dialog.pack();
        ok.requestFocusInWindow();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

}
