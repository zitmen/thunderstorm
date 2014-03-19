package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.drift.DriftResults;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.gui.Plot;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class FiducialDriftCorrection extends PostProcessingModule {

    @Override
    public String getMacroName() {
        return "fiducial";
    }

    @Override
    public String getTabName() {
        return "Fiducial Drift correction";
    }

    @Override
    protected JPanel createUIPanel() {
        JPanel panel = new JPanel();
        JButton runButton = new JButton("run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });
        panel.add(runButton);
        return panel;
    }

    @Override
    protected void runImpl() {
//        List<Molecule> molecules = getMoleculeList();
//        FiducialDriftEstimator estimator = new FiducialDriftEstimator();
//        DriftResults results = estimator.estimateDrift(molecules);  
//        showPlot(results);
    }

    public void showPlot(DriftResults results) {
        int minFrame  = results.getMinFrame();
        int maxFrame = results.getMaxFrame();
        
        int gridTicks = 200;
        double tickStep = (maxFrame - minFrame) / (double) gridTicks;
        double[] grid = new double[gridTicks];
        double[] driftX = new double[gridTicks];
        double[] driftY = new double[gridTicks];
        for(int i = 0; i < gridTicks; i++) {
            grid[i] = i * tickStep + minFrame;
            Point2D.Double offset = results.getInterpolatedDrift(grid[i]);
            driftX[i] = offset.x;
            driftY[i] = offset.y;
        }

        Plot plot = new Plot("Drift", "frame", "drift", (float[]) null, null);
        plot.setFrameSize(1280, 720);
        plot.setLimits(minFrame, maxFrame,
                MathProxy.min(VectorMath.min(results.getDriftDataX()), VectorMath.min(results.getDriftDataY())),
                MathProxy.max(VectorMath.max(results.getDriftDataX()), VectorMath.max(results.getDriftDataY())));
        plot.setColor(new Color(255,128,128));
        plot.addPoints(results.getDriftDataFrame(), results.getDriftDataX(), Plot.CROSS);
        plot.draw();
        plot.setColor(Color.red);
        plot.addPoints(grid, driftX, Plot.LINE);
        plot.setColor(new Color(128, 255, 128));
        plot.addPoints(results.getDriftDataFrame(), results.getDriftDataY(), Plot.CROSS);
        plot.setColor(Color.green);
        plot.addPoints(grid, driftY, Plot.LINE);
        plot.show();
    }

    
}
