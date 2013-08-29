package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;

public class CentroidFitter  implements OneLocationFitter {

    @Override
    public Molecule fit(SubImage img) {
        assert(img.size == 3);  // only images 3x3 are allowed

        double maxVal = img.getMax(), n = img.values.length;
        double [] coordinates = new double[] { img.detectorX, img.detectorY };
        for(int i = 0; i < img.values.length; i++) {
            coordinates[0] += (img.values[i] / maxVal) * img.xgrid[i] / n;
            coordinates[1] += (img.values[i] / maxVal) * img.ygrid[i] / n;
        }
                
        return new Molecule(new PSFModel.Params(new int[]{PSFModel.Params.X, PSFModel.Params.Y}, coordinates, false));
    }

}
