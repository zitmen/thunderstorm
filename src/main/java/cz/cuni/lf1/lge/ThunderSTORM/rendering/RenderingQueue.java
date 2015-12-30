package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import ij.ImagePlus;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class processes rendering tasks serially. It consists of a queue and a
 * thread that takes tasks out of the queue and processes them. Can perform a
 * user defined task after every [frequency] invocations.
 */
public class RenderingQueue {

    private ThreadPoolExecutor executor = null;
    Runnable repaintTask;
    public IncrementalRenderingMethod method;
    int taskCounter = 0;
    int repaintFrequency;

    /**
     * Example: new RenderingQueue(method, new Runnable(){public void
     * run(){image.repaint();}},10) will call image.repaint() every 10 tasks
     * executed
     *
     * @param method rendering method
     * @param repaintTask Runnable, which will be called after executing every
     * repaintFrequency tasks. Or null if no periodic action should be run.
     * @param repaintFrequency specifies how often to call repaintTask.run().
     */
    public RenderingQueue(IncrementalRenderingMethod method, Runnable repaintTask, int repaintFrequency) {
        if(method != null && repaintFrequency > 0) {
            this.repaintTask = repaintTask;
            this.method = method;
            this.repaintFrequency = repaintFrequency;

            //executor with one thread, that will die after not being used for 30 seconds      
            executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            executor.allowCoreThreadTimeOut(true);
        }
    }

    public void renderLater(List<Molecule> fits) {
        if(executor != null) {
            executor.execute(new RenTask(fits));
        }
    }

    public void repaintLater() {
        if(executor != null) {
            executor.execute(repaintTask);
        }
    }

    public void invokeLater(Runnable r) {
        if(executor != null) {
            executor.execute(r);
        }
    }

    public void resetLater() {
        if(executor != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    method.reset();
                }
            });
        }
    }

    public void shutdown() {
        if(executor != null) {
            executor.shutdown();
        }
    }

    class RenTask implements Runnable {

        List<Molecule> fits;

        public RenTask(List<Molecule> fits) {
            this.fits = fits;
        }

        @Override
        public void run() {
            method.addToImage(fits);
            taskCounter++;                // no need to use atomic counter because this method will be run only from single threaded executor
            if(taskCounter % repaintFrequency == 0) {
                repaintTask.run();
            }
        }
    }

    public static class DefaultRepaintTask implements Runnable {

        ImagePlus renderedImage;

        public DefaultRepaintTask(ImagePlus renderedImage) {
            this.renderedImage = renderedImage;
        }

        @Override
        public void run() {
            renderedImage.show();
            if(renderedImage.isVisible()) {
                double upperRange = findQuantileHisto(renderedImage, 0.99);
                //IJ.log("upper image range: " + upperRange);
                renderedImage.setDisplayRange(0, upperRange);
                renderedImage.updateAndDraw();
            }
        }
        /*
         private static Object [] getFloatImageArray(ImagePlus imp) {
         ImageStack stack = imp.getStack();
         ImageStack f_stack = new ImageStack(imp.getWidth(), imp.getHeight());
         for(int z = 0, zm = stack.getSize(); z < zm; z++) {
         f_stack.addSlice(null, stack.getProcessor(z+1).convertToFloat());
         }
         return new ImagePlus(null, f_stack).getStack().getImageArray();
         }
         */
        private static double findMaxStackValue(ImagePlus imp) {
            Object[] stack = imp.getStack().getImageArray();//getFloatImageArray(imp);
            double max = 0;
            for(int i = 0; i < stack.length; i++) {
                float[] pixels = (float[]) stack[i];
                if(pixels != null) {
                    for(int j = 0; j < pixels.length; j++) {
                        double val = pixels[j];
                        if(val > max) {
                            max = val;
                        }
                    }
                }
            }
            return max;
        }

        private static double findQuantileHisto(ImagePlus imp, double quantile) {
            assert imp.getType() == ImagePlus.GRAY32;
            assert quantile > 0 && quantile < 1;

            double max = findMaxStackValue(imp);
            int nBins = 1000;
            double binSize = max / nBins;
            int[] binCounts = new int[nBins + 1];
            Object[] stack = imp.getStack().getImageArray();//getFloatImageArray(imp);

            int totalNonZeroPixels = 0;
            for(int i = 0; i < stack.length; i++) {
                float[] pixels = (float[]) stack[i];
                if(pixels != null) {
                    for(int j = 0; j < pixels.length; j++) {
                        double val = pixels[j];
                        if(val != 0) {
                            totalNonZeroPixels++;
                            int bin = (int) (val / binSize);
                            binCounts[bin]++;
                        }
                    }
                }
            }
            int requiredPixels = (int) (totalNonZeroPixels * (quantile));
            int cumulativeCount = 0;
            for(int i = 0; i <= nBins; i++) {
                cumulativeCount += binCounts[i];
                if(cumulativeCount > requiredPixels) {
                    return (i + 1) * binSize;
                }
            }
            return max;
        }
    }
}
