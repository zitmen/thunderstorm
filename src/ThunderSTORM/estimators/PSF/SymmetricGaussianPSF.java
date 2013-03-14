package ThunderSTORM.estimators.PSF;

public class SymmetricGaussianPSF extends GaussianPSF {
    
    // TODO: dat atributy trid do private, abych mohl zamaskovat, ze sigma je ve skutecnosti sigma_x = sigma_y;
    // TODO: pridat settery a gettery....tam se to prave zamaskuje!
    // TODO: pro zrychleni fitovani bude potreba metode Gradient predavat uz hotovy objekt PSF, aby se furt dokola nealokovala nova pamet!
    
    @Override
    public PSF getGradient() {
        // TODO: vratit pouze sigma!!
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
