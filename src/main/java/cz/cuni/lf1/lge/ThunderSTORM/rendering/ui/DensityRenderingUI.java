package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.DensityRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DensityRenderingUI extends AbstractRenderingUI {

    public static final String name = "Normalized Gaussian";
    //param names
    private ParameterKey.Double dx;
    private ParameterKey.Double dz;
    private ParameterKey.Boolean forceDx;
    private ParameterKey.Boolean forceDz;

    public DensityRenderingUI() {
        super();
        initPars();
    }

    public DensityRenderingUI(int sizeX, int sizeY) {
        super(sizeX, sizeY);
        initPars();
    }

    private void initPars() {
        dx = parameters.createDoubleField("dx", DoubleValidatorFactory.positiveNonZero(), 20);
        dz = parameters.createDoubleField("dz", DoubleValidatorFactory.positiveNonZero(), 100, threeDCondition);
        forceDx = parameters.createBooleanField("dxforce", null, false);
        forceDz = parameters.createBooleanField("dzforce", null, false);
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = super.getOptionsPanel();
        //dx
        JCheckBox forceDXCheckBox = new JCheckBox("Force", false);
        forceDXCheckBox.setBorder(BorderFactory.createEmptyBorder());
        parameters.registerComponent(forceDx, forceDXCheckBox);
        JPanel latUncertaintyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(new JLabel("Lateral uncertainty [nm]:"), GridBagHelper.leftCol());
        JTextField dxTextField = new JTextField("", 10);
        parameters.registerComponent(dx, dxTextField);
        latUncertaintyPanel.add(dxTextField);
        latUncertaintyPanel.add(Box.createHorizontalStrut(5));
        latUncertaintyPanel.add(forceDXCheckBox);
        panel.add(latUncertaintyPanel, GridBagHelper.rightCol());
        //dz
        final JCheckBox forceDZCheckBox = new JCheckBox("Force", false);
        forceDZCheckBox.setBorder(BorderFactory.createEmptyBorder());
        parameters.registerComponent(forceDz, forceDZCheckBox);
        JPanel axUncertaintyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        final JLabel dzLabel = new JLabel("Axial uncertainty [nm]:");
        panel.add(dzLabel, GridBagHelper.leftCol());
        final JTextField dzTextField = new JTextField("", 10);
        parameters.registerComponent(dz, dzTextField);
        axUncertaintyPanel.add(dzTextField);
        axUncertaintyPanel.add(Box.createHorizontalStrut(5));
        axUncertaintyPanel.add(forceDZCheckBox);
        panel.add(axUncertaintyPanel, GridBagHelper.rightCol());
        //3D
        final JCheckBox threeDCheckBox = (JCheckBox) parameters.getRegisteredComponent(threeD);
        threeDCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dzLabel.setEnabled(threeDCheckBox.isSelected());
                forceDZCheckBox.setEnabled(threeDCheckBox.isSelected());
                dzTextField.setEnabled(threeDCheckBox.isSelected());
            }
        });

        parameters.loadPrefs();
        return panel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IncrementalRenderingMethod getMethod() {
        if(threeD.getValue()) {
            Range r = Range.parseFromStepTo(zRange.getValue());
            return new DensityRendering.Builder()
                    .roi(left, left+sizeX, top, top+sizeY)
                    .resolution(1 / magnification.getValue())
                    .defaultDX(dx.getValue() / CameraSetupPlugIn.getPixelSize())
                    .forceDefaultDX(forceDx.getValue())
                    .zRange(r.from, r.to, r.step)
                    .colorize(colorize.getValue())
                    .colorizationLUT(lutPicker.getLut(colorizationLut.getValue()))
                    .defaultDZ(dz.getValue())
                    .forceDefaultDZ(forceDz.getValue())
                    .build();
        } else {
            return new DensityRendering.Builder()
                    .roi(left, left+sizeX, top, top+sizeY)
                    .resolution(1 / magnification.getValue())
                    .defaultDX(dx.getValue() / CameraSetupPlugIn.getPixelSize())
                    .forceDefaultDX(forceDx.getValue()).build();
        }
    }
}
