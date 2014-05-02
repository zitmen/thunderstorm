package cz.cuni.lf1.lge.ThunderSTORM.util;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.LABEL_ID;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_X;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Y;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.junit.Test;
import static org.junit.Assert.*;

public class MoleculeMatcherTest {
    
    /**
     * Test of matchMolecules method, of class MoleculeMatcher.
     */
    @Test
    public void testMatchMolecules() {
        System.out.println("matchMolecules");
        //
        // Initialize the input data:
        MoleculeDescriptor desc = new MoleculeDescriptor(new String[] { LABEL_ID, LABEL_X, LABEL_Y }, new Units[] { Units.UNITLESS, Units.PIXEL, Units.PIXEL });
        Molecule gt1 = new Molecule(desc, new double[] { 1.0, 0.7710453838725644, 0.3536005585780880 });
        Molecule gt2 = new Molecule(desc, new double[] { 2.0, 0.7586700850084991, 0.4702063366092425 });
        Molecule gt3 = new Molecule(desc, new double[] { 3.0, 0.7208816979938985, 0.5172187710065674 });
        Molecule det1 = new Molecule(desc, new double[] { 1.0, 0.6171594495632347, 0.4949343818140205 });
	    Molecule det2 = new Molecule(desc, new double[] { 2.0, 0.3748525177688802, 0.3864459210533293 });
	    Molecule det3 = new Molecule(desc, new double[] { 3.0, 0.1256085534772942, 0.5591895052957198 });
        List<Molecule> gt = Arrays.asList(new Molecule[] { gt1, gt2, gt3 });
        List<Molecule> det = Arrays.asList(new Molecule[] { det1, det2, det3 });
        //
        // Initialize the correct results:
        List<Pair<Molecule, Molecule>> TP_correct = new Vector<Pair<Molecule, Molecule>>();
        List<Molecule> FP_correct = new Vector<Molecule>();
        List<Molecule> FN_correct = new Vector<Molecule>();
        TP_correct.add(new Pair<Molecule, Molecule>(gt1, det1));
        TP_correct.add(new Pair<Molecule, Molecule>(gt2, det2));
        TP_correct.add(new Pair<Molecule, Molecule>(gt3, det3));
        //
        // Run the procedure:
        List<Pair<Molecule, Molecule>> TP = new Vector<Pair<Molecule, Molecule>>();
        List<Molecule> FP = new Vector<Molecule>();
        List<Molecule> FN = new Vector<Molecule>();
        MoleculeMatcher matcher = new MoleculeMatcher(1.0, Units.PIXEL);
        matcher.matchMolecules(det, gt, TP, FP, FN);
        //
        // Validate the results:
        // 1) TP pairs
        Map gtTP = new HashMap();
        for(Pair<Molecule, Molecule> pair : TP_correct) {
            gtTP.put(pair.first, pair.second);
        }
        for(Pair<Molecule, Molecule> pair : TP) {
            assertEquals(gtTP.get(pair.first), pair.second);
        }
        // 2) FP & FN lists
        assertTrue(new HashSet(FP_correct).containsAll(FP));
        assertTrue(new HashSet(FN_correct).containsAll(FN));
    }
    
}
