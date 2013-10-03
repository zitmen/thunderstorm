package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.ASHRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import static cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.AbstractRenderingUI.THREE_D;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterName;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import ij.Prefs;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ASHRenderingUI extends AbstractRenderingUI {

    public static final String name = "Averaged shifted histograms";
    private static final int DEFAULT_SHIFTS = 2;
    private static final int DEFAULT_ZSHIFTS = 2;
    private static final ParameterName.Integer SHIFTS = new ParameterName.Integer("shifts");
    private static final ParameterName.Integer ZSHIFTS = new ParameterName.Integer("zshifts");

    private void initPars() {
        parameters.createIntField(SHIFTS, IntegerValidatorFactory.positiveNonZero(), DEFAULT_SHIFTS);
        parameters.createIntField(ZSHIFTS, IntegerValidatorFactory.positiveNonZero(), DEFAULT_ZSHIFTS, threeDCondition);
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
        parameters.registerComponent(SHIFTS, shiftsTextField);
        panel.add(new JLabel("Lateral shifts:"), GridBagHelper.leftCol());
        panel.add(shiftsTextField, GridBagHelper.rightCol());

        final JTextField zShiftsTextField = new JTextField(Prefs.get("thunderstorm.rendering.ash.zshifts", "" + DEFAULT_ZSHIFTS), 20);
        parameters.registerComponent(ZSHIFTS, zShiftsTextField);
        final JLabel zShiftsLabel = new JLabel("Axial shifts:");
        panel.add(zShiftsLabel, GridBagHelper.leftCol());
        panel.add(zShiftsTextField, GridBagHelper.rightCol());
        final JCheckBox threeDCheckBox = (JCheckBox) parameters.getRegisteredComponent(THREE_D);
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
        if(parameters.getBoolean(THREE_D)) {
            Range r = Range.parseFromStepTo(parameters.getString(Z_RANGE));
            return new ASHRendering.Builder()
                    .roi(0, sizeX, 0, sizeY)
                    .resolution(1 / parameters.getDouble(MAGNIFICATION))
                    .shifts(parameters.getInt(SHIFTS))
                    .zRange(r.from, r.to, r.step)
                    .zShifts(parameters.getInt(ZSHIFTS)).build();
        } else {
            return new ASHRendering.Builder()
                    .roi(0, sizeX, 0, sizeY)
                    .resolution(1 / parameters.getDouble(MAGNIFICATION))
                    .shifts(parameters.getInt(SHIFTS)).build();
        }
    }
}
