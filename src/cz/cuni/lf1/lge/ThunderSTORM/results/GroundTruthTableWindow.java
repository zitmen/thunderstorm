package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.ImportExportPlugIn;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

class GroundTruthTableWindow extends GenericTableWindow implements ActionListener {

    private JButton io_import;
    private JButton io_export;
    private JButton showHist;
    
    public GroundTruthTableWindow(String frameTitle) {
        super(frameTitle);
    }
    
    @Override
    protected void packFrame() {
        frame.setPreferredSize(new Dimension(400, 500));
        //
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        showHist = new JButton("Plot histogram...");
        io_import = new JButton("Import...");
        io_export = new JButton("Export...");
        showHist.addActionListener(this);
        io_import.addActionListener(this);
        io_export.addActionListener(this);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(showHist);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(io_import);
        buttons.add(Box.createHorizontalStrut(3));
        buttons.add(io_export);
        //
        Container contentPane = frame.getContentPane();
        JPanel controlsPane = new JPanel();
        controlsPane.setLayout(new BoxLayout(controlsPane, BoxLayout.PAGE_AXIS));
        contentPane.add(tableScrollPane, BorderLayout.CENTER);
        contentPane.add(controlsPane, BorderLayout.SOUTH);
        controlsPane.add(buttons);
        //
        frame.setContentPane(contentPane);
        frame.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == showHist) {
            new IJDistribution().run(IJGroundTruthTable.IDENTIFIER);
        } else if(e.getSource() == io_import) {
            new ImportExportPlugIn().run(ImportExportPlugIn.IMPORT + ";" + IJGroundTruthTable.IDENTIFIER);
        } else if(e.getSource() == io_export) {
            new ImportExportPlugIn().run(ImportExportPlugIn.EXPORT + ";" + IJGroundTruthTable.IDENTIFIER);
        }
    }

}
