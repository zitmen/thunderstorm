package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_Z;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units.NANOMETER;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units.PIXEL;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY;
import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;

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
    protected double defaultDZ;
    protected boolean forceDefaultDX;
    protected boolean forceDefaultDZ;
    protected double zFrom, zTo, zStep;
    protected int zSlices;
    protected boolean threeDimensions;
    protected boolean colorize;
    protected LUT colorizationLut;
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
        private boolean forceDefaultDZ = true;
        private double defaultDZ = 5;
        private double zFrom = Double.NEGATIVE_INFINITY, zTo = Double.POSITIVE_INFINITY, zStep = Double.POSITIVE_INFINITY;
        private int zSlices = 1;
        private boolean threeDimensions = false;
        private boolean colorize = false;
        private LUT colorizationLut = null;

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

        public BuilderType defaultDZ(double defaultDZ) {
            if(defaultDZ <= 0) {
                throw new IllegalArgumentException("Default dz must be positive. Passed value = " + defaultDZ);
            }
            this.defaultDZ = defaultDZ;
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

        public BuilderType forceDefaultDZ(boolean bool) {
            this.forceDefaultDZ = bool;
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

        public BuilderType colorize(boolean colorize) {
            this.colorize = colorize;
            return (BuilderType) this;
        }

        public BuilderType colorizationLUT(LUT lut) {
            this.colorizationLut = lut;
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
                imSizeX = (int)((xmax - xmin) / resolution);
                imSizeY = (int)((ymax - ymin) / resolution);
            } else {
                if(resolutionWasSet) {
                    int newImSizeX = (int)((xmax - xmin) / resolution);
                    int newImSizeY = (int)((ymax - ymin) / resolution);
                    if(newImSizeX != imSizeX || newImSizeY != imSizeY) {
                        throw new IllegalArgumentException("Invalid combination of image size, roi and resolution. Set only two of them.");
                    }
                } else {
                    resolution = (xmax - xmin) / imSizeX;
                    if(Math.abs(resolution - (ymax - ymin) / imSizeY) > 0.001) {
                        throw new IllegalArgumentException("Resolution in x and y appears to be different.");
                    }
                }
            }
            long pixelcount = (long) imSizeX * (long) imSizeY;
            if(pixelcount > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Tried to create too big image (" + imSizeX + " x " + imSizeY + "). Check that parameters are correct and use appropriate units.");
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
        this.forceDefaultDX = builder.forceDefaultDX;
        this.forceDefaultDZ = builder.forceDefaultDZ;
        this.defaultDX = builder.defaultDX;
        this.defaultDZ = builder.defaultDZ;
        this.zFrom = builder.zFrom;
        this.zSlices = builder.zSlices;
        this.zStep = builder.zStep;
        this.zTo = builder.zTo;
        this.threeDimensions = builder.threeDimensions;
        this.colorize = builder.colorize;
        this.colorizationLut = builder.colorizationLut;
        slices = new ImageProcessor[zSlices];
        stack = new ImageStack(imSizeX, imSizeY);
        for(int i = 0; i < zSlices; i++) {
            slices[i] = new FloatProcessor(imSizeX, imSizeY);
            stack.addSlice((i * zStep + zFrom) + " to " + ((i + 1) * zStep + zFrom), slices[i]);
        }
        image = new ImagePlus(getRendererName(), stack);
        Calibration calibration = new Calibration();
        double pixelSize = resolution * CameraSetupPlugIn.getPixelSize() / 1000;
        calibration.pixelHeight = pixelSize;
        calibration.pixelWidth = pixelSize;
        if(threeDimensions) {
            calibration.pixelDepth = zStep / 1000;
        }
        calibration.setUnit("um");
        image.setCalibration(calibration);
        if(colorize) {
            image.setDimensions(zSlices, 1, 1);
            CompositeImage image2 = new CompositeImage(image);
            image = image2;
            setupLuts();
        }
    }

    @Override
    public void addToImage(double[] x, double[] y, double[] z, double[] dx, double[] dz) {
        for(int i = 0; i < x.length; i++) {
            double zVal = z != null ? z[i] : 0;
            double dxVal = dx != null && !forceDefaultDX ? dx[i] : defaultDX;
            double dzVal = dz != null && !forceDefaultDZ ? dz[i] : defaultDZ;
            drawPoint(x[i], y[i], zVal, dxVal, dzVal);
        }
    }

    @Override
    public void addToImage(List<Molecule> fits) {
        if(fits.isEmpty()) {
            return;
        }
        MoleculeDescriptor descriptor = fits.get(0).descriptor;
        boolean useDefaultDX = forceDefaultDX || !descriptor.hasParam(LABEL_UNCERTAINTY_XY);
        boolean useDefaultDZ = forceDefaultDZ || !descriptor.hasParam(LABEL_UNCERTAINTY_Z);

        for(int i = 0, im = fits.size(); i < im; i++) {
            Molecule fit = fits.get(i);
            double zVal = fit.getZ();
            double dxVal = useDefaultDX ? defaultDX : fit.getParam(LABEL_UNCERTAINTY_XY, PIXEL);
            double dzVal = useDefaultDZ ? defaultDZ : fit.getParam(LABEL_UNCERTAINTY_Z, NANOMETER);
            //
            drawPoint(fit.getX(PIXEL), fit.getY(PIXEL), zVal, dxVal, dzVal);
        }
    }

    @Override
    public ImagePlus getRenderedImage() {
        return image;
    }

    @Override
    public ImagePlus getRenderedImage(double[] x, double[] y, double[] z, double[] dx, double[] dz) {
        reset();
        addToImage(x, y, z, dx, dz);
        return getRenderedImage();
    }

    protected abstract void drawPoint(double x, double y, double z, double dx, double dz);

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
        setupLuts();
    }

    protected boolean isInBounds(double x, double y) {
        return x >= xmin && x < xmax && y >= ymin && y < ymax && !Double.isNaN(x) && !Double.isNaN(y);
    }

    private void setupLuts() {
        if(image.isComposite()) {
            CompositeImage image2 = (CompositeImage) image;
            LUT[] channeLuts = new LUT[zSlices];
            if (colorizationLut == null) {  // fallback if no LUTs are installed
                for (int i = 0; i < channeLuts.length; i++) {
                    //Colormap for slices: (has constant grayscale intensity, unlike jet and similar)
                    //r:      /
                    //     __/
                    //g:    /\
                    //     /  \
                    //b:   \
                    //      \__
                    float norm = (float) i / zSlices;
                    float r, g, b;
                    if (norm < 0.5) {
                        b = 1 - 2 * norm;
                        g = 2 * norm;
                        r = 0;
                    } else {
                        b = 0;
                        g = -2 * norm + 2;
                        r = 2 * norm - 1;
                    }
                    channeLuts[i] = LUT.createLutFromColor(new Color(r, g, b));
                }
            } else {
                int[] rgb = new int[4];
                for (int i = 0; i < channeLuts.length; i++) {
                    colorizationLut.getComponents((int)(((float) i / zSlices) * 255f), rgb, 0);
                    channeLuts[i] = LUT.createLutFromColor(new Color(rgb[0], rgb[1], rgb[2]));
                }
            }
            image2.setLuts(channeLuts);
        }
    }
}
