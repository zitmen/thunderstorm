package ThunderSTORM.estimators.PSF;

public class RotatedGaussian extends GaussianPSF {

    private double angle;   // in radians
    
    @Override
    public PSF getGradient() {
        // TODO: vracet i gradient uhlu - nejdrive bude potreba napsat funkci getValueAt a pak podle toho odvodit ten gradient
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getValueAt(PSF where) {
        // TODO: spocitat pomoci toho vzorce, co mam v diplomce nebo zkusit nasobeni rotacni matici  a nasobit tak vystup ze super.getValueAt(where)! porovnat vysledky, snad budou stejny!!
        // TODO: vytvorit v ThunderSTORM.util i neco na tyhle geometricky transformace, protoze se to pak muze hodit mimo jine i u renderovani
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
