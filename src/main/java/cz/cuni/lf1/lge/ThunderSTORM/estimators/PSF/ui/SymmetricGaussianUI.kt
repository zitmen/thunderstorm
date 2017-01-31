package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.SymmetricGaussianPSF
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper
import cz.cuni.lf1.lge.ThunderSTORM.util.Range
import cz.cuni.lf1.lge.ThunderSTORM.util.RangeValidatorFactory

import javax.swing.*
import java.awt.*

/**
 * Note: here `sigma` and `z` are not semantically correct; The reason for this
 *       is that using FWMH range we simulate a linear defocus with the lower
 *       value of the range being in focus; this is simply a convenient way of
 *       implementation for data generator; it has no other semantical meaning
 */

private const val name = "Gaussian"

public class SymmetricGaussianUI : PsfUI() {

    private val fwhm_range = parameters.createStringField("fwhm_range", RangeValidatorFactory.fromTo(), "200:350")
    private var zRange: Range? = null

    public override fun getName()
            = name

    public override fun getOptionsPanel(): JPanel {
        val fwhmTextField = JTextField("", 20)
        parameters.registerComponent(fwhm_range, fwhmTextField)
        
        val panel = JPanel(GridBagLayout())
        panel.add(JLabel("FWHM range (from:to) [nm]:"), GridBagHelper.leftCol())
        panel.add(fwhmTextField, GridBagHelper.rightCol())
        
        parameters.loadPrefs()
        return panel
    }

    public override fun getImplementation()
            = SymmetricGaussianPSF(1.6)

    public override fun getAngle()
            = 0.0

    public override fun getZRange(): Range {
        if(zRange == null) {
            zRange = Range.parseFromTo(fwhm_range.getValue(), Units.NANOMETER, Units.PIXEL)
            zRange!!.from = fwhm2sigma(zRange!!.from)
            zRange!!.to = fwhm2sigma(zRange!!.to)
        }
        return zRange!!
    }

    public override fun getSigma1(z: Double): Double {
        if(!zRange!!.isIn(z)) {
            return Double.NaN
        }
        return z
    }

    public override fun getSigma2(z: Double)
            = getSigma1(z)

    public override fun is3D()
            = false
}