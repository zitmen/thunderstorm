package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.*;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IBiplaneEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IBiplaneEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.IRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.MeasurementProtocol;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.*;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.Utils;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageMath.add;
import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageMath.subtract;

/**
 * ThunderSTORM biplane analysis plugin.
 *
 * Open the options dialog, process an two image stacks simultaneously to recieve a list of
 * localized molecules which will get displayed in the {@code ResultsTable} and
 * previed in a new {@code ImageStack} with detections marked as crosses in
 * {@code Overlay} of each slice of the stack.
 */
public final class BiplaneAnalysisPlugIn implements PlugIn {

    private IFilterUI selectedFilterUI;
    private IBiplaneEstimatorUI selectedEstimatorUI;
    private IDetectorUI selectedDetectorUI;
    private ImagePlus imp1, imp2;
    private Roi roi1, roi2;
    private RenderingQueue renderingQueue;

    @Override
    public void run(String arg) {
        GUI.setLookAndFeel();
        //
        if (Utils.getOpenImageTitles(true).length < 3) {    // 3 = 2 images + 1 empty string
            IJ.error("Two images must be opened for biplane calibration to work!");
            return;
        }

        try {
            // load modules
            List<IFilterUI> allFilters = ModuleLoader.getUIModules(IFilterUI.class);
            List<IDetectorUI> allDetectors = ModuleLoader.getUIModules(IDetectorUI.class);
            List<IBiplaneEstimatorUI> allEstimators = ModuleLoader.getUIModules(IBiplaneEstimatorUI.class);
            List<IRendererUI> allRenderers = ModuleLoader.getUIModules(IRendererUI.class);

            // Create and show the dialog
            BiplaneAnalysisOptionsDialog dialog = new BiplaneAnalysisOptionsDialog(allFilters, allDetectors, allEstimators, allRenderers);
            if(dialog.showAndGetResult() != JOptionPane.OK_OPTION) {
                return;
            }
            selectedFilterUI = dialog.getActiveFilterUI();
            selectedDetectorUI = dialog.getActiveDetectorUI();
            selectedEstimatorUI = dialog.getActiveEstimatorUI();
            IRendererUI selectedRendererUI = dialog.getActiveRendererUI();

            if (((imp1 = dialog.getFirstPlaneStack()) == null) || ((imp2 = dialog.getSecondPlaneStack()) == null)) {
                IJ.error("Couldn't open both images!");
                return;
            }
            if (imp1.getImageStackSize() != imp2.getImageStackSize()) {
                IJ.error("Both stacks must have the same number of frames!");
                return;
            }

            roi1 = imp1.getRoi() != null ? imp1.getRoi() : new Roi(0, 0, imp1.getWidth(), imp1.getHeight());
            roi2 = imp2.getRoi() != null ? imp2.getRoi() : new Roi(0, 0, imp2.getWidth(), imp2.getHeight());

            if (roi1.getFloatWidth() != roi2.getFloatWidth() || roi1.getFloatHeight() != roi2.getFloatHeight()) {
                IJ.error("Both used images (or ROIs) must be of the same size!");
                return;
            }

            selectedRendererUI.setSize(roi1.getBounds().width, roi1.getBounds().height);
            IncrementalRenderingMethod method = selectedRendererUI.getImplementation();
            ImagePlus renderedImage = (method != null) ? method.getRenderedImage() : null;
            renderingQueue = new RenderingQueue(method, new RenderingQueue.DefaultRepaintTask(renderedImage), selectedRendererUI.getRepaintFrequency());

            // if recording window is open, record parameters of all modules
            if(Recorder.record) {
                MacroParser.recordFilterUI(dialog.getActiveFilterUI());
                MacroParser.recordDetectorUI(dialog.getActiveDetectorUI());
                MacroParser.recordBiplaneEstimatorUI(dialog.getActiveEstimatorUI());
                MacroParser.recordRendererUI(dialog.getActiveRendererUI());
            }

            try {
                Thresholder.loadFilters(allFilters);
                Thresholder.setActiveFilter(dialog.getActiveFilterUIIndex());   // !! must be called before any threshold is evaluated !!
                Thresholder.parseThreshold(selectedDetectorUI.getThreadLocalImplementation().getThresholdFormula());
            } catch(Exception ex) {
                IJ.error("Error parsing threshold formula! " + ex.toString());
                return;
            }

            // force clear and hide results table
            IJResultsTable rt = IJResultsTable.getResultsTable();
            rt.forceHide();
            rt.reset();
            rt.setOriginalState();
            rt.setMeasurementProtocol(new MeasurementProtocol(imp1, imp2, selectedFilterUI, selectedDetectorUI, selectedEstimatorUI));

            // analyze
            final AtomicInteger framesProcessed = new AtomicInteger(0);
            final int stackSize = imp1.getImageStackSize();
            Loop.withIndex(1, stackSize + 1, new Loop.BodyWithIndex() {
                @Override
                public void run(int frame) {
                    try {
                        analyzeFrame(frame, getRoiProcessor(imp1, roi1, frame), getRoiProcessor(imp2, roi2, frame));
                        framesProcessed.incrementAndGet();
                        IJ.showProgress((double) framesProcessed.intValue() / (double) stackSize);
                        IJ.showStatus("ThunderSTORM processing frame " + framesProcessed + " of " + stackSize + "...");
                        GUI.checkIJEscapePressed();
                    } catch (StoppedByUserException ie){
                        IJResultsTable rt = IJResultsTable.getResultsTable();
                        synchronized(rt) {
                            if(rt.isForceHidden()) {
                                showResults();
                            }
                        }
                    } catch (StoppedDueToErrorException ex) {
                        IJ.error(ex.getMessage());
                    }
                }
            });

            // finalize and display the results
            IJ.showStatus("ThunderSTORM is generating the results...");
            showResults();

        } catch (ClassCastException ex) {
            // this is usually caused by incompatibility of calibration file with an estimator
            IJ.error("Method can't be initiated: " + ex.getMessage());
        } catch(Throwable ex) {
            IJ.handleException(ex);
        }

        IJ.showProgress(1.0);
        IJ.showStatus("ThunderSTORM finished.");
    }

    private FloatProcessor getRoiProcessor(ImagePlus imp, Roi roi, int index) {
        ImageProcessor ip = imp.getStack().getProcessor(index);
        ip.setRoi(roi.getBounds());
        FloatProcessor fp = subtract(ip.crop().convertToFloatProcessor(), (float) CameraSetupPlugIn.getOffset());
        float minVal = VectorMath.min((float[]) fp.getPixels());
        if(minVal < 0) {
            IJ.log("\\Update:Camera base level is set higher than values in the image!");
            fp = add(-minVal, fp);
        }
        fp.setMask(roi.getMask());
        return fp;
    }

    private void analyzeFrame(int frame, FloatProcessor fp1, FloatProcessor fp2) {
        // init
        IFilter filter = selectedFilterUI.getThreadLocalImplementation();
        IDetector detector = selectedDetectorUI.getThreadLocalImplementation();
        IBiplaneEstimator estimator = selectedEstimatorUI.getThreadLocalImplementation();

        // detection in first plane
        Thresholder.setCurrentImage(fp1);
        List<Point> det1 = detector.detectMoleculeCandidates(filter.filterImage(fp1));
        // detection in second plane
        Thresholder.setCurrentImage(fp2);
        List<Point> det2 = detector.detectMoleculeCandidates(filter.filterImage(fp2));

        // run fitting on the pairs
        List<Molecule> fits = estimator.estimateParameters(fp1, fp2, Point.applyRoiMask(roi1, det1), Point.applyRoiMask(roi2, det2));
        storeFits(fits, frame);
        if(fits.size() > 0) {
            renderingQueue.renderLater(fits);
        }
    }
    
    synchronized private void storeFits(Iterable<Molecule> fits, int frame) {
        IJResultsTable rt = IJResultsTable.getResultsTable();
        for(Molecule psf : fits) {
            psf.insertParamAt(0, MoleculeDescriptor.LABEL_FRAME, MoleculeDescriptor.Units.UNITLESS, (double)frame);
            rt.addRow(psf);
        }
    }

    synchronized private void showResults() {
        //
        // Show table with results
        IJResultsTable rt = IJResultsTable.getResultsTable();
        rt.sortTableByFrame();
        rt.insertIdColumn();
        rt.copyOriginalToActual();
        rt.setActualState();
        rt.convertAllColumnsToAnalogUnits();
        rt.setPreviewRenderer(renderingQueue);
        setDefaultColumnsWidth(rt);
        // TODO: uncomment!!!
        /*
        if(processedImage != null) {
            rt.setAnalyzedImage(processedImage);
        }
        */
        rt.forceShow();
        //
        // Show detections in the image
        /*
        if(processedImage != null) {
            processedImage.setOverlay(null);
            RenderingOverlay.showPointsInImage(rt, processedImage, roi.getBounds(), Color.red, RenderingOverlay.MARKER_CROSS);
            renderingQueue.repaintLater();
        }
        */
    }

    private static void setDefaultColumnsWidth(IJResultsTable rt) {
        rt.setColumnPreferredWidth(MoleculeDescriptor.LABEL_ID, 40);
        rt.setColumnPreferredWidth(MoleculeDescriptor.LABEL_FRAME, 40);
        rt.setColumnPreferredWidth(PSFModel.Params.LABEL_X, 60);
        rt.setColumnPreferredWidth(PSFModel.Params.LABEL_Y, 60);
        rt.setColumnPreferredWidth(PSFModel.Params.LABEL_SIGMA, 40);
        rt.setColumnPreferredWidth(PSFModel.Params.LABEL_SIGMA1, 40);
        rt.setColumnPreferredWidth(PSFModel.Params.LABEL_SIGMA2, 40);
    }
}
