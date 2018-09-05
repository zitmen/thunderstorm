
package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.util.Comparator;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;

public class MoleculeXYZComparator implements Comparator<Molecule> {

	@Override
	public int compare(Molecule m1, Molecule m2) {
		if (m1.getX() == m2.getX()) {
			if (m1.getY() == m2.getY()) {
				return ((m1.getZ() - m2.getZ()) > 0 ? +1 : -1);
			}
			else {
				return ((m1.getY() - m2.getY()) > 0 ? +1 : -1);
			}
		}
		else {
			return ((m1.getX() - m2.getX()) > 0 ? +1 : -1);
		}
	}
}
