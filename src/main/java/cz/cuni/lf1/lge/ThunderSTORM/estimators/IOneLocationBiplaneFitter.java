package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;

public interface IOneLocationBiplaneFitter {
    Molecule fit(SubImage plane1, SubImage plane2);
}
