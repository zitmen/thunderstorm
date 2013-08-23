package cz.cuni.lf1.lge.ThunderSTORM;

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
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.EmptyRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.IRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.IJ;
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
        double[] dx = table.columnExists("dx") ? table.getColumnAsDoubles("dx", MoleculeDescriptor.Units.RADIAN) : null;

        List<IRendererUI> knownRenderers = ModuleLoader.getUIModules(IRendererUI.class);
        //do not show EmptyRenderer
        for(Iterator<IRendererUI> it = knownRenderers.iterator(); it.hasNext();) {
            if(it.next() instanceof EmptyRendererUI) {
                it.remove();
            }
        }
        IRendererUI selectedRendererUI;
        int sizeX, sizeY;
        boolean setAsPreview = false;

        if(MacroParser.isRanFromMacro()) {
            MacroParser parser = new MacroParser(null, null, null, knownRenderers);
            selectedRendererUI = parser.getRendererUI();

            sizeX = Integer.parseInt(Macro.getValue(Macro.getOptions(), "imwidth", "0"));
            sizeY = Integer.parseInt(Macro.getValue(Macro.getOptions(), "imheight", "0"));
        } else {
            RenderingDialog dialog = new RenderingDialog(preview, knownRenderers, (int) Math.ceil(max(xpos)) + 1, (int) Math.ceil(max(ypos)) + 1);
            dialog.setVisible(true);
            if(dialog.result == RenderingDialog.DialogResult.CANCELLED) {
                return;
            }
            if(dialog.result == RenderingDialog.DialogResult.PREVIEW) {
                setAsPreview = true;
            }
            selectedRendererUI = dialog.getSelectedRendererUI();
            sizeX = dialog.sizeX;
            sizeY = dialog.sizeY;
        }

        selectedRendererUI.setSize(sizeX, sizeY);
        IncrementalRenderingMethod method = selectedRendererUI.getImplementation();

        if(setAsPreview) {
            RenderingQueue queue = new RenderingQueue(method, new RenderingQueue.DefaultRepaintTask(method.getRenderedImage()), selectedRendererUI.getRepaintFrequency());
            ((IJResultsTable)table).setPreviewRenderer(queue);
            ((IJResultsTable)table).showPreview();
        } else {
            if(Recorder.record) {
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
}

class RenderingDialog extends JDialog {

    CardsPanel<IRendererUI> cardsPanel;
    JButton previewButton;
    JButton okButton;
    JButton cancelButton;
    DialogResult result = DialogResult.CANCELLED;
    JTextField sizeXTextField;
    JTextField sizeYTextField;
    int sizeX, sizeY;
    boolean enablePreview;

    enum DialogResult {

        CANCELLED, OK, PREVIEW;
    }

    public RenderingDialog(boolean preview, List<IRendererUI> knownRenderers, int sizeX, int sizeY) {
        super(IJ.getInstance(), "Rendering options", true);
        this.enablePreview = preview;
        this.cardsPanel = new CardsPanel<IRendererUI>(knownRenderers, 0);
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
        sizeXTextField = new JTextField(Integer.toString(sizeX), 20);
        sizeYTextField = new JTextField(Integer.toString(sizeY), 20);
        sizePanel.add(new JLabel("Image size X [px]:"), GridBagHelper.leftCol());
        sizePanel.add(sizeXTextField, GridBagHelper.rightCol());
        sizePanel.add(new JLabel("Image size Y [px]:"), GridBagHelper.leftCol());
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
