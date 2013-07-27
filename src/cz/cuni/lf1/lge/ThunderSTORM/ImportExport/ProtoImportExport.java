package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.ResultsTable.Results;
import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.ResultsTable.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
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
        
        rt.reset();
        
        Results results = Results.parseFrom(new FileInputStream(fp));
        
        int r = 0, nrows = results.getMoleculeCount();
        for(Molecule mol : results.getMoleculeList()) {
            rt.addRow();
            //if(mol.hasId()) rt.addValue(IJResultsTable.COLUMN_ID, mol.getId());   // skip!
            if(mol.hasX()) rt.addValue(PSFInstance.X, mol.getX());
            if(mol.hasY()) rt.addValue(PSFInstance.Y, mol.getY());
            if(mol.hasZ()) rt.addValue(PSFInstance.Z, mol.getZ());
            if(mol.hasSigma()) rt.addValue(PSFInstance.SIGMA, mol.getSigma());
            if(mol.hasSigma2()) rt.addValue(PSFInstance.SIGMA2, mol.getSigma2());
            if(mol.hasIntensity()) rt.addValue(PSFInstance.INTENSITY, mol.getIntensity());
            if(mol.hasBackground()) rt.addValue(PSFInstance.BACKGROUND, mol.getBackground());
            
            IJ.showProgress((double)(r++) / (double)nrows);
        }
    }

    @Override
    public void exportToFile(String fp, IJResultsTable.View rt, Vector<String> columns) throws FileNotFoundException, IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        assert(columns != null);
        
        int ncols = columns.size(), nrows = rt.getRowCount();
        String [] headers = new String[ncols];
        columns.toArray(headers);
        
        Results.Builder results = Results.newBuilder();
        for(int r = 0; r < nrows; r++) {
            Molecule.Builder mol = Molecule.newBuilder();
            for(int c = 0; c < ncols; c++) {
                double value = rt.getValue(headers[c],r);
                if(IJResultsTable.COLUMN_ID.equals(headers[c])) {
                    mol.setId((int)value);
                } else if(PSFInstance.X.equals(headers[c])) {
                    mol.setX(value);
                } else if(PSFInstance.Y.equals(headers[c])) {
                    mol.setY(value);
                } else if(PSFInstance.Z.equals(headers[c])) {
                    mol.setZ(value);
                } else if(PSFInstance.SIGMA.equals(headers[c])) {
                    mol.setSigma(value);
                } else if(PSFInstance.SIGMA2.equals(headers[c])) {
                    mol.setSigma2(value);
                } else if(PSFInstance.INTENSITY.equals(headers[c])) {
                    mol.setIntensity(value);
                } else if(PSFInstance.BACKGROUND.equals(headers[c])) {
                    mol.setBackground(value);
                } else {
                    throw new IllegalArgumentException("Parameter `" + headers[c] + "` is not supported in the current version of protocol buffer!");
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
