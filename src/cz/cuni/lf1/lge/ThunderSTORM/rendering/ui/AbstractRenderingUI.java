package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterName;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterTracker;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.Validator;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.ValidatorException;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public abstract class AbstractRenderingUI extends IRendererUI {

    int sizeX;
    int sizeY;
    //default values
    private final static double DEFAULT_MAGNIFICATION = 5;
    private final static int DEFAULT_REPAINT_FREQUENCY = 50;
    private static final String DEFAULT_Z_RANGE = "-500:100:500";
    //parameter names
    protected static final ParameterName.Double MAGNIFICATION =  new ParameterName.Double("magnification");
    protected static final ParameterName.Integer REPAINT_FREQUENCY = new ParameterName.Integer("repaint");
    protected static final ParameterName.Boolean THREE_D = new ParameterName.Boolean("threeD");
    protected static final ParameterName.String Z_RANGE = new ParameterName.String("zrange");
    protected ParameterTracker.Condition threeDCondition = new ParameterTracker.Condition() {
        @Override
        public boolean isSatisfied() {
            return parameters.getBoolean(THREE_D);
        }

        @Override
        public ParameterName[] dependsOn() {
            return new ParameterName[]{THREE_D};
        }
    };

    public AbstractRenderingUI() {
        parameters.createDoubleField(MAGNIFICATION, DoubleValidatorFactory.positiveNonZero(), DEFAULT_MAGNIFICATION);
        parameters.createIntField(REPAINT_FREQUENCY, IntegerValidatorFactory.positive(), DEFAULT_REPAINT_FREQUENCY);
        parameters.createBooleanField(THREE_D, null, false);
        parameters.createStringField(Z_RANGE, new Validator<String>() {
            @Override
            public void validate(String input) throws ValidatorException {
                try {
                    Range r = Range.parseFromStepTo(input);
                    int nSlices = (int) ((r.to - r.from) / r.step);
                    if(r.from > r.to) {
                        throw new RuntimeException("Z range \"from\" value (" + r.from + ") must be smaller than \"to\" value (" + r.to + ").");
                    }
                    if(nSlices < 1) {
                        throw new RuntimeException("Invalid range: Must have at least one slice.");
                    }
                } catch(RuntimeException ex) {
                    throw new ValidatorException(ex);
                }
            }
        }, DEFAULT_Z_RANGE,threeDCondition);
    }

    public AbstractRenderingUI(int sizeX, int sizeY) {
        this();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    @Override
    public void setSize(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    @Override
    public int getRepaintFrequency() {
        return parameters.getInt(REPAINT_FREQUENCY);
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        final JTextField resolutionTextField = new JTextField("", 20);
        parameters.registerComponent(MAGNIFICATION, resolutionTextField);
        final JTextField repaintFrequencyTextField = new JTextField("", 20);
        parameters.registerComponent(REPAINT_FREQUENCY, repaintFrequencyTextField);
        panel.add(new JLabel("Magnification:"), GridBagHelper.leftCol());
        panel.add(resolutionTextField, GridBagHelper.rightCol());

        panel.add(new JLabel("Repaint frequency [frames]:"), GridBagHelper.leftCol());
        panel.add(repaintFrequencyTextField, GridBagHelper.rightCol());

        final JLabel zRangeLabel = new JLabel("Z range (from:step:to) [nm]:");
        final JTextField zRangeTextField = new JTextField("", 20);
        parameters.registerComponent(Z_RANGE, zRangeTextField);
        final JCheckBox threeDCheckBox = new JCheckBox("", true);
        parameters.registerComponent(THREE_D, threeDCheckBox);
        threeDCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zRangeLabel.setEnabled(threeDCheckBox.isSelected());
                zRangeTextField.setEnabled(threeDCheckBox.isSelected());
            }
        });
        panel.add(new JLabel("3D:"), GridBagHelper.leftCol());
        panel.add(threeDCheckBox, GridBagHelper.rightCol());
        panel.add(zRangeLabel, GridBagHelper.leftCol());

        panel.add(zRangeTextField, GridBagHelper.rightCol());

        return panel;
    }

    @Override
    public IncrementalRenderingMethod getImplementation() {
        return getMethod();
    }

    protected abstract IncrementalRenderingMethod getMethod();
}
