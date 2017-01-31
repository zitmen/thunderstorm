package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.UI.LutPicker;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterTracker;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.Validator;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.ValidatorException;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractRenderingUI(var sizeX: Double = 0.0, var sizeY: Double = 0.0) : RendererUI() {

    var left: Double = 0.0
    var top: Double = 0.0

    var zRangeTextField: JTextField? = null
    var threeDCheckBox: JCheckBox? = null
    var lutPicker: LutPicker? = null
    //parameters
    public var magnification: ParameterKey.Double? = null
    public var repaintFrequency: ParameterKey.Integer? = null
    public var threeD: ParameterKey.Boolean? = null
    public var colorize: ParameterKey.Boolean? = null
    public var colorizationLut: ParameterKey.String? = null
    public var zRange: ParameterKey.String? = null

    public val threeDCondition = object : ParameterTracker.Condition {
        public override fun isSatisfied(): Boolean {
            return threeD!!.getValue()
        }

        public override fun dependsOn(): Array<ParameterKey?> {
            return arrayOf(threeD)
        }
    }

    public val colorizeCondition = object : ParameterTracker.Condition {
        public override fun isSatisfied(): Boolean {
            return colorize!!.getValue()
        }

        public override fun dependsOn(): Array<ParameterKey?> {
            return arrayOf(colorize)
        }
    }

    private var showRepaintFrequency: Boolean = true

    init {
        lutPicker = LutPicker()
        magnification = parameters.createDoubleField("magnification", DoubleValidatorFactory.positiveNonZero(), 5.0)
        repaintFrequency = parameters.createIntField("repaint", IntegerValidatorFactory.positive(), 50, object : ParameterTracker.Condition {
            public override fun isSatisfied(): Boolean {
                return showRepaintFrequency
            }

            public override fun dependsOn(): Array<ParameterKey?>? {
                return null
            }
        })
        threeD = parameters.createBooleanField("threeD", null, false)
        colorize = parameters.createBooleanField("colorize", null, false)
        zRange = parameters.createStringField("zrange", object : Validator<String> {
            @Throws(ValidatorException::class)
            public override fun validate(input: String) {
                try {
                    val r = Range.parseFromStepTo(input)
                    val nSlices = ((r.to - r.from) / r.step).toInt()
                    if (r.from > r.to) {
                        throw RuntimeException("Z range \"from\" value (" + r.from + ") must be smaller than \"to\" value (" + r.to + ").")
                    }
                    if (nSlices < 1) {
                        throw RuntimeException("Invalid range: Must have at least one slice.")
                    }
                } catch (ex: RuntimeException) {
                    throw ValidatorException(ex)
                }
            }
        }, "-500:100:500", threeDCondition)
        colorizationLut = parameters.createStringField("pickedlut", object : Validator<String> {
            @Throws(ValidatorException::class)
            public override fun validate(input: String) {
                if (!lutPicker!!.lutExists(input)) {
                    throw ValidatorException("LUT \"$input\" does not exist!")
                }
            }
        }, "intensity", colorizeCondition)
    }

    public override fun setSize(sizeX: Double, sizeY: Double) {
        setSize(0.0, 0.0, sizeX, sizeY)
    }
    
    public override fun setSize(left: Double, top: Double, sizeX: Double, sizeY: Double) {
        this.left = left
        this.top = top
        this.sizeX = sizeX
        this.sizeY = sizeY
    }

    public override fun setZRange(from: Double, to: Double) {
        if (!threeDCheckBox!!.isEnabled) return;
        val r = Range.parseFromStepTo(zRangeTextField!!.text)
        r.from = roundDownTo(from, r.step)
        r.to = roundUpTo(to, r.step)
        zRangeTextField!!.text = r.toStrFromStepTo()
    }

    public override fun set3D(checked: Boolean) {
        threeDCheckBox!!.isSelected = checked
    }

    protected fun roundUpTo(value: Double, mod: Double): Double {
        return (value + mod - modulo(value, mod))
    }

    protected fun roundDownTo(value: Double, mod: Double): Double {
        if (value > 0) {
            return (value - modulo(value, mod))
        } else {
            return (value - mod - modulo(value, mod))
        }
    }

    protected fun modulo(value: Double, mod: Double): Double {
        return (value - (value / mod).toInt().toDouble()*mod)
    }

    public override fun getRepaintFrequency(): Int {
        return repaintFrequency!!.getValue()
    }

    public fun setShowRepaintFrequency(show: Boolean): Unit {
        this.showRepaintFrequency = show
    }

    public override fun getOptionsPanel(): JPanel {
        val panel = JPanel(GridBagLayout())

        val resolutionTextField = JTextField("", 20)
        parameters.registerComponent(magnification, resolutionTextField)
        val repaintFrequencyTextField = JTextField("", 20)
        parameters.registerComponent(repaintFrequency, repaintFrequencyTextField)
        panel.add(JLabel("Magnification:"), GridBagHelper.leftCol())
        panel.add(resolutionTextField, GridBagHelper.rightCol())

        if(showRepaintFrequency) {
            panel.add(JLabel("Update frequency [frames]:"), GridBagHelper.leftCol())
            panel.add(repaintFrequencyTextField, GridBagHelper.rightCol())
        }

        val zRangeLabel = JLabel("Z range (from:step:to) [nm]:")
        zRangeTextField = JTextField("", 20)
        parameters.registerComponent(zRange, zRangeTextField)
        threeDCheckBox = JCheckBox("", true)
        parameters.registerComponent(threeD, threeDCheckBox)
        val colorizeCheckBox = JCheckBox("", true)
        val colorizeLabel = JLabel("Colorize Z:")
        val lutPickerLabel = JLabel("Lookup Table:")
        parameters.registerComponent(colorize, colorizeCheckBox)
        parameters.registerComponent(colorizationLut, lutPicker)
        threeDCheckBox!!.addItemListener {
            colorizeLabel!!.setEnabled(threeDCheckBox!!.isSelected())
            colorizeCheckBox!!.isEnabled = threeDCheckBox!!.isSelected()
            lutPickerLabel!!.setEnabled(threeDCheckBox!!.isSelected())
            lutPicker!!.setEnabled(threeDCheckBox!!.isSelected())
            zRangeLabel!!.setEnabled(threeDCheckBox!!.isSelected())
            zRangeTextField!!.setEnabled(threeDCheckBox!!.isSelected())
        }
        colorizeCheckBox.addItemListener { }
        panel.add(JLabel("3D:"), GridBagHelper.leftCol())
        panel.add(threeDCheckBox, GridBagHelper.rightCol())
        panel.add(colorizeLabel, GridBagHelper.leftCol())
        panel.add(colorizeCheckBox, GridBagHelper.rightCol())
        panel.add(lutPicker, GridBagHelper.rightCol())
        panel.add(lutPickerLabel, GridBagHelper.leftCol())
        panel.add(zRangeLabel, GridBagHelper.leftCol())
        panel.add(zRangeTextField, GridBagHelper.rightCol())

        return panel
    }

    public override fun getImplementation()
            = getMethod()

    protected abstract fun getMethod(): IncrementalRenderingMethod
}