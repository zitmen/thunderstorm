package cz.cuni.lf1.lge.ThunderSTORM;

import ij.ImagePlus;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;

/**
 * Brief info ... todo
 * 
 * Description ... todo
 */
public final class RenderingPlugIn implements ExtendedPlugInFilter {

    private final int pluginFlags = -1; // TODO
    
    /**
     * Returns flags specifying capabilities of the plugin.
     * 
     * @param command command, e.g., "analysis", "rendering", etc. (not required)
     * @param imp ImagePlus instance holding the active image (not required)
     * @return flags specifying capabilities of the plugin
     */
    @Override
    public int setup(String command, ImagePlus imp) {
        return pluginFlags;
    }
    
    /**
     * Show the options dialog ...todo
     * 
     * @param command command, e.g., "analysis", "rendering", etc. (not required in this version)
     * @param imp ImagePlus instance holding the active image (not required in this version)
     * @param pfr (not required in this version)
     * @return 
     */
    @Override
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
        // TODO
        return DONE;
    }

    /**
     * Gives the plugin information about the number of passes through the image stack we want to process.
     * 
     * Description...todo
     * 
     * @param nPasses number of passes through the image stack we want to process
     */
    @Override
    public void setNPasses(int nPasses) {
        // TODO
    }

    /**
     * Run the plugin.
     * 
     * Description...todo
     * 
     * @param ip input image
     */
    @Override
    public void run(ImageProcessor ip) {
        // TODO
    }
}