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

public class ASHRenderingUI(sizeX: Double = 0.0, sizeY: Double = 0.0) : AbstractRenderingUI(sizeX, sizeY) {

    companion object {
        public const val name = "Averaged shifted histograms"
    }

    private val shifts: ParameterKey.Integer?
    private val zShifts: ParameterKey.Integer?

    init {
        shifts = parameters.createIntField("shifts", IntegerValidatorFactory.positiveNonZero(), 2)
        zShifts = parameters.createIntField("zshifts", IntegerValidatorFactory.positiveNonZero(), 2, threeDCondition)
    }

    public override fun getName()
            = name

    public override fun getOptionsPanel(): JPanel {
        val panel = super.getOptionsPanel()

        val shiftsTextField = JTextField("", 20)
        parameters.registerComponent(shifts, shiftsTextField)
        panel.add(JLabel("Lateral shifts:"), GridBagHelper.leftCol())
        panel.add(shiftsTextField, GridBagHelper.rightCol())

        val zShiftsTextField = JTextField("", 20)
        parameters.registerComponent(zShifts, zShiftsTextField)
        val zShiftsLabel = JLabel("Axial shifts:")
        panel.add(zShiftsLabel, GridBagHelper.leftCol())
        panel.add(zShiftsTextField, GridBagHelper.rightCol())
        val threeDCheckBox = (parameters.getRegisteredComponent(threeD)) as JCheckBox
        threeDCheckBox.addActionListener(object : ActionListener {
            public override fun actionPerformed(e: ActionEvent) {
                zShiftsLabel.setEnabled(threeDCheckBox.isSelected())
                zShiftsTextField.setEnabled(threeDCheckBox.isSelected())
            }
        })

        parameters.loadPrefs()
        return panel
    }

    public override fun getMethod(): IncrementalRenderingMethod {
        if(parameters.getBoolean(threeD)) {
            val r = Range.parseFromStepTo(zRange!!.getValue())
            return ASHRendering.Builder()
                    .roi(left, left+sizeX, top, top+sizeY)
                    .resolution(1 / magnification!!.getValue())
                    .shifts(shifts!!.getValue())
                    .zRange(r.from, r.to, r.step)
                    .colorize(colorize!!.getValue())
                    .colorizationLUT(lutPicker!!.getLut(colorizationLut!!.getValue()))
                    .zShifts(zShifts!!.getValue()).build()
        } else {
            return ASHRendering.Builder()
                    .roi(left, left+sizeX, top, top+sizeY)
                    .resolution(1 / magnification!!.getValue())
                    .shifts(shifts!!.getValue()).build()
        }
    }
}