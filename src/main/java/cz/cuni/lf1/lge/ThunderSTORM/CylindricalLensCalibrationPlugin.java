package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.CalibrationDialog;
import cz.cuni.lf1.lge.ThunderSTORM.UI.RenderingOverlay;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.*;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator.Position;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.CalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.LABEL_FRAME;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import org.yaml.snakeyaml.Yaml;
import ij.gui.Roi;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class CylindricalLensCalibrationPlugin implements PlugIn {

    double angle;
    DefocusFunction defocusModel;
    IFilterUI selectedFilterUI;
    IDetectorUI selectedDetectorUI;
    CalibrationEstimatorUI calibrationEstimatorUI;
    String savePath;
    double stageStep;
    double zRangeLimit;//in nm
    ImagePlus imp;
    Roi roi;

    @Override
    public void run(String arg) {
        GUI.setLookAndFeel();
        //
        imp = IJ.getImage();
        if(imp == null) {
            IJ.error("No image open.");
            return;
        }
        if(imp.getImageStackSize() < 2) {
            IJ.error("Requires a stack.");
            return;
        }
        try {
            //load modules
            calibrationEstimatorUI = new CalibrationEstimatorUI();
            List<IFilterUI> filters = ModuleLoader.getUIModules(IFilterUI.class);
            List<IDetectorUI> detectors = ModuleLoader.getUIModules(IDetectorUI.class);
            List<IEstimatorUI> estimators = Arrays.asList(new IEstimatorUI[]{calibrationEstimatorUI}); // only one estimator can be used
            Thresholder.loadFilters(filters);

            // get user options
            try {
                GUI.setLookAndFeel();
            } catch(Exception e) {
                IJ.handleException(e);
            }
            CalibrationDialog dialog;
            dialog = new CalibrationDialog(imp, filters, detectors, estimators);
            if(dialog.showAndGetResult() != JOptionPane.OK_OPTION) {
                return;
            }
            selectedFilterUI = dialog.getActiveFilterUI();
            selectedDetectorUI = dialog.getActiveDetectorUI();
            savePath = dialog.getSavePath();
            stageStep = dialog.getStageStep();
            zRangeLimit = dialog.getZRangeLimit();
            defocusModel = dialog.getActiveDefocusModel();

            roi = imp.getRoi() != null ? imp.getRoi() : new Roi(0, 0, imp.getWidth(), imp.getHeight());

            //perform the calibration
            final CalibrationProcess process = new CalibrationProcess(selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit, imp, roi);

            process.estimateAngle();
            IJ.log("angle = " + process.getAngle());

            try {
                process.fitQuadraticPolynomials();
                IJ.log("s1 = " + process.getPolynomS1Final().toString());
                IJ.log("s2 = " + process.getPolynomS2Final().toString());
            } catch(NoMoleculesFittedException ex) {
                //if no beads were succesfully fitted, draw localizations anyway
                drawOverlay(imp, process.getAllFits(), process.getUsedPositions());
                IJ.handleException(ex);
                return;
            }
            drawOverlay(imp, process.getAllFits(), process.getUsedPositions());
            drawSigmaPlots(process.getAllPolynomsS1(), process.getAllPolynomsS2(),
                    process.getPolynomS1Final().convertToFrames(stageStep), process.getPolynomS2Final().convertToFrames(stageStep),
                    process.getAllFrames(), process.getAllSigma1s(), process.getAllSigma2s());

            try {
                saveToFile(savePath, process.getCalibration(defocusModel));
            } catch(IOException ex) {
                showAnotherLocationDialog(ex, process);
            }
        } catch(Exception ex) {
            IJ.handleException(ex);
        }
    }

    private void showAnotherLocationDialog(IOException ex, final CalibrationProcess process) {
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
                        saveToFile(f.getAbsolutePath(), process.getCalibration(defocusModel));
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

    private void saveToFile(String path, CylindricalLensCalibration calibration) throws IOException {
        FileWriter fw = null;
        try {
            File file = new File(path);
            Yaml yaml = new Yaml();
            fw = new FileWriter(file);
            yaml.dump(calibration, fw);
            IJ.log("Calibration file saved to: " + file.getAbsolutePath());
            IJ.showStatus("Calibration file saved to " + file.getAbsolutePath());
        } finally {
            if(fw != null) {
                fw.close();
            }
        }

    }

    private void drawSigmaPlots(List<DefocusFunction> sigma1Quadratics, List<DefocusFunction> sigma2Quadratics,
            DefocusFunction sigma1param, DefocusFunction sigma2param,
            double[] allFrames, double[] allSigma1s, double[] allSigma2s) {

        Plot plot = new Plot("Sigma", "z[slices]", "sigma", (float[]) null, (float[]) null);
        plot.setSize(1024, 768);
        //range
        int range = imp.getStackSize() / 2;
        plot.setLimits(-range, +range, 0, 10);
        double[] xVals = new double[range * 2 + 1];
        for(int val = -range, i = 0; val <= range; val++, i++) {
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

    /**
     * draws overlay with each detection and also the positions of beads that
     * were used for fitting polynomials
     *
     */
    private void drawOverlay(ImagePlus imp, List<Molecule> allFits, List<Position> usedPositions) {
        imp.setOverlay(null);
        Rectangle roiBounds = roi.getBounds();

        //allFits
        Map<Integer, List<Molecule>> fitsByFrame = new HashMap<Integer, List<Molecule>>(allFits.size());
        for(Molecule mol : allFits) {
            int frame = (int) mol.getParam(LABEL_FRAME);
            List<Molecule> list;
            if(!fitsByFrame.containsKey(frame)) {
                list = new ArrayList<Molecule>();
                fitsByFrame.put(frame, list);
            } else {
                list = fitsByFrame.get(frame);
            }
            list.add(mol);
        }
        for(Map.Entry<Integer, List<Molecule>> frameFitsEntry : fitsByFrame.entrySet()) {
            int frame = frameFitsEntry.getKey();
            List<Molecule> fits = frameFitsEntry.getValue();
            double[] xAll = new double[fits.size()];
            double[] yAll = new double[fits.size()];
            for(int i = 0; i < fits.size(); i++) {
                Molecule mol = fits.get(i);
                xAll[i] = mol.getX(MoleculeDescriptor.Units.PIXEL) + roiBounds.x;
                yAll[i] = mol.getY(MoleculeDescriptor.Units.PIXEL) + roiBounds.y;
            }
            RenderingOverlay.showPointsInImage(imp, xAll, yAll, frame, Color.BLUE, RenderingOverlay.MARKER_CROSS);
        }

        //centroids of used molecules
        double[] xCentroids = new double[usedPositions.size()];
        double[] yCentroids = new double[usedPositions.size()];
        for(int i = 0; i < xCentroids.length; i++) {
            Position p = usedPositions.get(i);
            xCentroids[i] = p.centroidX + roiBounds.x;
            yCentroids[i] = p.centroidY + roiBounds.y;
        }
        RenderingOverlay.showPointsInImage(imp, xCentroids, yCentroids, Color.red, RenderingOverlay.MARKER_CIRCLE);
        //usedFits
        for(Position p : usedPositions) {
            double[] frame = p.getFramesAsArray();
            double[] x = VectorMath.add(p.getXAsArray(), roiBounds.x);
            double[] y = VectorMath.add(p.getYAsArray(), roiBounds.y);
            for(int i = 0; i < frame.length; i++) {
                RenderingOverlay.showPointsInImage(imp, new double[]{x[i]}, new double[]{y[i]}, (int) frame[i], Color.RED, RenderingOverlay.MARKER_CROSS);
            }
        }

    }

    private void showHistoImages(List<DefocusFunction> sigma1Quadratics, List<DefocusFunction> sigma2Quadratics) {
        FloatProcessor a1 = new FloatProcessor(1, sigma1Quadratics.size());
        FloatProcessor a2 = new FloatProcessor(1, sigma2Quadratics.size());
        FloatProcessor b1 = new FloatProcessor(1, sigma2Quadratics.size());
        FloatProcessor b2 = new FloatProcessor(1, sigma2Quadratics.size());
        FloatProcessor cdif = new FloatProcessor(1, sigma2Quadratics.size());

        for(int i = 0; i < sigma1Quadratics.size(); i++) {
            a1.setf(i, (float) sigma1Quadratics.get(i).getA());
            b1.setf(i, (float) sigma1Quadratics.get(i).getB());
            a2.setf(i, (float) sigma2Quadratics.get(i).getA());
            b2.setf(i, (float) sigma2Quadratics.get(i).getB());
            cdif.setf(i, (float) (sigma2Quadratics.get(i).getC() - sigma1Quadratics.get(i).getC()));
        }
        new ImagePlus("a1", a1).show();
        new ImagePlus("a2", a2).show();
        new ImagePlus("b1", b1).show();
        new ImagePlus("b2", b2).show();
        new ImagePlus("cdif", cdif).show();
    }

    private void dumpShiftedPoints(double[] allFrames, double[] allSigma1s, double[] allSigma2s) {
        try {
            FileWriter fw = new FileWriter("d:\\dump.txt");
            fw.append("allFrames:\n");
            fw.append(Arrays.toString(allFrames));
            fw.append("\nallSigma1:\n");
            fw.append(Arrays.toString(allSigma1s));
            fw.append("\nallSigma2:\n");
            fw.append(Arrays.toString(allSigma2s));
            fw.close();
        } catch(Exception ex) {
            IJ.handleException(ex);
        }
    }
}
