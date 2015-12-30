package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * This estimator does no estimation, it just packs {@code Point}s found in
 * detection phase into {@code PSFModel} objects.
 *
 * This is suitable for quick preview of filtering/detection or for some basic
 * molecule counting applications where the pixel precision is not an issue.
 */
public class EmptyEstimator extends IEstimatorUI implements IEstimator  {

    private final String name = "No estimator";

    @Override
    public List<Molecule> estimateParameters(FloatProcessor fp, List<Point> detections) {
        List<Molecule> locations = new ArrayList<Molecule>();

        for (Point detection : detections) {
            locations.add(new Molecule(new PSFModel.Params(new int[]{PSFModel.Params.X, PSFModel.Params.Y},
                    new double[]{detection.x.doubleValue()+0.5, detection.y.doubleValue()+0.5}, false)));
        }
        return locations;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        return null;
    }

    @Override
    public void readParameters() {
        // nothing to do here
    }

    @Override
    public void recordOptions() {
    }

    @Override
    public void readMacroOptions(String options) {
    }

    @Override
    public IEstimator getImplementation() {
        return this;
    }

    @Override
    public void resetToDefaults() {
    }
}
