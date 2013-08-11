package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.ResultsTable.Results;
import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.ResultsTable.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.ResultsTable.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import ij.IJ;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class ProtoImportExport implements IImportExport {
    
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
            if(mol.hasFrame()) fields.add(PSFInstance.LABEL_FRAME);
            if(mol.hasX()) fields.add(PSFModel.Params.LABEL_X);
            if(mol.hasY()) fields.add(PSFModel.Params.LABEL_Y);
            if(mol.hasZ()) fields.add(PSFModel.Params.LABEL_Z);
            if(mol.hasSigma()) fields.add(PSFModel.Params.LABEL_SIGMA);
            if(mol.hasSigma1()) fields.add(PSFModel.Params.LABEL_SIGMA1);
            if(mol.hasSigma2()) fields.add(PSFModel.Params.LABEL_SIGMA2);
            if(mol.hasIntensity()) fields.add(PSFModel.Params.LABEL_INTENSITY);
            if(mol.hasBackground()) fields.add(PSFModel.Params.LABEL_BACKGROUND);
            if(mol.hasDetections()) fields.add(PSFInstance.LABEL_DETECTIONS);
            if(!rt.columnNamesEqual(fields.toArray(new String[0]))) {
                throw new IOException("Labels in the file do not correspond to the header of the table (excluding '" + PSFInstance.LABEL_ID + "')!");
            }
            //
            // Then fill the table
            rt.addRow();
            //if(mol.hasId()) rt.addValue((double)mol.getId(), PSFInstance.LABEL_ID); // skip! --> ncols = 1
            if(mol.hasFrame()) rt.addValue((double)mol.getFrame(), PSFInstance.LABEL_FRAME);
            if(mol.hasX()) rt.addValue(mol.getX(), PSFModel.Params.LABEL_X);
            if(mol.hasY()) rt.addValue(mol.getY(), PSFModel.Params.LABEL_Y);
            if(mol.hasZ()) rt.addValue(mol.getZ(), PSFModel.Params.LABEL_Z);
            if(mol.hasSigma()) rt.addValue(mol.getSigma(), PSFModel.Params.LABEL_SIGMA);
            if(mol.hasSigma1()) rt.addValue(mol.getSigma1(), PSFModel.Params.LABEL_SIGMA1);
            if(mol.hasSigma2()) rt.addValue(mol.getSigma2(), PSFModel.Params.LABEL_SIGMA2);
            if(mol.hasIntensity()) rt.addValue(mol.getIntensity(), PSFModel.Params.LABEL_INTENSITY);
            if(mol.hasBackground()) rt.addValue(mol.getBackground(), PSFModel.Params.LABEL_BACKGROUND);
            if(mol.hasDetections()) rt.addValue((double)mol.getFrame(), PSFInstance.LABEL_DETECTIONS);
            
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
                if(PSFInstance.LABEL_ID.equals(name)) {
                    units.setId(unit);
                } else if(PSFInstance.LABEL_FRAME.equals(name)) {
                    units.setFrame(unit);
                } else if(PSFModel.Params.LABEL_X.equals(name)) {
                    units.setX(unit);
                } else if(PSFModel.Params.LABEL_Y.equals(name)) {
                    units.setY(unit);
                } else if(PSFModel.Params.LABEL_Z.equals(name)) {
                    units.setZ(unit);
                } else if(PSFModel.Params.LABEL_SIGMA.equals(name)) {
                    units.setSigma(unit);
                } else if(PSFModel.Params.LABEL_SIGMA1.equals(name)) {
                    units.setSigma1(unit);
                } else if(PSFModel.Params.LABEL_SIGMA2.equals(name)) {
                    units.setSigma2(unit);
                } else if(PSFModel.Params.LABEL_INTENSITY.equals(name)) {
                    units.setIntensity(unit);
                } else if(PSFModel.Params.LABEL_BACKGROUND.equals(name)) {
                    units.setBackground(unit);
                } else if(PSFInstance.LABEL_DETECTIONS.equals(name)) {
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
                if(PSFInstance.LABEL_ID.equals(name)) {
                    mol.setId((int)value);
                } else if(PSFInstance.LABEL_FRAME.equals(name)) {
                    mol.setFrame((int)value);
                } else if(PSFModel.Params.LABEL_X.equals(name)) {
                    mol.setX(value);
                } else if(PSFModel.Params.LABEL_Y.equals(name)) {
                    mol.setY(value);
                } else if(PSFModel.Params.LABEL_Z.equals(name)) {
                    mol.setZ(value);
                } else if(PSFModel.Params.LABEL_SIGMA.equals(name)) {
                    mol.setSigma(value);
                } else if(PSFModel.Params.LABEL_SIGMA1.equals(name)) {
                    mol.setSigma1(value);
                } else if(PSFModel.Params.LABEL_SIGMA2.equals(name)) {
                    mol.setSigma2(value);
                } else if(PSFModel.Params.LABEL_INTENSITY.equals(name)) {
                    mol.setIntensity(value);
                } else if(PSFModel.Params.LABEL_BACKGROUND.equals(name)) {
                    mol.setBackground(value);
                } else if(PSFInstance.LABEL_DETECTIONS.equals(name)) {
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
