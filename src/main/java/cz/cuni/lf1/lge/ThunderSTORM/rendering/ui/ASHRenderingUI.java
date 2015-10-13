package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.ASHRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ASHRenderingUI extends AbstractRenderingUI {

    public static final String name = "Averaged shifted histograms";
    private ParameterKey.Integer shifts;
    private ParameterKey.Integer zShifts;

    private void initPars() {
        shifts = parameters.createIntField("shifts", IntegerValidatorFactory.positiveNonZero(), 2);
        zShifts = parameters.createIntField("zshifts", IntegerValidatorFactory.positiveNonZero(), 2, threeDCondition);
    }

    public ASHRenderingUI() {
        super();
        initPars();
    }

    public ASHRenderingUI(int sizeX, int sizeY) {
        super(sizeX, sizeY);
        initPars();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = super.getOptionsPanel();

        JTextField shiftsTextField = new JTextField("", 20);
        parameters.registerComponent(shifts, shiftsTextField);
        panel.add(new JLabel("Lateral shifts:"), GridBagHelper.leftCol());
        panel.add(shiftsTextField, GridBagHelper.rightCol());

        final JTextField zShiftsTextField = new JTextField("", 20);
        parameters.registerComponent(zShifts, zShiftsTextField);
        final JLabel zShiftsLabel = new JLabel("Axial shifts:");
        panel.add(zShiftsLabel, GridBagHelper.leftCol());
        panel.add(zShiftsTextField, GridBagHelper.rightCol());
        final JCheckBox threeDCheckBox = (JCheckBox) parameters.getRegisteredComponent(threeD);
        threeDCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zShiftsLabel.setEnabled(threeDCheckBox.isSelected());
                zShiftsTextField.setEnabled(threeDCheckBox.isSelected());
            }
        });

        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IncrementalRenderingMethod getMethod() {
        if(parameters.getBoolean(threeD)) {
            Range r = Range.parseFromStepTo(zRange.getValue());
            return new ASHRendering.Builder()
                    .roi(left, left+sizeX, top, top+sizeY)
                    .resolution(1 / magnification.getValue())
                    .shifts(shifts.getValue())
                    .zRange(r.from, r.to, r.step)
                    .colorize(colorize.getValue())
                    .colorizationLUT(lutPicker.getLut(colorizationLut.getValue()))
                    .zShifts(zShifts.getValue()).build();
        } else {
            return new ASHRendering.Builder()
                    .roi(left, left+sizeX, top, top+sizeY)
                    .resolution(1 / magnification.getValue())
                    .shifts(shifts.getValue()).build();
        }
    }
}
