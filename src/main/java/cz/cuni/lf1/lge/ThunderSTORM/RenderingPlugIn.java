package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.AnalysisOptionsDialog;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_X;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Y;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Z;
import cz.cuni.lf1.lge.ThunderSTORM.UI.CardsPanel;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units.PIXEL;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units.NANOMETER;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.AbstractRenderingUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.EmptyRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.IRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.Prefs;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class RenderingPlugIn implements PlugIn {

    private enum AutoSize {
        MANUAL("manual"),
        RESULTS("results"),
        IMAGE("image");

        private String value;
        private AutoSize(String val) {
            this.value = val;
        }

        public String getValue() {
            return this.value;
        }
    };

    public static Rectangle resizeByResults(double [] xpos, double [] ypos, double magnification) {
        int left  = ((int)Math.max(VectorMath.min(xpos) * magnification, 0));
        int top   = ((int)Math.max(VectorMath.min(ypos) * magnification, 0));
        int sizeX = (int)(VectorMath.max(xpos) * magnification) - left + 1;
        int sizeY = (int)(VectorMath.max(ypos) * magnification) - top  + 1;
        int mag   = (int)magnification;
        return new Rectangle(left/mag, top/mag, sizeX/mag, sizeY/mag);
    }

    @Override
    public void run(String string) {
        GUI.setLookAndFeel();
        //
        boolean preview = false;
        GenericTable table;
        if(IJGroundTruthTable.IDENTIFIER.equals(string)) {
            if(!IJGroundTruthTable.isGroundTruthWindow()) {
                IJ.error("Requires `" + IJGroundTruthTable.IDENTIFIER + "` window open!");
                return;
            }
            table = IJGroundTruthTable.getGroundTruthTable();
        } else {
            if(!IJResultsTable.isResultsWindow()) {
                IJ.error("Requires `" + IJResultsTable.IDENTIFIER + "` window open!");
                return;
            }
            table = IJResultsTable.getResultsTable();
            preview = true;
        }
        //
        if(!table.columnExists(LABEL_X) || !table.columnExists(LABEL_Y)) {
            IJ.error(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", LABEL_X, LABEL_Y, table.getColumnNames()));
            return;
        }

        double[] xpos = table.getColumnAsDoubles(LABEL_X, MoleculeDescriptor.Units.PIXEL);
        double[] ypos = table.getColumnAsDoubles(LABEL_Y, MoleculeDescriptor.Units.PIXEL);
        if(xpos == null || ypos == null) {
            IJ.error("results were null");
            return;
        }
        double[] z = table.columnExists(LABEL_Z) ? table.getColumnAsDoubles(LABEL_Z) : null;
        double[] dx = null;
        if(table.columnExists(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY)) {
            dx = table.getColumnAsDoubles(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY, MoleculeDescriptor.Units.PIXEL);
        }
        double[] dz = null;
        if(table.columnExists(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_Z)) {
            dz = table.getColumnAsDoubles(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_Z, MoleculeDescriptor.Units.NANOMETER);
        }

        List<IRendererUI> knownRenderers = ModuleLoader.getUIModules(IRendererUI.class);
        //do not show EmptyRenderer
        for(Iterator<IRendererUI> it = knownRenderers.iterator(); it.hasNext();) {
            IRendererUI rendererUI = it.next();
            if(rendererUI instanceof EmptyRendererUI) {
                it.remove();
            } else if(rendererUI instanceof AbstractRenderingUI) {
                ((AbstractRenderingUI) rendererUI).setShowRepaintFrequency(false);
            }
        }
        IRendererUI selectedRendererUI;
        double sizeX, sizeY, left, top;
        boolean setAsPreview = false;
        ImagePlus im;

        if(MacroParser.isRanFromMacro()) {
            MacroParser parser = new MacroParser(false, null, null, null, knownRenderers);
            selectedRendererUI = parser.getRendererUI();

            String autosize = Macro.getValue(Macro.getOptions(), "autosize", AutoSize.MANUAL.getValue());
            if (autosize.equals(AutoSize.IMAGE.getValue()) && (IJResultsTable.IDENTIFIER.equals(table.getTableIdentifier()) && (im = ((IJResultsTable) table).getAnalyzedImage()) != null)) {
                left = 0;
                top = 0;
                sizeX = im.getWidth();
                sizeY = im.getHeight();
            } else if (autosize.equals(AutoSize.RESULTS.getValue())) {
                Rectangle rect = resizeByResults(xpos, ypos, new EmptyRendererUI().magnification.getValue());
                left   = rect.getX();
                top    = rect.getY();
                sizeX  = rect.getWidth();
                sizeY  = rect.getHeight();
            } else {    // MANUAL
                left = Double.parseDouble(Macro.getValue(Macro.getOptions(), "imleft", "0"));
                top = Double.parseDouble(Macro.getValue(Macro.getOptions(), "imtop", "0"));
                sizeX = Double.parseDouble(Macro.getValue(Macro.getOptions(), "imwidth", "0"));
                sizeY = Double.parseDouble(Macro.getValue(Macro.getOptions(), "imheight", "0"));
            }
        } else {
            double guessedLeft, guessedTop, guessedWidth, guessedHeight;
            if(IJResultsTable.IDENTIFIER.equals(table.getTableIdentifier()) && (im = ((IJResultsTable) table).getAnalyzedImage()) != null) {
                guessedLeft = 0;
                guessedTop = 0;
                guessedWidth = im.getWidth();
                guessedHeight = im.getHeight();
            } else {
                Rectangle rect = resizeByResults(xpos, ypos, new EmptyRendererUI().magnification.getValue());
                guessedLeft   = rect.getX();
                guessedTop    = rect.getY();
                guessedWidth  = rect.getWidth();
                guessedHeight = rect.getHeight();
            }
            RenderingDialog dialog = new RenderingDialog(preview, knownRenderers, guessedLeft, guessedTop, guessedWidth, guessedHeight);
            dialog.setVisible(true);
            if(dialog.result == RenderingDialog.DialogResult.CANCELLED) {
                return;
            }
            if(dialog.result == RenderingDialog.DialogResult.PREVIEW) {
                setAsPreview = true;
            }
            selectedRendererUI = dialog.getSelectedRendererUI();
            left = dialog.left;
            top = dialog.top;
            sizeX = dialog.sizeX;
            sizeY = dialog.sizeY;
        }

        selectedRendererUI.setSize(left, top, sizeX, sizeY);
        IncrementalRenderingMethod method = selectedRendererUI.getImplementation();

        if(setAsPreview) {
            RenderingQueue queue = new RenderingQueue(method, new RenderingQueue.DefaultRepaintTask(method.getRenderedImage()), selectedRendererUI.getRepaintFrequency());
            ((IJResultsTable)table).setPreviewRenderer(queue);
            ((IJResultsTable)table).showPreview();
        } else {
            if(Recorder.record) {
                Recorder.recordOption("imleft", Double.toString(left));
                Recorder.recordOption("imtop", Double.toString(top));
                Recorder.recordOption("imwidth", Double.toString(sizeX));
                Recorder.recordOption("imheight", Double.toString(sizeY));
                MacroParser.recordRendererUI(selectedRendererUI);
            }

            method.reset();
            method.addToImage(xpos, ypos, z, dx, dz);
            new RenderingQueue.DefaultRepaintTask(method.getRenderedImage()).run();
        }
    }
}

class RenderingDialog extends JDialog {

    CardsPanel<IRendererUI> renderingMethods;
    JButton sizeResultsButton;
    JButton sizeAnalyzedImageButton;
    JButton previewButton;
    JButton okButton;
    JButton cancelButton;
    JButton defaultsButton;
    DialogResult result = DialogResult.CANCELLED;
    JTextField topTextField;
    JTextField leftTextField;
    JTextField sizeXTextField;
    JTextField sizeYTextField;
    double left, top, sizeX, sizeY;
    boolean enablePreview;
    int activeRendererIndex;

    enum DialogResult {
        CANCELLED, OK, PREVIEW;
    }

    public RenderingDialog(boolean preview, List<IRendererUI> knownRenderers, double left, double top, double sizeX, double sizeY) {
        super(IJ.getInstance(), "Visualization", true);
        this.activeRendererIndex = Integer.parseInt(Prefs.get("thunderstorm.rendering.index", "0"));
        this.renderingMethods = new CardsPanel<IRendererUI>(knownRenderers, activeRendererIndex);
        this.enablePreview = preview;
        this.sizeX = Prefs.get("thunderstorm.rendering.imwidth", sizeX);
        this.sizeY = Prefs.get("thunderstorm.rendering.imheight", sizeY);
        this.left = Prefs.get("thunderstorm.rendering.imleft", left);
        this.top = Prefs.get("thunderstorm.rendering.imtop", top);
        layoutComponents();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    public IRendererUI getSelectedRendererUI() {
        return renderingMethods.getActiveComboBoxItem();
    }

    private void layoutComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());

        JPanel sizePanel = new JPanel(new GridBagLayout());
        sizePanel.setBorder(new TitledBorder("ROI"));

        JPanel sizeButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sizeAnalyzedImageButton = new JButton("Auto size by analyzed image");
        sizeAnalyzedImageButton.setEnabled(IJResultsTable.getResultsTable().getAnalyzedImage() != null);
        sizeAnalyzedImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImagePlus analyzedImage = IJResultsTable.getResultsTable().getAnalyzedImage();
                left = top = 0;
                sizeX = analyzedImage.getWidth();
                sizeY = analyzedImage.getHeight();
                leftTextField.setText(left + "");
                topTextField.setText(top + "");
                sizeXTextField.setText(sizeX + "");
                sizeYTextField.setText(sizeY + "");
            }
        });
        sizeResultsButton = new JButton("Auto size by results");
        sizeResultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IJResultsTable rt = IJResultsTable.getResultsTable();
                Rectangle rect = RenderingPlugIn.resizeByResults(rt.getColumnAsDoubles(LABEL_X, PIXEL),
                                                                 rt.getColumnAsDoubles(LABEL_Y, PIXEL),
                                                                 new EmptyRendererUI().magnification.getValue());
                left   = rect.getX();
                top    = rect.getY();
                sizeX  = rect.getWidth();
                sizeY  = rect.getHeight();
                leftTextField.setText(left + "");
                topTextField.setText(top + "");
                sizeXTextField.setText(sizeX + "");
                sizeYTextField.setText(sizeY + "");

                if (rt.columnExists(LABEL_Z)) {
                    renderingMethods.getActiveComboBoxItem().set3D(true);
                    double[] zpos = rt.getColumnAsDoubles(LABEL_Z, NANOMETER);
                    renderingMethods.getActiveComboBoxItem().setZRange(VectorMath.min(zpos), VectorMath.max(zpos));
                } else {
                    renderingMethods.getActiveComboBoxItem().set3D(false);
                }
            }
        });
        sizeButtonsPanel.add(sizeAnalyzedImageButton);
        sizeButtonsPanel.add(sizeResultsButton);

        sizePanel.add(sizeButtonsPanel, GridBagHelper.twoCols());

        topTextField = new JTextField(Double.toString(top), 20);
        leftTextField = new JTextField(Double.toString(left), 20);
        sizeXTextField = new JTextField(Double.toString(sizeX), 20);
        sizeYTextField = new JTextField(Double.toString(sizeY), 20);
        sizePanel.add(new JLabel("Left top x [px]:"), GridBagHelper.leftCol());
        sizePanel.add(leftTextField, GridBagHelper.rightCol());
        sizePanel.add(new JLabel("Left top y [px]:"), GridBagHelper.leftCol());
        sizePanel.add(topTextField, GridBagHelper.rightCol());
        sizePanel.add(new JLabel("Width [px]:"), GridBagHelper.leftCol());
        sizePanel.add(sizeXTextField, GridBagHelper.rightCol());
        sizePanel.add(new JLabel("Height [px]:"), GridBagHelper.leftCol());
        sizePanel.add(sizeYTextField, GridBagHelper.rightCol());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    validateFields();
                    result = DialogResult.OK;
                    Prefs.set("thunderstorm.rendering.index", activeRendererIndex = renderingMethods.getActiveComboBoxItemIndex());
                    Prefs.set("thunderstorm.rendering.imwidth", sizeX);
                    Prefs.set("thunderstorm.rendering.imheight", sizeY);
                    Prefs.set("thunderstorm.rendering.imleft", left);
                    Prefs.set("thunderstorm.rendering.imtop", top);
                    dispose();
                } catch(Exception ex) {
                    IJ.showMessage(ex.toString());
                }
            }
        });
        getRootPane().setDefaultButton(okButton);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = DialogResult.CANCELLED;
                dispose();
            }
        });
        defaultsButton = new JButton("Defaults");
        defaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sizeXTextField.setText(sizeX + "");
                sizeYTextField.setText(sizeY + "");
                renderingMethods.setSelectedItemIndex(activeRendererIndex = 0);
                AnalysisOptionsDialog.resetModuleUIs(renderingMethods.getItems());
            }
        });
        buttonsPanel.add(defaultsButton);
        if(enablePreview) {
            previewButton = new JButton("Use for preview");
            previewButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        validateFields();
                        result = DialogResult.PREVIEW;
                        Prefs.set("thunderstorm.rendering.index", activeRendererIndex = renderingMethods.getActiveComboBoxItemIndex());
                        Prefs.set("thunderstorm.rendering.imwidth", sizeX);
                        Prefs.set("thunderstorm.rendering.imheight", sizeY);
                        Prefs.set("thunderstorm.rendering.imleft", left);
                        Prefs.set("thunderstorm.rendering.imtop", top);
                        dispose();
                    } catch(Exception ex) {
                        IJ.showMessage(ex.toString());
                    }
                }
            });
            buttonsPanel.add(previewButton);
        }
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        add(sizePanel, GridBagHelper.leftCol());
        JPanel cardsPanel = renderingMethods.getPanel("Method:");
        cardsPanel.setBorder(new TitledBorder("Visualization options"));
        add(cardsPanel, GridBagHelper.leftCol());
        add(buttonsPanel, GridBagHelper.leftCol());

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void validateFields() {
        left = Double.parseDouble(leftTextField.getText());
        top = Double.parseDouble(topTextField.getText());
        sizeX = Double.parseDouble(sizeXTextField.getText());
        if(sizeX <= 0.0) {
            throw new IllegalArgumentException("Image width must be positive.");
        }
        sizeY = Double.parseDouble(sizeYTextField.getText());
        if(sizeY <= 0.0) {
            throw new IllegalArgumentException("Image height must be positive.");
        }
        renderingMethods.getActiveComboBoxItem().readParameters();
    }
}
