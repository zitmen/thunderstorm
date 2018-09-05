package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import ij.IJ;
import ij.ImageJ;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

public class GenericTableTest {

    @BeforeClass
    public static void testSetup() {
        ImageJ.main(new String[]{"-"});
    }
    
    @AfterClass
    public static void testCleanup() {
        IJ.getInstance().quit();
    }
    
    /**
     * Test of columnNamesEqual method, of class GenericTable.
     */
/*    @Test
    public void testColumnNamesEqual() {
        System.out.println("columnNamesEqual");
        //
        GenericTable table = new GenericTableImpl();
        table.addRow(new Molecule(new PSFModel.Params(new int[]{PSFModel.Params.X, PSFModel.Params.Y}, new double[]{0.1, 0.2}, false)));
        assertTrue(table.columnNamesEqual(new String[]{PSFModel.Params.LABEL_X, PSFModel.Params.LABEL_Y}));
        assertTrue(table.columnNamesEqual(new String[]{PSFModel.Params.LABEL_Y, PSFModel.Params.LABEL_X}));
        assertFalse(table.columnNamesEqual(new String[]{PSFModel.Params.LABEL_X}));
        assertFalse(table.columnNamesEqual(new String[]{PSFModel.Params.LABEL_Y}));
        assertFalse(table.columnNamesEqual(new String[]{PSFModel.Params.LABEL_X, PSFModel.Params.LABEL_Y, PSFModel.Params.LABEL_Z}));
    }*/
    
    private class GenericTableImpl extends GenericTable<GenericTableWindow> {

        public GenericTableImpl() {
            super(new GenericTableWindowImpl("test"));
        }
        
        @Override
        public String getFrameTitle() {
            return "test";
        }

        @Override
        public String getTableIdentifier() {
            return "test";
        }
        
    }
    
    private class GenericTableWindowImpl extends GenericTableWindow {

        public GenericTableWindowImpl(String frameTitle) {
            super(frameTitle);
        }
    }

}