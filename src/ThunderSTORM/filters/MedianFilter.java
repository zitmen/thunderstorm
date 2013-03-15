package ThunderSTORM.filters;

import ij.process.FloatProcessor;

public class MedianFilter implements IFilter {

    public static final int CROSS = 4;
    public static final int BOX = 8;
    private int pattern;
    private int size;

    public MedianFilter(int pattern, int size) {
        assert ((pattern == BOX) || (pattern == CROSS));

        this.pattern = pattern;
        this.size = size;
    }

    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        FloatProcessor result = new FloatProcessor(image.getWidth(), image.getHeight());
        if (pattern == BOX) {
            float [] items = new float[size*size];
            for (int x = 0, xm = image.getWidth(); x < xm; x++) {
                for (int y = 0, ym = image.getHeight(); y < ym; y++) {
                    int ii = 0;
                    for(int i = x - size/2, im = i + size; i < im; i++) {
                        for(int j = x - size/2, jm = j + size; j < jm; j++) {
                            if((i >= 0) && (i < xm) && (j >= 0) && (j < ym)) {
                                items[ii] = image.getPixelValue(i, j);
                                ii++;
                            }
                        }
                    }
                    result.setf(x, y, items[ii/2]);
                }
            }
        } else {
            float [] items = new float[2*size];
            for (int x = 0, xm = image.getWidth(); x < xm; x++) {
                for (int y = 0, ym = image.getHeight(); y < ym; y++) {
                    int ii = 0;
                    for(int i = x - size/2, im = i + size; i < im; i++) {
                        if((i >= 0) && (i < xm)) {
                            items[ii] = image.getPixelValue(i, y);
                            ii++;
                        }
                    }
                    for(int j = y - size/2, jm = j + size; j < jm; j++) {
                        if((j >= 0) && (j < ym)) {
                            items[ii] = image.getPixelValue(x, j);
                            ii++;
                        }
                    }
                    result.setf(x, y, items[ii/2]);
                }
            }
        }
        return result;
    }
}
