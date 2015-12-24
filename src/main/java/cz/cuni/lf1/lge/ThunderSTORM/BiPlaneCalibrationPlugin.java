package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.BiplaneCalibrationDialog;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.BiplaneCalibrationProcess;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusFunction;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.NoMoleculesFittedException;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.BiplaneCalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.plugin.PlugIn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BiPlaneCalibrationPlugin implements PlugIn {

    DefocusFunction defocusModel;
    IFilterUI selectedFilterUI;
    IDetectorUI selectedDetectorUI;
    BiplaneCalibrationEstimatorUI calibrationEstimatorUI;
    String savePath;
    double stageStep;
    double zRangeLimit;//in nm
    ImagePlus imp1, imp2;
    Roi roi1, roi2;

    private boolean isStack(ImagePlus imp) {
        if(imp == null) {
            IJ.error("No image open.");
            return false;
        }
        if(imp.getImageStackSize() < 2) {
            IJ.error("Requires a stack.");
            return false;
        }
        return true;
    }

    @Override
    public void run(String arg) {
        GUI.setLookAndFeel();
        //
        if (Utils.getOpenImageTitles(true).length < 3) {    // 3 = 2 images + 1 empty string
            IJ.error("Two images must be opened for biplane calibration to work!");
            return;
        }
        //
        try {
            //load modules
            calibrationEstimatorUI = new BiplaneCalibrationEstimatorUI();
            List<IFilterUI> filters = ModuleLoader.getUIModules(IFilterUI.class);
            List<IDetectorUI> detectors = ModuleLoader.getUIModules(IDetectorUI.class);
            List<IEstimatorUI> estimators = Arrays.asList(new IEstimatorUI[]{calibrationEstimatorUI}); // only one estimator can be used
            List<DefocusFunction> defocusFunctions = ModuleLoader.getUIModules(DefocusFunction.class);
            Thresholder.loadFilters(filters);

            // get user options
            try {
                GUI.setLookAndFeel();
            } catch(Exception e) {
                IJ.handleException(e);
            }
            BiplaneCalibrationDialog dialog = new BiplaneCalibrationDialog(filters, detectors, estimators, defocusFunctions);
            if(dialog.showAndGetResult() != JOptionPane.OK_OPTION) {
                return;
            }
            selectedFilterUI = dialog.getActiveFilterUI();
            selectedDetectorUI = dialog.getActiveDetectorUI();
            savePath = dialog.getSavePath();
            stageStep = dialog.getStageStep();
            zRangeLimit = dialog.getZRangeLimit();
            defocusModel = dialog.getActiveDefocusFunction();

            if (!isStack(imp1 = dialog.getFirstPlaneStack())) return;
            if (!isStack(imp2 = dialog.getSecondPlaneStack())) return;

            roi1 = imp1.getRoi() != null ? imp1.getRoi() : new Roi(0, 0, imp1.getWidth(), imp1.getHeight());
            roi2 = imp2.getRoi() != null ? imp2.getRoi() : new Roi(0, 0, imp2.getWidth(), imp2.getHeight());

            //perform the calibration
            final BiplaneCalibrationProcess process = new BiplaneCalibrationProcess(selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit, imp1, imp2, roi1, roi2);

            try {
                process.fitQuadraticPolynomials();
                IJ.log("s1 = " + process.getPolynomS1Final().toString());
                IJ.log("s2 = " + process.getPolynomS2Final().toString());
            } catch(NoMoleculesFittedException ex) {
                //if no beads were succesfully fitted, draw localizations anyway
                process.drawOverlay();
                IJ.handleException(ex);
                return;
            }
            process.drawOverlay();
            drawSigmaPlots(process.getAllPolynomsS1(), process.getAllPolynomsS2(),
                    process.getPolynomS1Final(), process.getPolynomS2Final(),
                    process.getAllFrames(), process.getAllSigma1s(), process.getAllSigma2s());

            try {
                process.getCalibration(defocusModel).saveToFile(savePath);
            } catch(IOException ex) {
                showAnotherLocationDialog(ex, process);
            }
        } catch(Exception ex) {
            IJ.handleException(ex);
        }
    }

    private void showAnotherLocationDialog(IOException ex, final BiplaneCalibrationProcess process) {
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
                        process.getCalibration(defocusModel).saveToFile(f.getAbsolutePath());
                    } catch(IOException ex) {
                        showAnotherLocationDialog(ex, process);
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

    private void drawSigmaPlots(List<DefocusFunction> sigma1Quadratics, List<DefocusFunction> sigma2Quadratics,
                                DefocusFunction sigma1param, DefocusFunction sigma2param,
                                double[] allFrames, double[] allSigma1s, double[] allSigma2s) {

        Plot plot = new Plot("Sigma", "z [nm]", "sigma [px]", null, (float[]) null);
        plot.setSize(1024, 768);
        //range
        plot.setLimits(-2*zRangeLimit, +2*zRangeLimit, 0, stageStep);
        double[] xVals = new double[(int)(2*zRangeLimit/stageStep) * 2 + 1];
        for(int val = -2*(int)zRangeLimit, i = 0; val <= +2*(int)zRangeLimit; val += stageStep, i++) {
            xVals[i] = val;
        }
        plot.draw();
        //add points
        plot.setColor(new Color(255, 200, 200));
        plot.addPoints(allFrames, allSigma1s, Plot.CROSS);
        plot.setColor(new Color(200, 200, 255));
        plot.addPoints(allFrames, allSigma2s, Plot.CROSS);

        //add polynomials
        for(int i = 0; i < sigma1Quadratics.size(); i++) {
            double[] sigma1Vals = new double[xVals.length];
            double[] sigma2Vals = new double[xVals.length];
            for(int j = 0; j < sigma1Vals.length; j++) {
                sigma1Vals[j] = sigma1Quadratics.get(i).value(xVals[j]);
                sigma2Vals[j] = sigma2Quadratics.get(i).value(xVals[j]);
            }
            plot.setColor(new Color(255, 230, 230));
            plot.addPoints(xVals, sigma1Vals, Plot.LINE);
            plot.setColor(new Color(230, 230, 255));
            plot.addPoints(xVals, sigma2Vals, Plot.LINE);
        }

        //add final fitted curves
        double[] sigma1ValsAll = new double[xVals.length];
        double[] sigma2ValsAll = new double[xVals.length];
        for(int j = 0; j < sigma1ValsAll.length; j++) {
            sigma1ValsAll[j] = sigma1param.value(xVals[j]);
            sigma2ValsAll[j] = sigma2param.value(xVals[j]);
        }
        plot.setColor(new Color(255, 0, 0));
        plot.addPoints(xVals, sigma1ValsAll, Plot.LINE);
        plot.setColor(new Color(0, 0, 255));
        plot.addPoints(xVals, sigma2ValsAll, Plot.LINE);

        //legend
        plot.setColor(Color.red);
        plot.addLabel(0.1, 0.8, "sigma1");
        plot.setColor(Color.blue);
        plot.addLabel(0.1, 0.9, "sigma2");
        plot.show();
    }
}
