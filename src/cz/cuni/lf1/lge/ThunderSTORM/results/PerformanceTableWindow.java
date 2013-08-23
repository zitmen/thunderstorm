package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.ImportExportPlugIn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

class PerformanceTableWindow extends GenericTableWindow implements ActionListener {

    private JButton io_import;
    private JButton io_export;
    private JButton showHist;

    public PerformanceTableWindow(String frameTitle) {
        super(frameTitle);
    }

    @Override
    protected void packFrame() {
        frame.setPreferredSize(new Dimension(800, 600));
        //
        table.setDefaultRenderer(Color.class, new ColorRenderer(true));
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
            new ImportExportPlugIn().run(ImportExportPlugIn.IMPORT + ";" + IJPerformanceTable.IDENTIFIER);
        } else if(e.getSource() == io_export) {
            new ImportExportPlugIn().run(ImportExportPlugIn.EXPORT + ";" + IJPerformanceTable.IDENTIFIER);
        }
    }

    @Override
    protected void dropFile(File f) {
        new ImportExportPlugIn(f.getAbsolutePath()).run(ImportExportPlugIn.IMPORT + ";" + IJPerformanceTable.IDENTIFIER);
    }

    public class ColorRenderer extends JLabel implements TableCellRenderer {

        private Border unselectedBorder = null;
        private Border selectedBorder = null;
        private boolean isBordered;

        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true);    // MUST do this for background to show up.
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) {
            Color newColor = (Color) color;
            setBackground(newColor);
            if(isBordered) {
                if(isSelected) {
                    if(selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if(unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }
            return this;
        }
    }
}
