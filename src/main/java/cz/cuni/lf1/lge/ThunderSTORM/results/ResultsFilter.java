package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParser;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.Node;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.RetVal;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.EmptyRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.WorkerThread;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.ShapeRoi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_X;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Y;

public class ResultsFilter extends PostProcessingModule {

    private JTextField filterTextField;
    private JButton applyButton;
    private JButton restrictToROIButton;

    private ParameterKey.String formulaParameter;

    @Override
    public String getMacroName() {
        return "filter";
    }

    @Override
    public String getTabName() {
        return "Filter";
    }

    public ResultsFilter() {
        formulaParameter = params.createStringField("formula", null, "");
    }

    public void setFilterFormula(String formula) {
        filterTextField.setText(formula);
    }

    public String getFilterFormula() {
        return filterTextField.getText();
    }

    @Override
    protected JPanel createUIPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterTextField = new JTextField();
        InputListener listener = new InputListener();
        filterTextField.addKeyListener(listener);
        formulaParameter.registerComponent(filterTextField);
        applyButton = new JButton("Apply");
        applyButton.addActionListener(listener);
        restrictToROIButton = new JButton("Restrict to ROI");
        restrictToROIButton.addActionListener(listener);
        filterPanel.add(new JLabel("Filter: "), new GridBagHelper.Builder().gridxy(0, 0).anchor(GridBagConstraints.WEST).build());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        filterPanel.add(filterTextField, new GridBagHelper.Builder().gridxy(0, 1).fill(GridBagConstraints.HORIZONTAL).weightx(1).build());
        filterPanel.add(Help.createHelpButton(getClass()), new GridBagHelper.Builder().gridxy(2, 0).anchor(GridBagConstraints.EAST).build());
        filterPanel.add(applyButton, new GridBagHelper.Builder().gridxy(1, 1).build());
        filterPanel.add(restrictToROIButton, new GridBagHelper.Builder().gridxy(2, 1).build());
        return filterPanel;
    }

    @Override
    protected void runImpl() {
        final String filterText = formulaParameter.getValue();
        if((!applyButton.isEnabled()) || (filterText == null) || ("".equals(filterText))) {
            return;
        }
        filterTextField.setBackground(Color.WHITE);
        try {
            applyButton.setEnabled(false);
            saveStateForUndo();
            final int all = model.getRowCount();
            new WorkerThread<Void>() {
                @Override
                public Void doJob() {
                    applyToModel(model, filterText);
                    return null;
                }

                @Override
                public void finishJob(Void nothing) {
                    int filtered = all - model.getRowCount();
                    addOperationToHistory(new DefaultOperation());
                    String be = ((filtered > 1) ? "were" : "was");
                    String item = ((all > 1) ? "items" : "item");
                    table.setStatus(filtered + " out of " + all + " " + item + " " + be + " filtered out");
                    table.showPreview();
                }

                @Override
                public void exCatch(Throwable ex) {
                    filterTextField.setBackground(new Color(255, 200, 200));
                    GUI.showBalloonTip(filterTextField, ex.getCause().getMessage());
                }

                @Override
                public void exFinally() {
                    applyButton.setEnabled(true);
                }
            }.execute();
        } catch(Exception ex) {
            IJ.handleException(ex);
            filterTextField.setBackground(new Color(255, 200, 200));
            GUI.showBalloonTip(filterTextField, ex.toString());
        }
    }

    public void restrictToROIFilter() {
        double mag = new EmptyRendererUI().magnification.getValue();
        IJResultsTable rt = IJResultsTable.getResultsTable();
        ImagePlus preview = rt.getPreviewImage();
        Roi roi2 = null;
        if(preview != null) {
            roi2 = preview.getRoi();
        }
        if(roi2 == null) {
            GUI.showBalloonTip(restrictToROIButton, "There is no ROI in the preview image!");
            return;
        }
        Rectangle2D[] rectangleList;
        if(roi2 instanceof ShapeRoi) {
            ShapeRoi shapeRoi = (ShapeRoi) roi2;
            Roi[] roiList = shapeRoi.getRois();
            rectangleList = new Rectangle2D[roiList.length];
            for(int i = 0; i < roiList.length; i++) {
                rectangleList[i] = roiList[i].getBounds();
            }
        } else {
            rectangleList = new Rectangle2D[]{roi2.getBounds()};
        }
        Units ux = rt.getColumnUnits(LABEL_X);
        Units uy = rt.getColumnUnits(LABEL_Y);
        //
        for(int i = 0; i < rectangleList.length; i++) {
            rectangleList[i] = convertRectangleUnits(rectangleList[i], ux, uy, mag);
        }
        addNewRectanglesFilter(rectangleList);
    }

    private Rectangle2D.Double convertRectangleUnits(Rectangle2D rect, Units ux, Units uy, double magnification) {
        Rectangle2D.Double ret = new Rectangle2D.Double(
                Units.PIXEL.convertTo(ux, rect.getMinX() / magnification),
                Units.PIXEL.convertTo(uy, rect.getMinY() / magnification),
                0, 0);

        ret.add(Units.PIXEL.convertTo(ux, rect.getMaxX() / magnification),
                Units.PIXEL.convertTo(uy, rect.getMaxY() / magnification));
        return ret;
    }
    
    void addNewFilter(String paramName, double greaterThan, double lessThan) {
        String formula = getFilterFormula().trim();
        StringBuilder sb = new StringBuilder(formula);
        if(!formula.isEmpty()) {
            sb.append(" & ");
        }
        sb.append("(");
        sb.append(paramName).append(" > ").append(BigDecimal.valueOf(greaterThan).round(new MathContext(6)).toString());
        sb.append(" & ");
        sb.append(paramName).append(" < ").append(BigDecimal.valueOf(lessThan).round(new MathContext(6)).toString());
        sb.append(")");
        setFilterFormula(sb.toString());
    }

    public void addNewRectanglesFilter(Rectangle2D[] rectangles) {
        StringBuilder sb = new StringBuilder();
        String currentFilterFormula = getFilterFormula();
        if(currentFilterFormula != null && !currentFilterFormula.isEmpty()) {
            sb.append(currentFilterFormula);
            sb.append(" & ");
        }
        sb.append(createRectanglesFilter(rectangles));
        setFilterFormula(sb.toString());
    }

    public static String createRectanglesFilter(Rectangle2D[] rectangles) {
        StringBuilder sb = new StringBuilder();
        if(rectangles.length > 1) {
            sb.append("(");
        }
        for(int i = 0; i < rectangles.length; i++) {
            sb.append("(");
            sb.append("x > ").append(BigDecimal.valueOf(rectangles[i].getMinX()).round(new MathContext(6)).toString());
            sb.append(" & x < ").append(BigDecimal.valueOf(rectangles[i].getMaxX()).round(new MathContext(6)).toString());
            sb.append(" & y > ").append(BigDecimal.valueOf(rectangles[i].getMinY()).round(new MathContext(6)).toString());
            sb.append(" & y < ").append(BigDecimal.valueOf(rectangles[i].getMaxY()).round(new MathContext(6)).toString());
            sb.append(")");
            if(i != rectangles.length - 1) {
                sb.append(" | ");
            }
        }
        if(rectangles.length > 1) {
            sb.append(")");
        }
        return sb.toString();
    }

    private void applyToModel(GenericTableModel model, String text) {
        boolean[] results = new boolean[model.getRowCount()];
        if(text.isEmpty()) {
            Arrays.fill(results, true);
        } else {
            Node tree = new FormulaParser(text, FormulaParser.FORMULA_RESULTS_FILTER).parse();
            tree.semanticScan();
            RetVal retval = tree.eval(null);
            if(!retval.isVector()) {
                throw new FormulaParserException("Semantic error: result of filtering formula must be a vector of boolean values!");
            }
            Double[] res = (Double[]) retval.get();
            for(int i = 0; i < res.length; i++) {
                results[i] = (res[i].doubleValue() != 0.0);
            }
            model.filterRows(results);
        }
    }

    private class InputListener extends KeyAdapter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == applyButton) {
                run();
            } else if(e.getSource() == restrictToROIButton) {
                restrictToROIFilter();
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

    @Override
    public void resetParamsToDefaults() {
        filterTextField.setBackground(Color.white);
        super.resetParamsToDefaults();
    }
}
