package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.RadialSymmetryFitter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RadialSymmetryEstimatorUI implements IEstimatorUI {

    private final String name = "Radial symmetry estimator";
    protected int fitradius;
    protected transient JTextField fitregsizeTextField;
    private transient static final int DEFAULT_FITRAD = 5;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        fitregsizeTextField = new JTextField(Prefs.get("thunderstorm.estimators.fitregion", "" + DEFAULT_FITRAD), 20);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Estimation radius [px]:"), GridBagHelper.leftCol());
        panel.add(fitregsizeTextField, GridBagHelper.rightCol());

        return panel;
    }

    @Override
    public void readParameters() {
        fitradius = Integer.parseInt(fitregsizeTextField.getText());

        Prefs.set("thunderstorm.estimators.fitregion", "" + fitradius);
    }

    @Override
    public void recordOptions() {
        if(fitradius != DEFAULT_FITRAD) {
            Recorder.recordOption("fitrad", Integer.toString(fitradius));
        }
    }

    @Override
    public void readMacroOptions(String options) {
        fitradius = Integer.parseInt(Macro.getValue(options, "fitrad", Integer.toString(DEFAULT_FITRAD)));
    }

    @Override
    public IEstimator getImplementation() {
        return new MultipleLocationsImageFitting(fitradius, new RadialSymmetryFitter());
    }

    @Override
    public void resetToDefaults() {
        fitregsizeTextField.setText(Integer.toString(DEFAULT_FITRAD));
    }
}
