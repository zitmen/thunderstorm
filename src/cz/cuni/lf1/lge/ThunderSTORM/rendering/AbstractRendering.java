package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units.PIXEL;
import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor.convertFloatToByte;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import static java.lang.Math.ceil;
import java.util.Arrays;
import java.util.Vector;

/**
 * A common abstract superclass implementing RenderingMethod and
 * IncrementalRenderingMethod. You can override the single abstract method
 * drawPoint to quickly add another Rendering Method.
 */
public abstract class AbstractRendering implements RenderingMethod, IncrementalRenderingMethod {

    protected double xmin, xmax, ymin, ymax;
    protected double resolution;
    protected int imSizeX, imSizeY;
    protected double defaultDX;
    protected boolean forceDefaultDX;
    protected double defaultDZ = 5;
    protected double zFrom, zTo, zStep;
    protected int zSlices;
    protected boolean threeDimensions;
    protected boolean colorizeZ;
    protected ImageProcessor[] slices;
    protected ImagePlus image;
    private ImageStack stack;

    /**
     * A class for creating objects of sublasses of AbstractRendering.
     */
    protected static abstract class AbstractBuilder<BuilderType extends AbstractBuilder, BuiltType extends AbstractRendering> {

        protected double resolution, xmin = 0, xmax, ymin = 0, ymax;
        protected int imSizeY, imSizeX;
        protected boolean resolutionWasSet = false, roiWasSet = false, sizeWasSet = false;
        protected final static double defaultResolution = 20;
        private double defaultDX = 0.2;
        private boolean forceDefaultDX = true;
        private double defaultDZ = 5;
        private double zFrom = Double.NEGATIVE_INFINITY, zTo = Double.POSITIVE_INFINITY, zStep = Double.POSITIVE_INFINITY;
        private int zSlices = 1;
        private boolean threeDimensions = false;
        private boolean colorizeZ = false;

        /**
         * Sets the desired resolution of the final image. In localization units
         * per pixel of the original image. <br> If the molecule locations are
         * in nm, value of 20 signifies that one pixel in the superresolution
         * image will be 20nm big <br> If the molecule locations are in pixels,
         * a value of 0.2 signifies that the pixel size of the superresoultion
         * image will be 0.2 times the pixel size of the original image.(5x
         * smaller)
         *
         */
        public BuilderType resolution(double nmPerPixel) {
            if(nmPerPixel <= 0) {
                throw new IllegalArgumentException("Resolution must be positive. Passed value = " + nmPerPixel);
            }
            this.resolution = nmPerPixel;
            resolutionWasSet = true;
            return (BuilderType) this;
        }

        /**
         * Sets the region of interest. Only molecules inside the roi will be
         * rendered.
         *
         */
        public BuilderType roi(double xmin, double xmax, double ymin, double ymax) {
            if(xmax < xmin || ymax < ymin) {
                throw new IllegalArgumentException("xmax (ymax) must be greater than xmin (ymin)");
            }
            this.xmin = xmin;
            this.xmax = xmax;
            this.ymin = ymin;
            this.ymax = ymax;
            roiWasSet = true;
            return (BuilderType) this;
        }

        /**
         * Sets the size of the superresolution image
         */
        public BuilderType imageSize(int x, int y) {
            if(x <= 0 || y <= 0) {
                throw new IllegalArgumentException("Image size must be positive. Passed values = " + x + " " + y);
            }
            this.imSizeX = x;
            this.imSizeY = y;
            sizeWasSet = true;
            return (BuilderType) this;
        }

        public BuilderType defaultDX(double defaultDX) {
            if(defaultDX <= 0) {
                throw new IllegalArgumentException("Default dx must be positive. Passed value = " + defaultDX);
            }
            this.defaultDX = defaultDX;
            resolutionWasSet = true;
            return (BuilderType) this;
        }

        /**
         * Specifies whether the defaultDX value is used even if a dx (lateral
         * uncertainty) value is provided for each molecule
         */
        public BuilderType forceDefaultDX(boolean bool) {
            this.forceDefaultDX = bool;
            return (BuilderType) this;
        }

        public BuilderType defaultDZ(double defaultDZ) {
            if(defaultDZ <= 0) {
                throw new IllegalArgumentException("Default dz must be positive. Passed value = " + defaultDZ);
            }
            this.defaultDZ = defaultDZ;
            return (BuilderType) this;
        }

        public BuilderType zRange(double from, double to, double step) {
            if(to <= from) {
                throw new IllegalArgumentException("Z range \"from\" value (" + from + ") must be smaller than \"to\" value (" + to + ").");
            }
            if(step <= 0) {
                throw new IllegalArgumentException("Z range \"step\" value must be positive. Passed value = " + step);
            }
            this.zFrom = from;
            this.zStep = step;
            this.zSlices = (int) ((to - from) / step);
            if(zSlices < 1) {
                throw new RuntimeException("Invalid z range: Must have at least one slice.");
            }
            this.zTo = zSlices * step + from;
            threeDimensions = true;
            return (BuilderType) this;
        }
        
        public BuilderType colorizeZ(boolean colorize) {
            this.colorizeZ = colorize;
            return (BuilderType) this;
        }

        protected void validate() {
            if(!roiWasSet && !sizeWasSet) {
                throw new IllegalArgumentException("Image size must be resolved while building. Set at least image size or roi.");
            }
            if(!roiWasSet && sizeWasSet) {
                if(!resolutionWasSet) {
                    resolution = defaultResolution;
                }
                xmax = imSizeX * resolution;
                ymax = imSizeY * resolution;
                xmin = 0;
                ymin = 0;
            } else if(roiWasSet && !sizeWasSet) {
                if(!resolutionWasSet) {
                    resolution = defaultResolution;
                }
                imSizeX = (int) (ceil((xmax - xmin) / resolution));
                imSizeY = (int) (ceil((ymax - ymin) / resolution));
            } else {
                if(resolutionWasSet) {
                    int newImSizeX = (int) ceil((xmax - xmin) / resolution);
                    int newImSizeY = (int) ceil((ymax - ymin) / resolution);
                    if(newImSizeX != imSizeX || newImSizeY != imSizeY) {
                        throw new IllegalArgumentException("Invalid combination of image size, roi and resolution. Set only two of them.");
                    }
                } else {
                    resolution = (xmax - xmin) / imSizeX;
                    if(Math.abs(resolution - (ymax - ymin) / imSizeY) > 0.0001) {
                        throw new IllegalArgumentException("Resolution in x and y appears to be different.");
                    }
                }
            }
        }

        /**
         * Returns the newly created object.
         */
        public abstract BuiltType build();
    }

    protected AbstractRendering(AbstractBuilder builder) {
        this.xmin = builder.xmin;
        this.xmax = builder.xmax;
        this.ymin = builder.ymin;
        this.ymax = builder.ymax;
        this.resolution = builder.resolution;
        this.imSizeX = builder.imSizeX;
        this.imSizeY = builder.imSizeY;
        this.defaultDX = builder.defaultDX;
        this.forceDefaultDX = builder.forceDefaultDX;
        this.defaultDZ = builder.defaultDZ;
        this.zFrom = builder.zFrom;
        this.zSlices = builder.zSlices;
        this.zStep = builder.zStep;
        this.zTo = builder.zTo;
        this.threeDimensions = builder.threeDimensions;
        this.colorizeZ = builder.colorizeZ;
        slices = new ImageProcessor[zSlices];
        stack = new ImageStack(imSizeX, imSizeY);
        for(int i = 0; i < zSlices; i++) {
            slices[i] = new FloatProcessor(imSizeX, imSizeY);
            stack.addSlice((i * zStep + zFrom) + " to " + ((i + 1) * zStep + zFrom), slices[i]);
        }
        image = new ImagePlus(getRendererName(), stack);
    }

    @Override
    public void addToImage(double[] x, double[] y, double[] z, double[] dx) {
        for(int i = 0; i < x.length; i++) {
            double zVal = z != null ? z[i] : 0;
            double dxVal = dx != null && !forceDefaultDX ? dx[i] : defaultDX;
            drawPoint(x[i], y[i], zVal, dxVal);
        }
    }

    @Override
    public void addToImage(Vector<Molecule> fits) {
        if(fits.isEmpty()) {
            return;
        }
        MoleculeDescriptor descriptor = fits.get(0).descriptor;
        Units unitsDX = null;
        int dxIndex = -1;
        if(!forceDefaultDX) {
            dxIndex = descriptor.getParamIndex(MoleculeDescriptor.Fitting.LABEL_THOMPSON);
            unitsDX = descriptor.units.elementAt(descriptor.getParamColumn(MoleculeDescriptor.Fitting.LABEL_THOMPSON));
        }
        Units unitsX = descriptor.units.elementAt(descriptor.getParamColumn(PSFModel.Params.LABEL_X));
        Units unitsY = descriptor.units.elementAt(descriptor.getParamColumn(PSFModel.Params.LABEL_Y));

        for(int i = 0, im = fits.size(); i < im; i++) {
            Molecule fit = fits.elementAt(i);
            double zVal = fit.hasParam(PSFModel.Params.LABEL_Z) ? fit.getParam(PSFModel.Params.LABEL_Z) : 0;
            double dxVal = dxIndex < 0 ? defaultDX : unitsDX.convertTo(PIXEL, fit.getParamAt(dxIndex));

            //
            drawPoint(unitsX.convertTo(PIXEL, fit.getX()), unitsY.convertTo(PIXEL, fit.getY()), zVal, dxVal);
        }
    }

    @Override
    public ImagePlus getRenderedImage() {
        if(colorizeZ) {
            int w = image.getWidth(), h = image.getHeight(), imsize = w * h;
            byte [] H = new byte[imsize];
            byte [] S = new byte[imsize];
            byte [] B;
            Arrays.fill(S, (byte)255);
            ImageStack clr_stack = new ImageStack(w, h);
            for(int z = 0; z < slices.length; z++) {
                B = (byte[])convertFloatToByte((FloatProcessor)slices[z]).getPixels();
                Arrays.fill(H, (byte)(255.0 / (double)slices.length * (double)z));
                ColorProcessor clr = new ColorProcessor(w, h);
                clr.setHSB(H, S, B);
                clr_stack.addSlice(stack.getSliceLabel(z+1), clr);
            }
            return new ImagePlus(getRendererName(), clr_stack);
        }
        return image;
    }

    @Override
    public ImagePlus getRenderedImage(double[] x, double[] y, double[] z, double[] dx) {
        reset();
        addToImage(x, y, z, dx);
        return getRenderedImage();
    }

    protected abstract void drawPoint(double x, double y, double z, double dx);

    @Override
    public void reset() {
        for(int i = 0; i < slices.length; i++) {
            float[] px = (float[]) slices[i].getPixels();
            Arrays.fill(px, 0);
        }
        stack = new ImageStack(imSizeX, imSizeY);
        for(int i = 0; i < zSlices; i++) {
            stack.addSlice((i * zStep + zFrom) + " to " + ((i + 1) * zStep + zFrom), slices[i]);
        }
        image.setStack(stack);
    }

    protected boolean isInBounds(double x, double y) {
        return x >= xmin && x < xmax && y >= ymin && y < ymax && !Double.isNaN(x) && !Double.isNaN(y);
    }
}
