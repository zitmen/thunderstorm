package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.ModuleLoader;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IBiplaneEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.IRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.DialogStub;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterTracker;
import cz.cuni.lf1.lge.ThunderSTORM.util.PluginCommands;
import ij.*;
import ij.measure.Calibration;
import ij.plugin.frame.Recorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class BiplaneAnalysisOptionsDialog extends DialogStub implements ActionListener {

    private ParameterKey.String filterName;
    private ParameterKey.String detectorName;
    private ParameterKey.String estimatorName;
    private ParameterKey.String rendererName;
    private ParameterKey.String rawImage1Stack;
    private ParameterKey.String rawImage2Stack;

    private List<IFilterUI> filters;
    private List<IDetectorUI> detectors;
    private List<IBiplaneEstimatorUI> estimators;
    private List<IRendererUI> renderers;

    public BiplaneAnalysisOptionsDialog(List<IFilterUI> filters, List<IDetectorUI> detectors,
                                        List<IBiplaneEstimatorUI> estimators, List<IRendererUI> renderers) {
        super(new ParameterTracker("thunderstorm.analysis.biplane"), IJ.getInstance(), "Run biplane analysis");
        params.getComponentHandlers().addForStringParameters(CardsPanel.class, new CardsPanelMacroUIHandler());

        filterName = params.createStringField("filter", null, filters.get(0).getName());
        detectorName = params.createStringField("detector", null, detectors.get(0).getName());
        estimatorName = params.createStringField("estimator", null, estimators.get(0).getName());
        rendererName = params.createStringField("renderer", null, renderers.get(0).getName());
        rawImage1Stack = params.createStringField("raw_image1_stack", null, "");
        rawImage2Stack = params.createStringField("raw_image2_stack", null, "");

        this.filters = filters;
        this.detectors = detectors;
        this.estimators = estimators;
        this.renderers = renderers;
    }

    @Override
    protected void layoutComponents() {
        JPanel pane = new JPanel();
        pane.setLayout(new GridBagLayout());

        GridBagConstraints componentConstraints = new GridBagConstraints();
        componentConstraints.gridx = 0;
        componentConstraints.fill = GridBagConstraints.BOTH;
        componentConstraints.weightx = 1;

        JPanel cameraPanel = new JPanel(new BorderLayout());
        JButton cameraSetup = new JButton("Camera setup");
        cameraSetup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MacroParser.runNestedWithRecording(PluginCommands.CAMERA_SETUP, null);
            }
        });
        cameraPanel.add(cameraSetup);
        cameraPanel.setBorder(BorderFactory.createTitledBorder("Camera"));
        pane.add(cameraPanel, componentConstraints);

        JPanel dataPanel = new JPanel(new GridBagLayout());
        JComboBox<String> rawImage1ComboBox = createOpenImagesComboBox(false);
        JComboBox<String> rawImage2ComboBox = createOpenImagesComboBox(false);
        rawImage1ComboBox.setPreferredSize(new Dimension(150, (int) rawImage1ComboBox.getPreferredSize().getHeight()));
        rawImage2ComboBox.setPreferredSize(new Dimension(150, (int) rawImage2ComboBox.getPreferredSize().getHeight()));
        if (rawImage1ComboBox.getItemCount() < 2) {
            throw new RuntimeException("Two images are required to be opened for biplane analysis!");
        }
        rawImage1ComboBox.setSelectedIndex(0);
        rawImage2ComboBox.setSelectedIndex(1);
        rawImage1Stack.registerComponent(rawImage1ComboBox);
        rawImage2Stack.registerComponent(rawImage2ComboBox);
        dataPanel.add(new JLabel("First plane:"), GridBagHelper.leftCol());
        dataPanel.add(rawImage1ComboBox, GridBagHelper.rightCol());
        dataPanel.add(new JLabel("Second plane:"), GridBagHelper.leftCol());
        dataPanel.add(rawImage2ComboBox, GridBagHelper.rightCol());
        dataPanel.setBorder(BorderFactory.createTitledBorder("Source data"));
        pane.add(dataPanel, componentConstraints);

        CardsPanel<IFilterUI> filtersPanel = new CardsPanel<IFilterUI>(filters, 0);
        filterName.registerComponent(filtersPanel);
        JPanel p = filtersPanel.getPanel("Filter:");
        p.setBorder(BorderFactory.createTitledBorder("Image filtering"));
        pane.add(p, componentConstraints);

        CardsPanel<IDetectorUI> detectorsPanel = new CardsPanel<IDetectorUI>(detectors, 0);
        detectorName.registerComponent(detectorsPanel);
        p = detectorsPanel.getPanel("Method:");
        p.setBorder(BorderFactory.createTitledBorder("Approximate localization of molecules"));
        pane.add(p, componentConstraints);

        CardsPanel<IBiplaneEstimatorUI> estimatorsPanel = new CardsPanel<IBiplaneEstimatorUI>(estimators, 0);
        estimatorName.registerComponent(estimatorsPanel);
        p = estimatorsPanel.getPanel("Method:");
        p.setBorder(BorderFactory.createTitledBorder("Sub-pixel localization of molecules"));
        pane.add(p, componentConstraints);

        CardsPanel<IRendererUI> renderersPanel = new CardsPanel<IRendererUI>(renderers, 0);
        rendererName.registerComponent(renderersPanel);
        p = renderersPanel.getPanel("Method:");
        p.setBorder(BorderFactory.createTitledBorder("Visualisation of the results"));
        pane.add(p, componentConstraints);
        //
        JButton defaults = new JButton("Defaults");
        JButton ok = new JButton("Ok");
        JButton cancel = new JButton("Cancel");

        defaults.addActionListener(this);
        ok.addActionListener(this);
        cancel.addActionListener(this);

        JPanel buttons = new JPanel();
        buttons.add(defaults);
        buttons.add(Box.createHorizontalStrut(30));
        buttons.add(ok);
        buttons.add(cancel);
        pane.add(buttons, componentConstraints);
        pane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JScrollPane scrollPane = new JScrollPane(pane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane);
        getRootPane().setDefaultButton(ok);
        pack();

        int maxScreenHeight = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
        if(getHeight() > maxScreenHeight) {
            setSize(getWidth(), maxScreenHeight);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if("Ok".equals(e.getActionCommand())) {
                params.readDialogOptions();
                Thresholder.setActiveFilter(getActiveFilterUIIndex());
                try {
                    getActiveFilterUI().readParameters();
                    getActiveDetectorUI().readParameters();
                    getActiveEstimatorUI().readParameters();
                    getActiveRendererUI().readParameters();
                } catch(Exception ex) {
                    IJ.error("Error parsing parameters: " + ex.toString());
                    return;
                }
                params.savePrefs();
                if(Recorder.record) {
                    getActiveFilterUI().recordOptions();
                    getActiveDetectorUI().recordOptions();
                    getActiveEstimatorUI().recordOptions();
                    getActiveRendererUI().recordOptions();
                    params.recordMacroOptions();
                }

                Calibration cal = new Calibration();
                cal.setUnit("um");
                cal.pixelWidth = cal.pixelHeight = CameraSetupPlugIn.getPixelSize() / 1000.0;
                getFirstPlaneStack().setCalibration(cal);
                getSecondPlaneStack().setCalibration(cal);

                result = JOptionPane.OK_OPTION;
                dispose();
            } else if("Cancel".equals(e.getActionCommand())) {
                dispose();
            } else if("Defaults".equals(e.getActionCommand())) {
                params.resetToDefaults(true);
                //noinspection unchecked
                AnalysisOptionsDialog.resetModuleUIs(filters, detectors, estimators, renderers);
            }
        } catch(Exception ex) {
            IJ.handleException(ex);
        }
    }

    @Override
    public int showAndGetResult() {
        if(MacroParser.isRanFromMacro()) {
            params.readMacroOptions();
            String options = Macro.getOptions();
            getActiveFilterUI().readMacroOptions(options);
            getActiveDetectorUI().readMacroOptions(options);
            getActiveEstimatorUI().readMacroOptions(options);
            getActiveRendererUI().readMacroOptions(options);

            return JOptionPane.OK_OPTION;
        } else {
            return super.showAndGetResult();
        }
    }

    public IFilterUI getActiveFilterUI() {
        return ModuleLoader.moduleByName(filters, filterName.getValue());
    }

    public int getActiveFilterUIIndex() {
        return ModuleLoader.moduleIndexByName(filters, filterName.getValue());
    }

    public IDetectorUI getActiveDetectorUI() {
        return ModuleLoader.moduleByName(detectors, detectorName.getValue());
    }

    public IBiplaneEstimatorUI getActiveEstimatorUI() { return ModuleLoader.moduleByName(estimators, estimatorName.getValue()); }

    public IRendererUI getActiveRendererUI() { return ModuleLoader.moduleByName(renderers, rendererName.getValue()); }

    public ImagePlus getFirstPlaneStack() {
        return WindowManager.getImage(rawImage1Stack.getValue());
    }

    public ImagePlus getSecondPlaneStack() {
        return WindowManager.getImage(rawImage2Stack.getValue());
    }
}
