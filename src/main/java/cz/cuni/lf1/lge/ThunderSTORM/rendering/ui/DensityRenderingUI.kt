package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn
import cz.cuni.lf1.lge.ThunderSTORM.rendering.DensityRendering
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper
import cz.cuni.lf1.lge.ThunderSTORM.util.Range
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

public class DensityRenderingUI(sizeX: Double = 0.0, sizeY: Double = 0.0) : AbstractRenderingUI(sizeX, sizeY) {

    companion object {
        public const val name = "Normalized Gaussian"
    }

    //param names
    private var dx: ParameterKey.Double? = null
    private var dz: ParameterKey.Double? = null
    private var forceDx: ParameterKey.Boolean? = null
    private var forceDz: ParameterKey.Boolean? = null

    init {
        dx = parameters.createDoubleField("dx", DoubleValidatorFactory.positiveNonZero(), 20.0)
        dz = parameters.createDoubleField("dz", DoubleValidatorFactory.positiveNonZero(), 100.0, threeDCondition)
        forceDx = parameters.createBooleanField("dxforce", null, false)
        forceDz = parameters.createBooleanField("dzforce", null, false)
    }

    public override fun getOptionsPanel(): JPanel {
        val panel = super.getOptionsPanel()
        //dx
        val forceDXCheckBox = JCheckBox("Force", false)
        forceDXCheckBox.setBorder(BorderFactory.createEmptyBorder())
        parameters.registerComponent(forceDx, forceDXCheckBox)
        val latUncertaintyPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        panel.add(JLabel("Lateral uncertainty [nm]:"), GridBagHelper.leftCol())
        val dxTextField = JTextField("", 10)
        parameters.registerComponent(dx, dxTextField)
        latUncertaintyPanel.add(dxTextField)
        latUncertaintyPanel.add(Box.createHorizontalStrut(5))
        latUncertaintyPanel.add(forceDXCheckBox)
        panel.add(latUncertaintyPanel, GridBagHelper.rightCol())
        //dz
        val forceDZCheckBox = JCheckBox("Force", false)
        forceDZCheckBox.setBorder(BorderFactory.createEmptyBorder())
        parameters.registerComponent(forceDz, forceDZCheckBox)
        val axUncertaintyPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        val dzLabel = JLabel("Axial uncertainty [nm]:")
        panel.add(dzLabel, GridBagHelper.leftCol())
        val dzTextField = JTextField("", 10)
        parameters.registerComponent(dz, dzTextField)
        axUncertaintyPanel.add(dzTextField)
        axUncertaintyPanel.add(Box.createHorizontalStrut(5))
        axUncertaintyPanel.add(forceDZCheckBox)
        panel.add(axUncertaintyPanel, GridBagHelper.rightCol())
        //3D
        val threeDCheckBox = (parameters.getRegisteredComponent(threeD)) as JCheckBox
        threeDCheckBox.addActionListener(object : ActionListener {
            public override fun actionPerformed(e: ActionEvent) {
                dzLabel.setEnabled(threeDCheckBox.isSelected())
                forceDZCheckBox.setEnabled(threeDCheckBox.isSelected())
                dzTextField.setEnabled(threeDCheckBox.isSelected())
            }
        })

        parameters.loadPrefs()
        return panel
    }

    public override fun getName()
            = name

    public override fun getMethod(): IncrementalRenderingMethod {
        if(threeD!!.getValue()) {
            val r = Range.parseFromStepTo(zRange!!.getValue())
            return DensityRendering.Builder()
                    .roi(left, left+sizeX, top, top+sizeY)
                    .resolution(1 / magnification!!.getValue())
                    .defaultDX(dx!!.getValue() / CameraSetupPlugIn.getPixelSize())
                    .forceDefaultDX(forceDx!!.getValue())
                    .zRange(r.from, r.to, r.step)
                    .colorize(colorize!!.getValue())
                    .colorizationLUT(lutPicker!!.getLut(colorizationLut!!.getValue()))
                    .defaultDZ(dz!!.getValue())
                    .forceDefaultDZ(forceDz!!.getValue())
                    .build()
        } else {
            return DensityRendering.Builder()
                    .roi(left, left+sizeX, top, top+sizeY)
                    .resolution(1 / magnification!!.getValue())
                    .defaultDX(dx!!.getValue() / CameraSetupPlugIn.getPixelSize())
                    .forceDefaultDX(forceDx!!.getValue()).build()
        }
    }
}
