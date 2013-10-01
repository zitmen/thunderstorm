package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.CentroidFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterName;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CenterOfMassEstimatorUI extends IEstimatorUI {

    private final String name = "Centroid of local neighborhood";
    private transient static final int DEFAULT_FITRAD = 3;
    private transient static final ParameterName.Integer FITRAD = new ParameterName.Integer("fitradius");

    public CenterOfMassEstimatorUI() {
        parameters.createIntField(FITRAD, IntegerValidatorFactory.positiveNonZero(), DEFAULT_FITRAD);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        JTextField fitregsizeTextField = new JTextField("", 20);
        parameters.registerComponent(FITRAD, fitregsizeTextField);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Estimation radius [px]:"), GridBagHelper.leftCol());
        panel.add(fitregsizeTextField, GridBagHelper.rightCol());

        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IEstimator getImplementation() {
        return new MultipleLocationsImageFitting(parameters.getInt(FITRAD), new CentroidFitter());
    }
}
