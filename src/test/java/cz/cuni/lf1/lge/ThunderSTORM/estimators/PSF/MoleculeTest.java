package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MoleculeTest {
    
    Molecule mol;
    int [] labels;
    double [] values;

    @Before
    public void setUp() {
        labels = new int[] { PSFModel.Params.X, PSFModel.Params.Y, PSFModel.Params.SIGMA1, PSFModel.Params.SIGMA2, PSFModel.Params.INTENSITY, PSFModel.Params.OFFSET, PSFModel.Params.BACKGROUND };
        values = new double[] { 0.0, 0.0, 1.4, 1.6, 1000.0, 100.0, 10.0 };
        mol = new Molecule(new PSFModel.Params(labels, values, false));
    }

    /**
     * Test of addParam method, of class Molecule.
     */
    @Test
    public void testAddParam() {
        System.out.println("addParam");
        Molecule m = mol.clone(mol.descriptor.clone()); // clone so the `mol` remains the same
        m.addParam("test", Units.PHOTON, 2000.0);
        //
        assertTrue(m.hasParam("test"));
        assertEquals(2000.0, m.getParam("test"), 0.0);
        //
        int index = m.descriptor.getParamIndex("test");
        assertEquals(m.values.length-1, index);
        assertEquals(2000.0, m.values[index], 0.0);
        //
        int column = m.descriptor.getParamColumn("test");
        assertEquals(m.descriptor.getParamsCount()-1, column);
        assertEquals(2000.0, m.getParam("test"), 0.0);
        assertEquals("test", m.getParamNameAtColumn(column));
    }

    /**
     * Test of insertParamAt method, of class Molecule.
     */
    @Test
    public void testInsertParamAt() {
        System.out.println("insertParamAt");
        Molecule m = mol.clone(mol.descriptor.clone()); // clone so the `mol` remains the same
        int column = 2;
        m.insertParamAt(column, "test", Units.PHOTON, 2000.0);
        //
        assertTrue(m.hasParam("test"));
        assertEquals(2000.0, m.getParam("test"), 0.0);
        //
        int index = m.descriptor.getParamIndex("test");
        assertEquals(m.values.length-1, index);
        assertEquals(2000.0, m.values[index], 0.0);
        //
        assertEquals(column, m.descriptor.getParamColumn("test"));
        assertEquals(2000.0, m.getParam("test"), 0.0);
        assertEquals("test", m.getParamNameAtColumn(column));
        //
        for(int c = 0; c < column; c++) {
            assertEquals(mol.getParamAtColumn(c), m.getParamAtColumn(c), 0.0);
            assertEquals(mol.getParamNameAtColumn(c), m.getParamNameAtColumn(c));
        }
        for(int c = column + 1; c < m.descriptor.getParamsCount(); c++) {
            assertEquals(mol.getParamAtColumn(c-1), m.getParamAtColumn(c), 0.0);
            assertEquals(mol.getParamNameAtColumn(c-1), m.getParamNameAtColumn(c));
        }
    }

    /**
     * Test of methods hasParam, getParam, getParamAt, and getParamNameAt of class Molecule.
     */
    @Test
    public void testGetHasParamAtNameAt() {
        System.out.println("hasParam, getParam, getParamAt, getParamNameAt");
        for(int c = 0; c < labels.length; c++) {
            String label = PSFModel.Params.getParamLabel(labels[c]);
            assertTrue(mol.hasParam(label));
            assertEquals(values[c], mol.getParam(label), 0.0);
            assertEquals(label, mol.getParamNameAtColumn(c));
        }
    }

    /**
     * Test of setParam and setParamAt methods, of class Molecule.
     */
    @Test
    public void testSetParamAt() {
        System.out.println("setParam, setParamAt");
        Molecule m1 = mol.clone(mol.descriptor.clone()); // clone so the `mol` remains the same
        Molecule m2 = mol.clone(mol.descriptor.clone()); // clone so the `mol` remains the same
        //
        int paramIdx = 4;
        double newVal = 2000.0;
        assertEquals(values[paramIdx], m1.getParamAt(paramIdx), 0.0);
        assertEquals(values[paramIdx], m2.getParamAt(paramIdx), 0.0);
        //
        m1.setParam(PSFModel.Params.getParamLabel(paramIdx), newVal);
        m2.setParamAt(paramIdx, newVal);
        assertEquals(newVal, m1.getParamAt(paramIdx), 0.0);
        assertEquals(newVal, m2.getParamAt(paramIdx), 0.0);
    }

    /**
     * Test of getX and setX methods, of class Molecule.
     */
    @Test
    public void testGetSetX() {
        System.out.println("getX, setX");
        //
        Molecule m1 = mol.clone(mol.descriptor.clone()); // clone so the `mol` remains the same
        assertEquals(values[0], m1.getX(), 0.0);
        m1.setX(2.2);
        assertEquals(2.2, m1.getX(), 0.0);
    }
    
    /**
     * Test of getY and setY methods, of class Molecule.
     */
    @Test
    public void testGetSetY() {
        System.out.println("getY, setY");
        //
        Molecule m1 = mol.clone(mol.descriptor.clone()); // clone so the `mol` remains the same
        assertEquals(values[0], m1.getY(), 0.0);
        m1.setY(-1.9);
        assertEquals(-1.9, m1.getY(), 0.0);
    }
    
    /**
     * Test of getZ and setZ methods, of class Molecule.
     */
    @Test
    public void testGetSetZ() {
        System.out.println("getZ, setZ");
        //
        Molecule m1 = mol.clone(mol.descriptor.clone()); // clone so the `mol` remains the same
        assertEquals(0.0, m1.getZ(), 0.0);  // if Z is not present, return zero
        //
        Molecule m2 = new Molecule(new PSFModel.Params(new int[]{PSFModel.Params.X, PSFModel.Params.Y, PSFModel.Params.Z}, new double[]{-0.2, 0.3, 150.0}, false));
        assertEquals(150.0, m2.getZ(), 0.0);
        m2.setZ(250.0);
        assertEquals(250.0, m2.getZ(), 0.0);
    }

}