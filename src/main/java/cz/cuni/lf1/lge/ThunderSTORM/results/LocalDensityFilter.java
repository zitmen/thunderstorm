package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.WorkerThread;
import cz.cuni.lf1.lge.ThunderSTORM.util.javaml.kdtree.KDTree;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.StringValidatorFactory;
import ij.IJ;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LocalDensityFilter extends PostProcessingModule {

    final String[] dim = {"2D", "3D"};

    private JTextField distanceTextField;
    private JTextField neighborsTextField;
    private JButton applyButton;

    private ParameterKey.Double distanceRadius;
    private ParameterKey.String dimensions;
    private ParameterKey.Integer minNeighbors;

    @Override
    public String getMacroName() {
        return "density";
    }

    @Override
    public String getTabName() {
        return "Density filter";
    }

    public LocalDensityFilter() {
        distanceRadius = params.createDoubleField("radius", DoubleValidatorFactory.positiveNonZero(), 50.0);
        dimensions = params.createStringField("dimensions", StringValidatorFactory.isMember(dim), dim[0]);
        minNeighbors = params.createIntField("neighbors", IntegerValidatorFactory.positiveNonZero(), 5);
    }

    @Override
    protected JPanel createUIPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        InputListener listener = new InputListener();

        JLabel distanceLabel = new JLabel("Distance radius [nm]: ");
        distanceTextField = new JTextField(20);
        distanceTextField.addKeyListener(listener);
        distanceRadius.registerComponent(distanceTextField);

        JLabel neighborsLabel = new JLabel("Minimum number of neighbors in the radius: ");
        neighborsTextField = new JTextField(20);
        neighborsTextField.addKeyListener(listener);
        minNeighbors.registerComponent(neighborsTextField);

        ButtonGroup btnGroup = new ButtonGroup();
        JRadioButton twoRadioButton = new JRadioButton(dim[0]);
        JRadioButton threeRadioButton = new JRadioButton(dim[1]);
        btnGroup.add(twoRadioButton);
        btnGroup.add(threeRadioButton);
        dimensions.registerComponent(btnGroup);

        applyButton = new JButton("Apply");
        applyButton.addActionListener(listener);

        filterPanel.add(distanceLabel, new GridBagHelper.Builder().gridxy(0, 0).weightx(0.3).anchor(GridBagConstraints.EAST).build());
        filterPanel.add(distanceTextField, new GridBagHelper.Builder().gridxy(1, 0).weightx(0.3).anchor(GridBagConstraints.WEST).build());
        filterPanel.add(twoRadioButton, new GridBagHelper.Builder().gridxy(2, 0).weightx(0.2).anchor(GridBagConstraints.WEST).build());
        filterPanel.add(threeRadioButton, new GridBagHelper.Builder().gridxy(3, 0).weightx(0.2).anchor(GridBagConstraints.WEST).build());
        filterPanel.add(Help.createHelpButton(getClass()), new GridBagHelper.Builder().gridxy(4, 0).anchor(GridBagConstraints.EAST).build());
        filterPanel.add(neighborsLabel, new GridBagHelper.Builder().gridxy(0, 1).weightx(0.5).anchor(GridBagConstraints.EAST).build());
        filterPanel.add(neighborsTextField, new GridBagHelper.Builder().gridxy(1, 1).weightx(0.5).anchor(GridBagConstraints.WEST).build());
        filterPanel.add(applyButton, new GridBagHelper.Builder().gridxy(4, 1).build());
        params.updateComponents();
        return filterPanel;
    }

    protected boolean is3D() {
        return dimensions.getValue().equals(dim[1]);
    }

    @Override
    protected void runImpl() {
        if(!applyButton.isEnabled()) {
            return;
        }
        try {
            final IJResultsTable table = IJResultsTable.getResultsTable();
            if (!table.columnExists(PSFModel.Params.LABEL_X) || !table.columnExists(PSFModel.Params.LABEL_Y)) {
                throw new Exception("Columns `x` and `y` must be present in the table!");
            }
            if (is3D() && !table.columnExists(PSFModel.Params.LABEL_Z)) {
                throw new Exception("Column `z` must be present in the table! Use 2D filter instead.");
            }
            //
            applyButton.setEnabled(false);
            saveStateForUndo();
            final int nRows = model.getRowCount();
            new WorkerThread<Void>() {
                @Override
                public Void doJob() {
                    double[][] coords = new double[table.getRowCount()][];
                    if (is3D()) {
                        for(int i = 0; i < coords.length; i++) {
                            Molecule m = table.getRow(i);
                            coords[i] = new double[] { m.getX(MoleculeDescriptor.Units.NANOMETER), m.getY(MoleculeDescriptor.Units.NANOMETER), m.getZ(MoleculeDescriptor.Units.NANOMETER) };
                        }
                    } else {
                        for(int i = 0; i < coords.length; i++) {
                            Molecule m = table.getRow(i);
                            coords[i] = new double[] { m.getX(MoleculeDescriptor.Units.NANOMETER), m.getY(MoleculeDescriptor.Units.NANOMETER) };
                        }
                    }
                    IJ.showStatus("Local density filtering...");
                    boolean filter[] = new boolean[coords.length];
                    KDTree<double[]> tree = new KDTree<double[]>(coords[0].length);
                    for(double[] dataPoint : coords) {
                        tree.insert(dataPoint, dataPoint);
                    }
                    int countThr = minNeighbors.getValue();
                    double radius = distanceRadius.getValue();
                    for (int i = 0; i < coords.length; i++) {
                        filter[i] = ((tree.ballQuery(coords[i], radius).size() - 1) >= countThr);  // -1 --> do not count the query point itself!;
                    }
                    model.filterRows(filter);
                    IJ.showStatus("Done.");
                    return null;
                }

                @Override
                public void finishJob(Void nothing) {
                    int filtered = nRows - model.getRowCount();
                    addOperationToHistory(new DefaultOperation());
                    String be = ((filtered > 1) ? "were" : "was");
                    String item = ((nRows > 1) ? "items" : "item");
                    table.setStatus(filtered + " out of " + nRows + " " + item + " " + be + " filtered out");
                    table.showPreview();
                }

                @Override
                public void exCatch(Throwable ex) {
                    IJ.error(ex.getMessage());
                }

                @Override
                public void exFinally() {
                    applyButton.setEnabled(true);
                }
            }.execute();
        } catch (Exception ex) {
            IJ.error(ex.getMessage());
        } finally {
            applyButton.setEnabled(true);
        }
    }

    private class InputListener extends KeyAdapter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == applyButton) {
                run();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                run();
            }
        }
    }
}
