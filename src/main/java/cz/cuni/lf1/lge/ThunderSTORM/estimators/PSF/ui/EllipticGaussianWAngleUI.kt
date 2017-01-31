package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusCalibration
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianWAnglePSF
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.StringValidatorFactory
import cz.cuni.lf1.lge.ThunderSTORM.util.Range
import cz.cuni.lf1.lge.ThunderSTORM.util.RangeValidatorFactory
import ij.IJ
import org.yaml.snakeyaml.Yaml
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.FileNotFoundException
import java.io.FileReader
import javax.swing.*

private const val name = "Eliptical Gaussian (3D astigmatism)"

public class EllipticGaussianWAngleUI : PsfUI() {

    private val CALIBRATION = parameters.createStringField("calibration", StringValidatorFactory.fileExists(), "")
    private val Z_RANGE = parameters.createStringField("z_range", RangeValidatorFactory.fromTo(), "-300:+300")
    
    private var calibration: DefocusCalibration? = null
    
    public override fun getName()
            = name

    public override fun getOptionsPanel(): JPanel {
        val calibrationTextField = JTextField("", 20)
        val zRangeTextField = JTextField("", 20)
        parameters.registerComponent(CALIBRATION, calibrationTextField)
        parameters.registerComponent(Z_RANGE, zRangeTextField)
        
        val panel = JPanel(GridBagLayout())
        panel.add(JLabel("Calibration file:"), GridBagHelper.leftCol())
        
        val findCalibrationButton = JButton("...")
        findCalibrationButton.setMargin(Insets(1, 1, 1, 1))
        findCalibrationButton.addActionListener(object : ActionListener {
            public override fun actionPerformed(e: ActionEvent) {
                val fileChooser = JFileChooser(IJ.getDirectory("image"))
                val userAction = fileChooser.showOpenDialog(null)
                if(userAction == JFileChooser.APPROVE_OPTION) {
                    calibrationTextField.setText(fileChooser.getSelectedFile().getPath())
                }
            }
        })
        val calibrationPanel = object : JPanel(BorderLayout()) {
            public override fun getPreferredSize(): Dimension {
                return ((parameters.getRegisteredComponent(Z_RANGE)) as JTextField).getPreferredSize()
            }
        }
        calibrationPanel.add(calibrationTextField, BorderLayout.CENTER)
        calibrationPanel.add(findCalibrationButton, BorderLayout.EAST)
        panel.add(calibrationPanel, GridBagHelper.rightCol())
        
        panel.add(JLabel("Z-range (from:to) [nm]:"), GridBagHelper.leftCol())
        panel.add(zRangeTextField, GridBagHelper.rightCol())
        
        parameters.loadPrefs()
        return panel
    }

    public override fun getImplementation()
            = EllipticGaussianWAnglePSF(1.6, 0.0)

    public override fun getAngle(): Double {
        if(calibration == null) {
            calibration = loadCalibration(CALIBRATION.getValue())
        }
        return calibration!!.getAngle()  // [rad]
    }

    public override fun getZRange(): Range {
        return Range.parseFromTo(Z_RANGE.getValue())
    }

    public override fun getSigma1(z: Double): Double {
        if(calibration == null) {
            calibration = loadCalibration(CALIBRATION.getValue())
        }
        return calibration!!.getSigma1(z)
    }

    public override fun getSigma2(z: Double): Double {
        if(calibration == null) {
            calibration = loadCalibration(CALIBRATION.getValue())
        }
        return calibration!!.getSigma2(z)
    }

    public override fun is3D()
            = true
    
    private fun loadCalibration(calibrationFilePath: String): DefocusCalibration {
        try {
            val yaml = Yaml()
            val loaded = yaml.load(FileReader(calibrationFilePath))
            return loaded as DefocusCalibration
        } catch(ex: FileNotFoundException) {
            throw RuntimeException("Could not read calibration file.", ex)
        } catch(ex: ClassCastException) {
            throw RuntimeException("Could not parse calibration file.", ex)
        }
    }
    
}
