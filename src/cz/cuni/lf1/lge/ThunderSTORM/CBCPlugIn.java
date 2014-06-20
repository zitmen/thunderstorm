package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.colocalization.CBC;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ASHRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTableModel;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.IValue;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterKey;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterTracker;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.plugin.PlugIn;

public class CBCPlugIn implements PlugIn {

    ParameterTracker params = new ParameterTracker();
    //ParameterKey.Double maxDist = params.createDoubleField("maxDist", DoubleValidatorFactory.positiveNonZero(), 10);

    public CBCPlugIn() {
        params.setNoGuiParametersAllowed(true);
    }

    @Override
    public void run(String arg) {
        try {
            if(Macro.getOptions() != null) {
                params.readMacroOptions();
            } else {
                params.recordMacroOptions();
            }
            
            IJResultsTable resultsTable = IJResultsTable.getResultsTable();
            IJGroundTruthTable groundTruthTable = IJGroundTruthTable.getGroundTruthTable();

            if(resultsTable == null || groundTruthTable == null) {
                IJ.error("Requires results table and ground-truth table open.");
                return;
            }
            if(resultsTable.getRowCount() <= 0) {
                IJ.error("Results table empty.");
                return;
            }
            if(groundTruthTable.getRowCount() <= 0) {
                IJ.error("Ground-truth table empty.");
                return;
            }
            
            GenericDialogPlus gd = new GenericDialogPlus("ThunderSTORM: Performance evaluation");
            gd.addNumericField("Radius step [nm]: ", 100, 0);
            gd.addNumericField("Step count: ", 5, 0);
            gd.addCheckbox("Add CBC into the results table: ", true);
            gd.addCheckbox("Add distance to the nearest neighbor into the results table: ", true);
            gd.addCheckbox("Add count of neighbors in within a radius into the results table: ", true);
            gd.showDialog();
            if(!gd.wasCanceled()) {
                double radiusStep = gd.getNextNumber();
                int radiusCount = (int)gd.getNextNumber();
                boolean addCBCToTable = gd.getNextBoolean();
                boolean addNNDistToTable = gd.getNextBoolean();
                boolean addNeighborsInDistToTable = gd.getNextBoolean();
                runCBC(radiusStep, radiusCount, addCBCToTable, addNNDistToTable, addNeighborsInDistToTable);
            }
        } catch(Exception e) {
            IJ.handleException(e);
        }
    }
    
    public void runCBC(double radiusStep, int radiusCount, boolean addCBCToTable, boolean addNNDistToTable, boolean addNeighborsInDistToTable) {
        IJResultsTable resultsTable = IJResultsTable.getResultsTable();
        IJGroundTruthTable groundTruthTable = IJGroundTruthTable.getGroundTruthTable();

        double[][] firstXY = new double[resultsTable.getRowCount()][];
        for(int i = 0; i < firstXY.length; i++) {
            Molecule m = resultsTable.getRow(i);
            firstXY[i] = new double[]{m.getX(MoleculeDescriptor.Units.PIXEL), m.getY(MoleculeDescriptor.Units.PIXEL)};
        }
        double[][] secondXY = new double[groundTruthTable.getRowCount()][];
        for(int i = 0; i < secondXY.length; i++) {
            Molecule m = groundTruthTable.getRow(i);
            secondXY[i] = new double[]{m.getX(MoleculeDescriptor.Units.PIXEL), m.getY(MoleculeDescriptor.Units.PIXEL)};
        }

        CBC cbc = new CBC(firstXY, secondXY, radiusStep, radiusCount);

        IJ.showStatus("Calculating first channel CBC.");
        double[] firstChannelCBC = cbc.calculateFirstChannelCBC();
        if(addCBCToTable) addColumnToModel(resultsTable.getModel(), firstChannelCBC, "cbc", Units.UNITLESS);
        if(addNNDistToTable) addColumnToModel(resultsTable.getModel(), cbc.firstChannelNearestNeighborDistances, "nn_dist", Units.PIXEL);
        if(addNeighborsInDistToTable) {
            for(int i = 0; i < cbc.squaredRadiusDomainNm.length; i++) {
                addColumnToModel(resultsTable.getModel(), cbc.firstChannelNeighborsInDistance[i], "neighbors_in_dist_"+((i+1)*radiusStep), Units.PIXEL);
            }
        }

        double[] x1 = resultsTable.getColumnAsDoubles(PSFModel.Params.LABEL_X, MoleculeDescriptor.Units.PIXEL);
        double[] y1 = resultsTable.getColumnAsDoubles(PSFModel.Params.LABEL_Y, MoleculeDescriptor.Units.PIXEL);
        double[] x2 = groundTruthTable.getColumnAsDoubles(PSFModel.Params.LABEL_X, MoleculeDescriptor.Units.PIXEL);
        double[] y2 = groundTruthTable.getColumnAsDoubles(PSFModel.Params.LABEL_Y, MoleculeDescriptor.Units.PIXEL);
        double maxRoiX = MathProxy.max(VectorMath.max(x1), VectorMath.max(x2));
        double maxRoiY = MathProxy.max(VectorMath.max(y1), VectorMath.max(y2));

        RenderingMethod renderer = new ASHRendering.Builder()
                .shifts(2)
                .zShifts(1).colorizeZ(true).zRange(-1, 1, 0.1)
                .roi(0, maxRoiX, 0, maxRoiY)
                .resolution(0.2)
                .build();
        ImagePlus imp = renderer.getRenderedImage(x1, y1, firstChannelCBC, null);
        imp.show();

        IJ.showStatus("Calculating second channel CBC.");
        double[] secondChannelCBC = cbc.calculateSecondChannelCBC();
        if(addCBCToTable) addColumnToModel(groundTruthTable.getModel(), secondChannelCBC, "cbc", Units.UNITLESS);
        if(addNNDistToTable) addColumnToModel(groundTruthTable.getModel(), cbc.secondChannelNearestNeighborDistances, "nn_dist", Units.PIXEL);
        if(addNeighborsInDistToTable) {
            for(int i = 0; i < cbc.squaredRadiusDomainNm.length; i++) {
                addColumnToModel(groundTruthTable.getModel(), cbc.secondChannelNeighborsInDistance[i], "neighbors_in_dist_"+((i+1)*radiusStep), Units.PIXEL);
            }
        }

        RenderingMethod renderer2 = new ASHRendering.Builder()
                .shifts(2)
                .zShifts(1).colorizeZ(true).zRange(-1, 1, 0.1)
                .roi(0, maxRoiX, 0, maxRoiY)
                .resolution(0.2)
                .build();

        ImagePlus imp2 = renderer2.getRenderedImage(x2, y2, secondChannelCBC, null);
        imp2.show();
        IJ.showStatus("Done.");
    }

    private void addColumnToModel(GenericTableModel model, final double[] data, String title, Units units) {
            model.addColumn(title, units, new IValue<Double>() {
                int i = 0;
                @Override
                public Double getValue() {
                    return data[i++];
                }
            });
        model.fireTableStructureChanged();
        model.fireTableDataChanged();
    }

}
