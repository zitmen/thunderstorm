package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.ResultsTable.Results;
import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.ResultsTable.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.ResultsTable.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import ij.IJ;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class ProtoImportExport implements IImportExport {
    
    public static final String FRAME = "frame";
    public static final String DETECTIONS = "detections";

    @Override
    public void importFromFile(String fp, IJResultsTable rt) throws FileNotFoundException, IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        Results results = Results.parseFrom(new FileInputStream(fp));
        
        Vector<String> fields = new Vector<String>();
        int r = 0, nrows = results.getMoleculeCount();
        for(Molecule mol : results.getMoleculeList()) {
            //
            // First check if the columns correspond to the fields in the file
            // skip `mol.hasId()`!
            fields.clear();
            if(mol.hasFrame()) fields.add(FRAME);
            if(mol.hasX()) fields.add(PSFInstance.X_POS);
            if(mol.hasY()) fields.add(PSFInstance.Y_POS);
            if(mol.hasZ()) fields.add(PSFInstance.Z_POS);
            if(mol.hasSigma()) fields.add(PSFInstance.SIGMA);
            if(mol.hasSigma2()) fields.add(PSFInstance.SIGMA2);
            if(mol.hasIntensity()) fields.add(PSFInstance.INTENSITY);
            if(mol.hasBackground()) fields.add(PSFInstance.BACKGROUND);
            if(mol.hasFrame()) fields.add(DETECTIONS);
            if(!rt.columnNamesEqual(fields.toArray(new String[0]))) {
                throw new IOException("Labels in the file do not correspond to the header of the table (excluding '" + IJResultsTable.COLUMN_ID + "')!");
            }
            //
            // Then fill the table
            rt.addRow();
            //if(mol.hasId()) rt.addValue((double)mol.getId(), IJResultsTable.COLUMN_ID); // skip! --> ncols = 1
            if(mol.hasFrame()) rt.addValue((double)mol.getFrame(), FRAME);
            if(mol.hasX()) rt.addValue(mol.getX(), PSFInstance.X_POS);
            if(mol.hasY()) rt.addValue(mol.getY(), PSFInstance.Y_POS);
            if(mol.hasZ()) rt.addValue(mol.getZ(), PSFInstance.Z_POS);
            if(mol.hasSigma()) rt.addValue(mol.getSigma(), PSFInstance.SIGMA);
            if(mol.hasSigma2()) rt.addValue(mol.getSigma2(), PSFInstance.SIGMA2);
            if(mol.hasIntensity()) rt.addValue(mol.getIntensity(), PSFInstance.INTENSITY);
            if(mol.hasBackground()) rt.addValue(mol.getBackground(), PSFInstance.BACKGROUND);
            if(mol.hasFrame()) rt.addValue((double)mol.getFrame(), DETECTIONS);
            
            IJ.showProgress((double)(r++) / (double)nrows);
        }
        rt.copyOriginalToActual();
        rt.setActualState();
    }

    @Override
    public void exportToFile(String fp, IJResultsTable rt, Vector<String> columns) throws FileNotFoundException, IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        assert(columns != null);
        
        int ncols = columns.size(), nrows = rt.getRowCount();
        
        Results.Builder results = Results.newBuilder();
        Units.Builder units = Units.newBuilder();
        for(int c = 0; c < ncols; c++) {
            String name = columns.elementAt(c);
            String unit = rt.getColumnUnits(name);
            if((unit != null) && !unit.trim().isEmpty()) {
                if(IJResultsTable.COLUMN_ID.equals(name)) {
                    units.setId(unit);
                } else if(FRAME.equals(name)) {
                    units.setFrame(unit);
                } else if(PSFInstance.X_POS.equals(name)) {
                    units.setX(unit);
                } else if(PSFInstance.Y_POS.equals(name)) {
                    units.setY(unit);
                } else if(PSFInstance.Z_POS.equals(name)) {
                    units.setZ(unit);
                } else if(PSFInstance.SIGMA.equals(name)) {
                    units.setSigma(unit);
                } else if(PSFInstance.SIGMA2.equals(name)) {
                    units.setSigma2(unit);
                } else if(PSFInstance.INTENSITY.equals(name)) {
                    units.setIntensity(unit);
                } else if(PSFInstance.BACKGROUND.equals(name)) {
                    units.setBackground(unit);
                } else if(DETECTIONS.equals(name)) {
                    units.setDetections(unit);
                } else {
                    throw new IllegalArgumentException("Parameter `" + columns.elementAt(c) + "` is not supported in the current version of protocol buffer!");
                }
            }
        }
        results.setUnits(units);
        for(int r = 0; r < nrows; r++) {
            Molecule.Builder mol = Molecule.newBuilder();
            for(int c = 0; c < ncols; c++) {
                String name = columns.elementAt(c);
                double value = rt.getValue(r, name);
                if(IJResultsTable.COLUMN_ID.equals(name)) {
                    mol.setId((int)value);
                } else if(FRAME.equals(name)) {
                    mol.setFrame((int)value);
                } else if(PSFInstance.X_POS.equals(name)) {
                    mol.setX(value);
                } else if(PSFInstance.Y_POS.equals(name)) {
                    mol.setY(value);
                } else if(PSFInstance.Z_POS.equals(name)) {
                    mol.setZ(value);
                } else if(PSFInstance.SIGMA.equals(name)) {
                    mol.setSigma(value);
                } else if(PSFInstance.SIGMA2.equals(name)) {
                    mol.setSigma2(value);
                } else if(PSFInstance.INTENSITY.equals(name)) {
                    mol.setIntensity(value);
                } else if(PSFInstance.BACKGROUND.equals(name)) {
                    mol.setBackground(value);
                } else if(DETECTIONS.equals(name)) {
                    mol.setDetections((int)value);
                } else {
                    throw new IllegalArgumentException("Parameter `" + columns.elementAt(c) + "` is not supported in the current version of protocol buffer!");
                }
            }
            results.addMolecule(mol);
            IJ.showProgress((double)r / (double)nrows);
        }
        
        FileOutputStream output = new FileOutputStream(fp);
        results.build().writeTo(output);
        output.close();
    }

    @Override
    public String getName() {
        return "Google Protocol Buffer";
    }

    @Override
    public String getSuffix() {
        return "gpb";
    }

}
