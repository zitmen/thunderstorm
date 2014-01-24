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
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.AbstractRenderingUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.EmptyRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.IRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RenderingPlugIn implements PlugIn {

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
        if(table.columnExists(MoleculeDescriptor.Fitting.LABEL_THOMPSON)) {
            dx = table.getColumnAsDoubles(MoleculeDescriptor.Fitting.LABEL_THOMPSON, MoleculeDescriptor.Units.PIXEL);
        }

        List<IRendererUI> knownRenderers = ModuleLoader.getUIModules(IRendererUI.class);
        //do not show EmptyRenderer
        for(Iterator<IRendererUI> it = knownRenderers.iterator(); it.hasNext();) {
            IRendererUI rendererUI = it.next();
            if(rendererUI instanceof EmptyRendererUI) {
                it.remove();
            }else if(rendererUI instanceof AbstractRenderingUI){
                ((AbstractRenderingUI)rendererUI).setShowRepaintFrequency(false);
            }
        }
        IRendererUI selectedRendererUI;
        int sizeX, sizeY, left, top;
        boolean setAsPreview = false;

        if(MacroParser.isRanFromMacro()) {
            MacroParser parser = new MacroParser(null, null, null, knownRenderers);
            selectedRendererUI = parser.getRendererUI();

            left  = Integer.parseInt(Macro.getValue(Macro.getOptions(), "imleft", "0"));
            top   = Integer.parseInt(Macro.getValue(Macro.getOptions(), "imtop", "0"));
            sizeX = Integer.parseInt(Macro.getValue(Macro.getOptions(), "imwidth", "0")) - left + 1;
            sizeY = Integer.parseInt(Macro.getValue(Macro.getOptions(), "imheight", "0")) - top + 1;
        } else {
            int guessedLeft;
            int guessedTop;
            int guessedWidth;
            int guessedHeight;
            ImagePlus im;
            if(IJResultsTable.IDENTIFIER.equals(table.getTableIdentifier()) && (im = ((IJResultsTable) table).getAnalyzedImage()) != null) {
                guessedLeft = 0;
                guessedTop = 0;
                guessedWidth = im.getWidth();
                guessedHeight = im.getHeight();
            } else {
                guessedLeft = Math.max((int) Math.floor(min(xpos)) - 1, 0);
                guessedTop = Math.max((int) Math.floor(min(ypos)) - 1, 0);
                guessedWidth = (int) Math.ceil(max(xpos))  + 1;
                guessedHeight = (int) Math.ceil(max(ypos)) + 1;
            }
            RenderingDialog dialog = new RenderingDialog(preview, knownRenderers, guessedLeft, guessedTop, guessedWidth - guessedLeft, guessedHeight - guessedTop);
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
            ((IJResultsTable) table).setPreviewRenderer(queue);
            ((IJResultsTable) table).showPreview();
        } else {
            if(Recorder.record) {
                Recorder.recordOption("imleft", Integer.toString(left));
                Recorder.recordOption("imtop", Integer.toString(top));
                Recorder.recordOption("imwidth", Integer.toString(sizeX));
                Recorder.recordOption("imheight", Integer.toString(sizeY));
                MacroParser.recordRendererUI(selectedRendererUI);
            }

            method.reset();
            method.addToImage(xpos, ypos, z, dx);
            new RenderingQueue.DefaultRepaintTask(method.getRenderedImage()).run();
        }
    }

    private double max(double[] arr) {
        double max = arr[0];
        for(int i = 0; i < arr.length; i++) {
            if(arr[i] > max) {
                max = arr[i];
            }
        }
        return max;
    }
    
    private double min(double[] arr) {
        double min = arr[0];
        for(int i = 0; i < arr.length; i++) {
            if(arr[i] < min) {
                min = arr[i];
            }
        }
        return min;
    }
}

class RenderingDialog extends JDialog {

    CardsPanel<IRendererUI> cardsPanel;
    JButton previewButton;
    JButton okButton;
    JButton cancelButton;
    JButton defaultsButton;
    DialogResult result = DialogResult.CANCELLED;
    JTextField topTextField;
    JTextField leftTextField;
    JTextField sizeXTextField;
    JTextField sizeYTextField;
    int left, top, sizeX, sizeY;
    boolean enablePreview;

    enum DialogResult {

        CANCELLED, OK, PREVIEW;
    }

    public RenderingDialog(boolean preview, List<IRendererUI> knownRenderers, int left, int top, int sizeX, int sizeY) {
        super(IJ.getInstance(), "Rendering options", true);
        this.enablePreview = preview;
        this.cardsPanel = new CardsPanel<IRendererUI>(knownRenderers, 0);
        this.left = left;
        this.top = top;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        layoutComponents();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    public IRendererUI getSelectedRendererUI() {
        return cardsPanel.getActiveComboBoxItem();
    }

    private void layoutComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        JPanel sizePanel = new JPanel(new GridBagLayout());
        topTextField = new JTextField(Integer.toString(top), 20);
        leftTextField = new JTextField(Integer.toString(left), 20);
        sizeXTextField = new JTextField(Integer.toString(sizeX), 20);
        sizeYTextField = new JTextField(Integer.toString(sizeY), 20);
        sizePanel.add(new JLabel("Image left offset [px]:"), GridBagHelper.leftCol());
        sizePanel.add(leftTextField, GridBagHelper.rightCol());
        sizePanel.add(new JLabel("Image top offset [px]:"), GridBagHelper.leftCol());
        sizePanel.add(topTextField, GridBagHelper.rightCol());
        sizePanel.add(new JLabel("Original image width [px]:"), GridBagHelper.leftCol());
        sizePanel.add(sizeXTextField, GridBagHelper.rightCol());
        sizePanel.add(new JLabel("Original image height [px]:"), GridBagHelper.leftCol());
        sizePanel.add(sizeYTextField, GridBagHelper.rightCol());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    validateFields();
                    result = DialogResult.OK;
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
                cardsPanel.setSelectedItemIndex(0);
                AnalysisOptionsDialog.resetModuleUIs(cardsPanel.getItems());
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
        add(cardsPanel.getPanel("Renderer:"), GridBagHelper.leftCol());
        add(buttonsPanel, GridBagHelper.leftCol());

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void validateFields() {
        left = Integer.parseInt(leftTextField.getText());
        if(left < 0) {
            throw new IllegalArgumentException("Image offset must not be negative.");
        }
        top = Integer.parseInt(topTextField.getText());
        if(top < 0) {
            throw new IllegalArgumentException("Image offset must not be negative.");
        }
        sizeX = Integer.parseInt(sizeXTextField.getText());
        if(sizeX < 1) {
            throw new IllegalArgumentException("Image width must be positive.");
        }
        sizeY = Integer.parseInt(sizeYTextField.getText());
        if(sizeY < 1) {
            throw new IllegalArgumentException("Image height must be positive.");
        }
        cardsPanel.getActiveComboBoxItem().readParameters();
    }
}
