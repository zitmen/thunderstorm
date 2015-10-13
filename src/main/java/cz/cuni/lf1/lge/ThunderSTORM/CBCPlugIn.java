package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.colocalization.CBC;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ASHRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTableModel;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.*;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.DialogStub;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterTracker;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.StringValidatorFactory;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.plugin.PlugIn;
import javax.swing.*;
import java.awt.*;

public class CBCPlugIn implements PlugIn {

    ParameterTracker params = new ParameterTracker();

    public CBCPlugIn() {
        params.setNoGuiParametersAllowed(true);
    }

    @Override
    public void run(String arg) {
        GUI.setLookAndFeel();
        try {
            if(Macro.getOptions() != null) {
                params.readMacroOptions();
            } else {
                params.recordMacroOptions();
            }
            
            CBCDialog dialog = new CBCDialog(IJ.getInstance());
            if(MacroParser.isRanFromMacro()) {
                dialog.getParams().readMacroOptions();
            } else {
                if(dialog.showAndGetResult() != JOptionPane.OK_OPTION) {
                    return;
                }
            }
            runCBC(dialog.getChannel1Table(), dialog.getChannel2Table(), dialog.is3D(), dialog.radiusStep.getValue(),
                   dialog.stepCount.getValue(), dialog.addCBC.getValue(), dialog.addNNDist.getValue(), dialog.addNNCount.getValue());

        } catch(Exception ex) {
            IJ.error(ex.getMessage());
        }
    }
    
    public void runCBC(GenericTable channel1Table, GenericTable channel2Table, boolean is3D, double radiusStep, int stepCount, boolean addCBCToTable, boolean addNNDistToTable, boolean addNeighborsInDistToTable) {
        if (channel1Table == null || channel2Table == null) return;

        double[][] firstCoords = new double[channel1Table.getRowCount()][];
        if (is3D) {
            for (int i = 0; i < firstCoords.length; i++) {
                Molecule m = channel1Table.getRow(i);
                firstCoords[i] = new double[]{m.getX(Units.NANOMETER), m.getY(Units.NANOMETER), m.getZ(Units.NANOMETER)};
            }
        } else {
            for (int i = 0; i < firstCoords.length; i++) {
                Molecule m = channel1Table.getRow(i);
                firstCoords[i] = new double[]{m.getX(Units.NANOMETER), m.getY(Units.NANOMETER)};
            }
        }

        CBC cbc;
        if (channel1Table != channel2Table) {
            double[][] secondCoords = new double[channel2Table.getRowCount()][];
            if (is3D) {
                for (int i = 0; i < secondCoords.length; i++) {
                    Molecule m = channel2Table.getRow(i);
                    secondCoords[i] = new double[]{m.getX(Units.NANOMETER), m.getY(Units.NANOMETER), m.getZ(Units.NANOMETER)};
                }
            } else {
                for (int i = 0; i < secondCoords.length; i++) {
                    Molecule m = channel2Table.getRow(i);
                    secondCoords[i] = new double[]{m.getX(Units.NANOMETER), m.getY(Units.NANOMETER)};
                }
            }
            cbc = new CBC(firstCoords, secondCoords, radiusStep, stepCount);
        } else {
            cbc = new CBC(firstCoords, firstCoords, radiusStep, stepCount);
        }

        IJ.showStatus("Calculating first channel CBC.");
        double[] firstChannelCBC = cbc.calculateFirstChannelCBC();
        if(addCBCToTable) addColumnToModel(channel1Table.getModel(), firstChannelCBC, "cbc", Units.UNITLESS);
        if(addNNDistToTable) addColumnToModel(channel1Table.getModel(), cbc.firstChannelNearestNeighborDistances, "nn_dist", Units.NANOMETER);
        if(addNeighborsInDistToTable) {
            for(int i = 0; i < cbc.squaredRadiusDomain.length; i++) {
                addColumnToModel(channel1Table.getModel(), cbc.firstChannelNeighborsInDistance[i], "neighbors_in_dist_"+((int)((i+1)*radiusStep)), Units.UNITLESS);
            }
        }

        double[] x1 = channel1Table.getColumnAsDoubles(PSFModel.Params.LABEL_X, MoleculeDescriptor.Units.PIXEL);
        double[] y1 = channel1Table.getColumnAsDoubles(PSFModel.Params.LABEL_Y, MoleculeDescriptor.Units.PIXEL);
        double[] x2 = channel2Table.getColumnAsDoubles(PSFModel.Params.LABEL_X, MoleculeDescriptor.Units.PIXEL);
        double[] y2 = channel2Table.getColumnAsDoubles(PSFModel.Params.LABEL_Y, MoleculeDescriptor.Units.PIXEL);
        double maxRoiX = MathProxy.max(VectorMath.max(x1), VectorMath.max(x2));
        double maxRoiY = MathProxy.max(VectorMath.max(y1), VectorMath.max(y2));

        RenderingMethod renderer = new ASHRendering.Builder()
                .shifts(2)
                .zShifts(1).colorize(true).zRange(-1, 1, 0.1)
                .roi(0, maxRoiX, 0, maxRoiY)
                .resolution(0.2)
                .build();
        ImagePlus imp = renderer.getRenderedImage(x1, y1, firstChannelCBC, null, null);
        imp.show();

        if (channel1Table != channel2Table) {
            IJ.showStatus("Calculating second channel CBC.");
            double[] secondChannelCBC = cbc.calculateSecondChannelCBC();
            if (addCBCToTable) addColumnToModel(channel2Table.getModel(), secondChannelCBC, "cbc", Units.UNITLESS);
            if (addNNDistToTable)
                addColumnToModel(channel2Table.getModel(), cbc.secondChannelNearestNeighborDistances, "nn_dist", Units.NANOMETER);
            if (addNeighborsInDistToTable) {
                for (int i = 0; i < cbc.squaredRadiusDomain.length; i++) {
                    addColumnToModel(channel2Table.getModel(), cbc.secondChannelNeighborsInDistance[i], "neighbors_in_dist_" + ((int) ((i + 1) * radiusStep)), Units.UNITLESS);
                }
            }

            RenderingMethod renderer2 = new ASHRendering.Builder()
                    .shifts(2)
                    .zShifts(1).colorize(true).zRange(-1, 1, 0.1)
                    .roi(0, maxRoiX, 0, maxRoiY)
                    .resolution(0.2)
                    .build();

            ImagePlus imp2 = renderer2.getRenderedImage(x2, y2, secondChannelCBC, null, null);
            imp2.show();
        }
        IJ.showStatus("Done.");
    }

    private void addColumnToModel(GenericTableModel model, final double[] data, String title, Units units) {
        int col = model.findColumn(title);
        if (col == GenericTableModel.COLUMN_NOT_FOUND) {
            model.addColumn(title, units, new IValue<Double>() {
                int i = 0;
                @Override
                public Double getValue() {
                    return data[i++];
                }
            });
            model.fireTableStructureChanged();
        } else {
            model.insertColumn(col, title, units, new IValue<Double>() {
                int i = 0;
                @Override
                public Double getValue() {
                    return data[i++];
                }
            });
        }
        model.fireTableDataChanged();
    }

    class CBCDialog extends DialogStub {

        ParameterKey.Integer radiusStep;
        ParameterKey.String dimensions;
        ParameterKey.Integer stepCount;
        ParameterKey.String channel1;
        ParameterKey.String channel2;
        ParameterKey.Boolean addCBC;
        ParameterKey.Boolean addNNDist;
        ParameterKey.Boolean addNNCount;

        final String[] dim = {"2D", "3D"};
        final String[] tables = new String[] {"Results table", "Ground-truth table"};

        public CBCDialog(Window owner) {
            super(new ParameterTracker("thunderstorm.cbc"), owner, "Coordinate-Based Colocalization");
            radiusStep = params.createIntField("radiusStep", IntegerValidatorFactory.positiveNonZero(), 50);
            dimensions = params.createStringField("dimensions", StringValidatorFactory.isMember(dim), dim[0]);
            stepCount = params.createIntField("stepCount", IntegerValidatorFactory.rangeInclusive(2, 10000), 10);
            channel1 = params.createStringField("channel1", StringValidatorFactory.isMember(tables), tables[0]);
            channel2 = params.createStringField("channel2", StringValidatorFactory.isMember(tables), tables[1]);
            addCBC = params.createBooleanField("addCBC", null, true);
            addNNDist = params.createBooleanField("addNNDist", null, false);
            addNNCount = params.createBooleanField("addNNCount", null, false);
        }

        ParameterTracker getParams() {
            return params;
        }

        @Override
        protected void layoutComponents() {
            JTextField radiusStepTextField = new JTextField(20);
            JTextField stepCountTextField = new JTextField(20);
            ButtonGroup dimBtnGroup = new ButtonGroup();
            JRadioButton twoDimRadioButton = new JRadioButton(dim[0]);
            JRadioButton threeDimRadioButton = new JRadioButton(dim[1]);
            dimBtnGroup.add(twoDimRadioButton);
            dimBtnGroup.add(threeDimRadioButton);
            JComboBox<String> channel1ComboBox = new JComboBox<String>(tables);
            JComboBox<String> channel2ComboBox = new JComboBox<String>(tables);
            JCheckBox addCBCCheckBox = new JCheckBox("Add CBC into the results table");
            JCheckBox addNNDistCheckBox = new JCheckBox("Add distance to the nearest neighbor into the results table");
            JCheckBox addNNCountCheckBox = new JCheckBox("Add count of neighbors within the radius into the results table");

            radiusStep.registerComponent(radiusStepTextField);
            dimensions.registerComponent(dimBtnGroup);
            stepCount.registerComponent(stepCountTextField);
            channel1.registerComponent(channel1ComboBox);
            channel2.registerComponent(channel2ComboBox);
            addCBC.registerComponent(addCBCCheckBox);
            addNNDist.registerComponent(addNNDistCheckBox);
            addNNCount.registerComponent(addNNCountCheckBox);

            JPanel dimPanel = new JPanel(new GridBagLayout());
            dimPanel.add(twoDimRadioButton, new GridBagHelper.Builder().gridxy(0, 0).weightx(0.2).anchor(GridBagConstraints.WEST).build());
            dimPanel.add(threeDimRadioButton, new GridBagHelper.Builder().gridxy(1, 0).weightx(0.2).anchor(GridBagConstraints.WEST).build());
            dimPanel.add(Box.createHorizontalGlue(), new GridBagHelper.Builder().gridxy(2, 0).weightx(0.6).anchor(GridBagConstraints.WEST).build());

            add(new JLabel("Radius step [nm]:"), GridBagHelper.leftCol());
            add(radiusStepTextField, GridBagHelper.rightCol());
            add(new JLabel("Dimensions:"), GridBagHelper.leftCol());
            add(dimPanel, GridBagHelper.rightCol());
            add(new JLabel("Step count:"), GridBagHelper.leftCol());
            add(stepCountTextField, GridBagHelper.rightCol());
            add(new JLabel("Channel 1:"), GridBagHelper.leftCol());
            add(channel1ComboBox, GridBagHelper.rightCol());
            add(new JLabel("Channel 2:"), GridBagHelper.leftCol());
            add(channel2ComboBox, GridBagHelper.rightCol());
            add(Box.createVerticalStrut(10), GridBagHelper.twoCols());
            add(addCBCCheckBox, GridBagHelper.twoCols());
            add(addNNDistCheckBox, GridBagHelper.twoCols());
            add(addNNCountCheckBox, GridBagHelper.twoCols());

            add(Box.createVerticalStrut(10), GridBagHelper.twoCols());

            //buttons
            JPanel buttons = new JPanel(new GridBagLayout());
            buttons.add(createDefaultsButton());
            buttons.add(Box.createHorizontalGlue(), new GridBagHelper.Builder()
                    .fill(GridBagConstraints.HORIZONTAL).weightx(1).build());
            buttons.add(Help.createHelpButton(CBCPlugIn.class));
            buttons.add(createOKButton());
            buttons.add(createCancelButton());
            add(buttons, GridBagHelper.twoCols());

            params.loadPrefs();

            getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            pack();
            setLocationRelativeTo(null);
            setModal(true);
        }

        boolean is3D() {
            return dimensions.getValue().equals(dim[1]);
        }

        public GenericTable getChannel1Table() throws Exception {
            return getChannelTable(channel1);
        }

        public GenericTable getChannel2Table() throws Exception {
            return getChannelTable(channel2);
        }

        protected GenericTable getChannelTable(ParameterKey.String channel) throws Exception {
            GenericTable table;
            if (channel.getValue().equals(tables[0])) { // results table
                table = IJResultsTable.getResultsTable();
                if (table == null) {
                    throw new Exception("Requires results table to be opened!");
                }
                if (table.getRowCount() <= 0) {
                    throw new Exception("Results table cannot be empty!");
                }
            } else if (channel.getValue().equals(tables[1])) { // ground-truth table
                table = IJGroundTruthTable.getGroundTruthTable();
                if (table == null) {
                    throw new Exception("Requires ground-truth table to be opened!");
                }
                if (table.getRowCount() <= 0) {
                    throw new Exception("Ground-truth table cannot be empty!");
                }
            } else {
                throw new Exception("Unknown channel!");
            }
            if (!table.columnExists(PSFModel.Params.LABEL_X) || !table.columnExists(PSFModel.Params.LABEL_Y)) {
                throw new Exception("Columns `x` and `y` must be present in the table!");
            }
            if (is3D() && !table.columnExists(PSFModel.Params.LABEL_Z)) {
                throw new Exception("Column `z` must be present in the table! Use 2D filter instead.");
            }
            return table;
        }
    }

}
