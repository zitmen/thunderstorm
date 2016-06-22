package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.ResultsTable.Results;
import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.ResultsTable.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import ij.IJ;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProtoImportExport implements IImportExport {
    
    @Override
    public void importFromFile(String fp, GenericTable table, int startingFrame) throws FileNotFoundException, IOException {
        assert(table != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        Results results = Results.parseFrom(new FileInputStream(fp));
        
        double [] values = null;
        List<String> fields = new ArrayList<String>();
        int r = 0, nrows = results.getMoleculeCount();
        for(Molecule mol : results.getMoleculeList()) {
            //
            // First check if the columns correspond to the fields in the file
            // skip `mol.hasId()`!
            fields.clear();
            if(mol.hasFrame()) fields.add(MoleculeDescriptor.LABEL_FRAME);
            if(mol.hasX()) fields.add(PSFModel.Params.LABEL_X);
            if(mol.hasY()) fields.add(PSFModel.Params.LABEL_Y);
            if(mol.hasZ()) fields.add(PSFModel.Params.LABEL_Z);
            if(mol.hasZRel()) fields.add(PSFModel.Params.LABEL_Z_REL);
            if(mol.hasSigma()) fields.add(PSFModel.Params.LABEL_SIGMA);
            if(mol.hasSigma1()) fields.add(PSFModel.Params.LABEL_SIGMA1);
            if(mol.hasSigma2()) fields.add(PSFModel.Params.LABEL_SIGMA2);
            if(mol.hasIntensity()) fields.add(PSFModel.Params.LABEL_INTENSITY);
            if(mol.hasBackground()) fields.add(PSFModel.Params.LABEL_BACKGROUND);
            if(mol.hasDetections()) fields.add(MoleculeDescriptor.LABEL_DETECTIONS);
            if(mol.hasOffset()) fields.add(PSFModel.Params.LABEL_OFFSET);
            if(mol.hasThompsonCcd()) fields.add(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY);
            if(mol.hasThompsonEmccd()) fields.add(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY);
            if(!table.columnNamesEqual(fields.toArray(new String[0]))) {
                throw new IOException("Labels in the file do not correspond to the header of the table (excluding '" + MoleculeDescriptor.LABEL_ID + "')!");
            }
            if(values == null) {
                values = new double[fields.size()];
            }
            if(table.isEmpty()) {
                table.setDescriptor(new MoleculeDescriptor(fields.toArray(new String[0])));
                setUnits(table, results);
            }
            //
            // Then fill the table
            int i = 0;
            //if(mol.hasId()) rt.addValue((double)mol.getId(), PSFInstance.LABEL_ID); // skip! --> ncols = 1
            if(mol.hasFrame()) { values[i] = (double)mol.getFrame() + startingFrame-1; i++; }
            if(mol.hasX()) { values[i] = mol.getX(); i++; }
            if(mol.hasY()) { values[i] = mol.getY(); i++; }
            if(mol.hasZ()) { values[i] = mol.getZ(); i++; }
            if(mol.hasZRel())  { values[i] = mol.getZRel(); i++; }
            if(mol.hasSigma()) { values[i] = mol.getSigma(); i++; }
            if(mol.hasSigma1()) { values[i] = mol.getSigma1(); i++; }
            if(mol.hasSigma2()) { values[i] = mol.getSigma2(); i++; }
            if(mol.hasIntensity()) { values[i] = mol.getIntensity(); i++; }
            if(mol.hasBackground()) { values[i] = mol.getBackground(); i++; }
            if(mol.hasDetections()) { values[i] = (double)mol.getFrame(); i++; }
            if(mol.hasOffset()) { values[i] = mol.getOffset(); i++; }
            if(mol.hasThompsonCcd()) { values[i] = mol.getThompsonCcd(); i++; }
            if(mol.hasThompsonEmccd()) { values[i] = mol.getThompsonEmccd(); i++; }
            table.addRow(values);
            
            IJ.showProgress((double)(r++) / (double)nrows);
        }
        table.insertIdColumn();
        table.copyOriginalToActual();
        table.setActualState();
    }

    @Override
    public void exportToFile(String fp, int floatPrecision, GenericTable table, List<String> columns) throws FileNotFoundException, IOException {
        assert(table != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        assert(columns != null);
        
        int ncols = columns.size(), nrows = table.getRowCount();
        
        Results.Builder results = Results.newBuilder();
        cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.ResultsTable.Units.Builder units = cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.ResultsTable.Units.newBuilder();
        for(int c = 0; c < ncols; c++) {
            String name = columns.get(c);
            String unit = table.getColumnUnits(name).toString();
            if((unit != null) && !unit.trim().isEmpty()) {
                if(MoleculeDescriptor.LABEL_ID.equals(name)) {
                    units.setId(unit);
                } else if(MoleculeDescriptor.LABEL_FRAME.equals(name)) {
                    units.setFrame(unit);
                } else if(PSFModel.Params.LABEL_X.equals(name)) {
                    units.setX(unit);
                } else if(PSFModel.Params.LABEL_Y.equals(name)) {
                    units.setY(unit);
                } else if(PSFModel.Params.LABEL_Z.equals(name)) {
                    units.setZ(unit);
                } else if(PSFModel.Params.LABEL_Z_REL.equals(name)) {
                    units.setZRel(unit);
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
                } else if(MoleculeDescriptor.LABEL_DETECTIONS.equals(name)) {
                    units.setDetections(unit);
                } else if(PSFModel.Params.LABEL_OFFSET.equals(name)) {
                    units.setOffset(unit);
                } else if(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY.equals(name)) {
                    if(CameraSetupPlugIn.getIsEmGain()) {
                        units.setThompsonEmccd(unit);
                    } else {
                        units.setThompsonCcd(unit);
                    }
                } else {
                    throw new IllegalArgumentException("Parameter `" + columns.get(c) + "` is not supported in the current version of protocol buffer!");
                }
            }
        }
        results.setUnits(units);
        for(int r = 0; r < nrows; r++) {
            Molecule.Builder mol = Molecule.newBuilder();
            for(int c = 0; c < ncols; c++) {
                String name = columns.get(c);
                double value = table.getValue(r, name);
                if(MoleculeDescriptor.LABEL_ID.equals(name)) {
                    mol.setId((int)value);
                } else if(MoleculeDescriptor.LABEL_FRAME.equals(name)) {
                    mol.setFrame((int)value);
                } else if(PSFModel.Params.LABEL_X.equals(name)) {
                    mol.setX(value);
                } else if(PSFModel.Params.LABEL_Y.equals(name)) {
                    mol.setY(value);
                } else if(PSFModel.Params.LABEL_Z.equals(name)) {
                    mol.setZ(value);
                } else if(PSFModel.Params.LABEL_Z_REL.equals(name)) {
                    mol.setZRel(value);
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
                } else if(MoleculeDescriptor.LABEL_DETECTIONS.equals(name)) {
                    mol.setDetections((int)value);
                } else if(PSFModel.Params.LABEL_OFFSET.equals(name)) {
                    mol.setOffset(value);
                } else if(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY.equals(name)) {
                    if(CameraSetupPlugIn.getIsEmGain()) {
                        mol.setThompsonEmccd(value);
                    } else {
                        mol.setThompsonCcd(value);
                    }
                } else {
                    throw new IllegalArgumentException("Parameter `" + columns.get(c) + "` is not supported in the current version of protocol buffer!");
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

    private void setUnits(GenericTable table, Results results) {
        if(results.hasUnits()) {
            cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.ResultsTable.Units u = results.getUnits();
            if(u.hasFrame()) table.setColumnUnits(MoleculeDescriptor.LABEL_FRAME, Units.fromString(u.getFrame()));
            if(u.hasX()) table.setColumnUnits(PSFModel.Params.LABEL_X, Units.fromString(u.getX()));
            if(u.hasY()) table.setColumnUnits(PSFModel.Params.LABEL_Y, Units.fromString(u.getY()));
            if(u.hasZ()) table.setColumnUnits(PSFModel.Params.LABEL_Z, Units.fromString(u.getZ()));
            if(u.hasZRel()) table.setColumnUnits(PSFModel.Params.LABEL_Z_REL, Units.fromString(u.getZRel()));
            if(u.hasSigma()) table.setColumnUnits(PSFModel.Params.LABEL_SIGMA, Units.fromString(u.getSigma()));
            if(u.hasSigma1()) table.setColumnUnits(PSFModel.Params.LABEL_SIGMA1, Units.fromString(u.getSigma1()));
            if(u.hasSigma2()) table.setColumnUnits(PSFModel.Params.LABEL_SIGMA2, Units.fromString(u.getSigma2()));
            if(u.hasIntensity()) table.setColumnUnits(PSFModel.Params.LABEL_INTENSITY, Units.fromString(u.getIntensity()));
            if(u.hasBackground()) table.setColumnUnits(PSFModel.Params.LABEL_BACKGROUND, Units.fromString(u.getBackground()));
            if(u.hasDetections()) table.setColumnUnits(MoleculeDescriptor.LABEL_DETECTIONS, Units.fromString(u.getDetections()));
            if(u.hasOffset()) table.setColumnUnits(PSFModel.Params.LABEL_OFFSET, Units.fromString(u.getOffset()));
            if(u.hasThompsonCcd()) table.setColumnUnits(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY, Units.fromString(u.getThompsonCcd()));
            if(u.hasThompsonEmccd()) table.setColumnUnits(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY, Units.fromString(u.getThompsonEmccd()));
        }
    }

}
