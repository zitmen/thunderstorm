package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import com.google.protobuf.ExtensionRegistry;
import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.proto.TSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units.DIGITAL;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units.PHOTON;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units.PIXEL;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.IOUtils;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqrt;
import ij.IJ;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.DoubleList;
import static org.apache.commons.math3.util.FastMath.log;

public class TSFImportExport implements IImportExport {

    @Override
    public void importFromFile(String fp, GenericTable table, int startingFrame) throws FileNotFoundException, IOException {
        assert (table != null);
        assert (fp != null);
        assert (!fp.isEmpty());

        RandomAccessFile inputFile = null;
        InputStream inputStream = null;
        double FWHM_factor = 2 * sqrt(2 * log(2));

        try {
            inputFile = new RandomAccessFile(fp, "r");
            inputStream = new BufferedInputStream(new FileInputStream(inputFile.getFD()));

            int magic = inputFile.readInt();
            assert magic == 0;
            long metadataOffset = inputFile.readLong();
            inputFile.seek(metadataOffset + 12);

            TSF.SpotList spotList = TSF.SpotList.parseDelimitedFrom(inputStream);

            Units locationUnits = Units.PIXEL;
            if(spotList.hasLocationUnits()) {
                locationUnits = translateLocationUnits(spotList.getLocationUnits());
            }
            Units intensityUnits = Units.DIGITAL;
            if(spotList.hasIntensityUnits()) {
                intensityUnits = translateIntensityUnits(spotList.getIntensityUnits());
            }
            long spotsCount = spotList.getNrSpots();
            List<String> columnNames = new ArrayList<String>();
            DoubleList values = new ArrayDoubleList();

            ExtensionRegistry extensions = ExtensionRegistry.newInstance();
            extensions.add(TSF.bkgstd);
            extensions.add(TSF.detections);

            inputFile.seek(12);
            for(long i = 0; i < spotsCount; i++) {
                TSF.Spot spot = TSF.Spot.parseDelimitedFrom(inputStream, extensions);
                columnNames.clear();
                values.clear();

                Units spotLocationUnits = spot.hasLocationUnits() ? translateLocationUnits(spot.getLocationUnits()) : locationUnits;
                Units spotIntensityUnits = spot.hasIntensityUnits() ? translateIntensityUnits(spot.getIntensityUnits()) : intensityUnits;
                if(spotLocationUnits != locationUnits || spotIntensityUnits != intensityUnits) {
                    throw new RuntimeException("Different units for each spot not supported yet.");
                }
                //required fields
                columnNames.add(MoleculeDescriptor.LABEL_ID);
                values.add(spot.getMolecule() + 1);

                columnNames.add(MoleculeDescriptor.LABEL_FRAME);
                values.add(spot.getFrame());

                columnNames.add(PSFModel.Params.LABEL_X);
                values.add(spot.getX());

                columnNames.add(PSFModel.Params.LABEL_Y);
                values.add(spot.getY());

                columnNames.add(PSFModel.Params.LABEL_INTENSITY);
                values.add(spot.getIntensity());

                //optional fields
                if(spot.hasZ()) {
                    columnNames.add(PSFModel.Params.LABEL_Z);
                    values.add(spot.getZ());
                }
                if(spot.hasZOriginal()) {
                    columnNames.add(PSFModel.Params.LABEL_Z_REL);
                    values.add(spot.getZOriginal());
                }
                if(spot.hasBackground()) {
                    columnNames.add(PSFModel.Params.LABEL_OFFSET);
                    values.add(spot.getBackground());
                }
                if(spot.hasWidth()) {
                    if(!spot.hasA() || (spotList.hasFitMode() && spotList.getFitMode() == TSF.FitMode.ONEAXIS)) {
                        columnNames.add(PSFModel.Params.LABEL_SIGMA);
                        values.add(spot.getWidth() / FWHM_factor);
                    } else {
                        columnNames.add(PSFModel.Params.LABEL_SIGMA1);
                        columnNames.add(PSFModel.Params.LABEL_SIGMA2);
                        values.add(spot.getWidth() / FWHM_factor * MathProxy.sqrt(spot.getA()));
                        values.add(spot.getWidth() / FWHM_factor / MathProxy.sqrt(spot.getA()));
                    }
                }
                if(spot.hasExtension(TSF.bkgstd)) {
                    columnNames.add(PSFModel.Params.LABEL_BACKGROUND);
                    values.add(spot.getExtension(TSF.bkgstd));
                }
                if(spot.hasExtension(TSF.detections)) {
                    columnNames.add(MoleculeDescriptor.LABEL_DETECTIONS);
                    values.add(spot.getExtension(TSF.detections));
                }
                if(spot.hasXPrecision()) {
                    columnNames.add(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY);
                    values.add(spot.getXPrecision());
                }

                if(table.isEmpty()) {
                    table.setDescriptor(new MoleculeDescriptor(columnNames.toArray(new String[0])));
                    table.setColumnUnits(PSFModel.Params.LABEL_X, locationUnits);
                    table.setColumnUnits(PSFModel.Params.LABEL_Y, locationUnits);
                    table.setColumnUnits(PSFModel.Params.LABEL_INTENSITY, intensityUnits);
                    if(spot.hasZ()) {
                        table.setColumnUnits(PSFModel.Params.LABEL_Z, locationUnits);
                    }
                    if(spot.hasZOriginal()) {
                        table.setColumnUnits(PSFModel.Params.LABEL_Z_REL, locationUnits);
                    }
                    if(spot.hasBackground()) {
                        table.setColumnUnits(PSFModel.Params.LABEL_OFFSET, intensityUnits);
                    }
                    if(spot.hasWidth()) {
                        if(!spot.hasA() || (spotList.hasFitMode() && spotList.getFitMode() == TSF.FitMode.ONEAXIS)) {
                            table.setColumnUnits(PSFModel.Params.LABEL_SIGMA, locationUnits);
                        } else {
                            table.setColumnUnits(PSFModel.Params.LABEL_SIGMA1, locationUnits);
                            table.setColumnUnits(PSFModel.Params.LABEL_SIGMA2, locationUnits);
                        }
                    }
                    if(spot.hasWidth() && spot.hasA()) {
                    }
                    if(spot.hasExtension(TSF.bkgstd)) {
                        table.setColumnUnits(PSFModel.Params.LABEL_BACKGROUND, intensityUnits);
                    }
                    if(spot.hasXPrecision()) {
                        table.setColumnUnits(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY, locationUnits);
                    }
                }
                table.addRow(values.toArray());
                IJ.showProgress((double) (i) / (double) spotsCount);
            }
            inputStream.close();
            table.copyOriginalToActual();
            table.setActualState();
            IJ.showProgress(1);
        } finally {
            IOUtils.closeQuietly(inputFile);
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public void exportToFile(String fp, int floatPrecision, GenericTable table, List<String> columns) throws FileNotFoundException, IOException {
        assert (table != null);
        assert (fp != null);
        assert (!fp.isEmpty());
        assert (columns != null);

        int nrows = table.getRowCount();
        Set<String> columnsSet = new HashSet<String>(columns);

        double FWHM_factor = 2 * sqrt(2 * log(2));  //tsf requires fwhm but we use stdev
        //use same units for all locations and same units for all intensities in TSF format
        Units locationUnits = table.getColumnUnits(PSFModel.Params.LABEL_X);
        Units intensityUnits = table.getColumnUnits(PSFModel.Params.LABEL_INTENSITY);

        RandomAccessFile outputFile = null;
        OutputStream outputStream = null;
        try {
            outputFile = new RandomAccessFile(fp, "rw");
            outputStream = new BufferedOutputStream(new FileOutputStream(outputFile.getFD()));
            //dataOutputStream = new DataOutputStream(outputStream);
            outputFile.writeInt(0);   //magic
            outputFile.writeLong(0);  //offset will be rewritten later

            //write spots
            for(int r = 0; r < nrows; r++) {
                Molecule mol = table.getRow(r);

                TSF.Spot.Builder spotBuilder = TSF.Spot.newBuilder();
                spotBuilder.setChannel(1);
                spotBuilder.setMolecule(r);
                if(mol.hasParam(MoleculeDescriptor.LABEL_FRAME)) {
                    spotBuilder.setFrame((int) mol.getParam(MoleculeDescriptor.LABEL_FRAME));
                }
                spotBuilder.setX((float) mol.getX(locationUnits));
                spotBuilder.setY((float) mol.getY(locationUnits));
                if(mol.hasParam(PSFModel.Params.LABEL_Z) && columnsSet.contains(PSFModel.Params.LABEL_Z)) {
                    spotBuilder.setZ((float) mol.getZ(locationUnits));
                }
                if(mol.hasParam(PSFModel.Params.LABEL_Z_REL) && columnsSet.contains(PSFModel.Params.LABEL_Z_REL)) {
                    spotBuilder.setZOriginal((float) mol.getParam(PSFModel.Params.LABEL_Z_REL, locationUnits));
                }
                spotBuilder.setIntensity(mol.hasParam(PSFModel.Params.LABEL_INTENSITY)
                        ? (float) mol.getParam(PSFModel.Params.LABEL_INTENSITY, intensityUnits)
                        : 1);
                if(mol.hasParam(PSFModel.Params.LABEL_OFFSET) && columnsSet.contains(PSFModel.Params.LABEL_OFFSET)) {
                    spotBuilder.setBackground((float) mol.getParam(PSFModel.Params.LABEL_OFFSET, intensityUnits));
                }
                if(mol.hasParam(PSFModel.Params.LABEL_SIGMA) && columnsSet.contains(PSFModel.Params.LABEL_SIGMA)) {
                    spotBuilder.setWidth((float) (FWHM_factor * mol.getParam(PSFModel.Params.LABEL_SIGMA, locationUnits)));
                }
                if(mol.hasParam(PSFModel.Params.LABEL_SIGMA2)) {
                    double fwhm1 = FWHM_factor * mol.getParam(PSFModel.Params.LABEL_SIGMA1, locationUnits);
                    double fwhm2 = FWHM_factor * mol.getParam(PSFModel.Params.LABEL_SIGMA2, locationUnits);

                    spotBuilder.setWidth((float) MathProxy.sqrt(fwhm1 * fwhm2));
                    spotBuilder.setA((float) (fwhm1 / fwhm2));
                }

                if(mol.hasParam(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY) && columnsSet.contains(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY)) {
                    float uncertainty = (float) mol.getParam(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY, locationUnits);
                    spotBuilder.setXPrecision(uncertainty);
                    spotBuilder.setYPrecision(uncertainty);
                }
                if(mol.hasParam(PSFModel.Params.LABEL_BACKGROUND)) {
                    spotBuilder.setExtension(TSF.bkgstd, (float) mol.getParam(PSFModel.Params.LABEL_BACKGROUND, intensityUnits));
                }
                if(mol.hasParam(MoleculeDescriptor.LABEL_DETECTIONS)) {
                    spotBuilder.setExtension(TSF.detections, (int) mol.getParam(MoleculeDescriptor.LABEL_DETECTIONS));
                }
                spotBuilder.build().writeDelimitedTo(outputStream);
                IJ.showProgress((double) r / (double) nrows);
            }
            //save spotlist offset
            outputStream.flush();
            long bytesWritten = outputFile.getFilePointer();

            //write spotlist
            TSF.SpotList.Builder spotlistBuilder = TSF.SpotList.newBuilder();
            spotlistBuilder.setApplicationId(1);
            spotlistBuilder.setNrSpots(nrows);
            spotlistBuilder.setPixelSize((float) CameraSetupPlugIn.getPixelSize());
            spotlistBuilder.setLocationUnits(translateLocationUnits(locationUnits));
            spotlistBuilder.setIntensityUnits(translateIntensityUnits(intensityUnits));
            spotlistBuilder.setFitMode(determineFitMode(table));
            Rectangle r = getBounds(table);
            spotlistBuilder.setNrPixelsX(r.width + r.x);
            spotlistBuilder.setNrPixelsY(r.height + r.y);
            //spotlistBuilder.setFitMode(determineFitMode(table));
            spotlistBuilder.build().writeDelimitedTo(outputStream);

            outputStream.flush();
            outputFile.seek(4);
            outputFile.writeLong(bytesWritten - 12); // bytes written minus one int and one long (magic and offset)
            outputStream.close();
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(outputFile);
        }
    }

    @Override
    public String getName() {
        return "Tagged spot file";
    }

    @Override
    public String getSuffix() {
        return "tsf";
    }

    private TSF.LocationUnits translateLocationUnits(Units locationUnits) {
        switch(locationUnits) {
            case PIXEL:
                return TSF.LocationUnits.PIXELS;
            case NANOMETER:
                return TSF.LocationUnits.NM;
            case MICROMETER:
                return TSF.LocationUnits.UM;
            default:
                throw new RuntimeException("TSF format does not allow " + locationUnits.toString() + " as location units.");
        }
    }

    private Units translateLocationUnits(TSF.LocationUnits locationUnits) {
        switch(locationUnits) {
            case PIXELS:
                return Units.PIXEL;
            case NM:
                return Units.NANOMETER;
            case UM:
                return Units.MICROMETER;
            default:
                throw new RuntimeException("Unknown units:" + locationUnits.toString());
        }
    }

    private TSF.IntensityUnits translateIntensityUnits(Units units) {
        switch(units) {
            case DIGITAL:
                return TSF.IntensityUnits.COUNTS;
            case PHOTON:
                return TSF.IntensityUnits.PHOTONS;
            default:
                throw new RuntimeException("TSF format does not allow " + units.toString() + " as intensity units.");
        }
    }

    private Units translateIntensityUnits(TSF.IntensityUnits units) {
        switch(units) {
            case COUNTS:
                return DIGITAL;
            case PHOTONS:
                return PHOTON;
            default:
                throw new RuntimeException("Unknown units: " + units.toString());
        }
    }

    private TSF.FitMode determineFitMode(GenericTable table) {
        if(table.columnExists(PSFModel.Params.LABEL_SIGMA2)) {
            return TSF.FitMode.TWOAXIS;
        }
        return TSF.FitMode.ONEAXIS;
    }

    private Rectangle getBounds(GenericTable table) {
        int rowCount = table.getRowCount();
        double minX = 0, maxX = 0, minY = 0, maxY = 0;
        Molecule firstMol = table.getRow(0);
        if(rowCount > 0) {
            minX = firstMol.getX(PIXEL);
            maxX = firstMol.getX(PIXEL);
            minY = firstMol.getY(PIXEL);
            maxY = firstMol.getY(PIXEL);
        }

        for(int i = 1; i < rowCount; i++) {
            Molecule mol = table.getRow(i);
            double x = mol.getX(Units.PIXEL);
            if(x > maxX) {
                maxX = x;
            }
            if(x < minX) {
                minX = x;
            }

            double y = mol.getY(Units.PIXEL);
            if(y > maxY) {
                maxY = y;
            }
            if(y < minY) {
                minY = y;
            }
        }
        double minXInt = Math.floor(minX);
        double minYInt = Math.floor(minY);
        return new Rectangle((int) minXInt, (int) minYInt, (int) (Math.ceil(maxX) - minXInt), (int) (Math.ceil(maxY) - minYInt));
    }
}
