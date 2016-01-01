package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;

// TODO: this should be handled differently, ideally by injecting an interface
//       to allow implementation independent of input data!
//       --> there would be only a single OneLocationFitter interface fit the same fit method
public interface OneLocationBiplaneFitter {
    Molecule fit(SubImage plane1, SubImage plane2) throws Exception;
}
