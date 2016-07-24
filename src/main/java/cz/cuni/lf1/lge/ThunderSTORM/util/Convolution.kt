package cz.cuni.lf1.lge.ThunderSTORM.util;

import ij.process.FloatProcessor;

/**
 * Convolution algorithm.
 */
object Convolution {

    /**
     * 2D convolution of an input image with a specified kernel.
     *
     * A two dimensional convolution: {
     *
     * @mathjax (image*kernel)(s,t) =
     * \int_{-\infty}^{+\infty}{\int_{-\infty}^{+\infty}{image(x,y) \cdot
     * kernel(s-x,t-y) \; \mathrm{d}x\mathrm{d}y}}}
     *
     * @param image an input image
     * @param kernel convolution kernel
     * @param paddingType one of the padding types
     * ({@code PADDING_NONE}, {@code PADDING_ZERO}, {@code PADDING_DUPLICATE},
     * or {@code PADDING_CYCLIC})
     * @return a <strong>new instance</strong> of FloatProcessor that contains
     * the convolved image
     */
    fun convolve2D(image: FloatProcessor, kernel: FloatProcessor, paddingType: Padding): FloatProcessor {

        val kw = kernel.width
        val kh = kernel.height
        val padsize = MathProxy.ceil(java.lang.Math.max(kw, kh) / 2.0).toInt()
        val iw = image.width
        val ih = image.height
        //if(paddingType == Padding.PADDING_NONE) { iw -= 2*padsize; ih -= 2*padsize; }
        val img = paddingType.addBorder(image, padsize)

        // convolution
        val result = FloatArray(iw * ih)

        val kernelPixels = kernel.pixels as FloatArray
        val width = img.width
        val height = img.height
        val x1 = padsize
        val y1 = padsize
        val x2 = width - padsize
        val y2 = height - padsize
        val uc = kw / 2
        val vc = kh / 2
        val pixels = result
        val pixels2 = img.pixels as FloatArray
        var sum: Double
        var offset: Int
        var i: Int
        var idx = if (paddingType == Padding.PADDING_NONE) iw * padsize + x1 else 0
        for(y in y1..(y2-1)) {
            for(x in x1..(x2-1)) {
                sum = 0.0
                i = 0
                for(v in -vc..(kh-vc-1)) {
                    offset = x + (y - v) * width
                    for(u in -uc..(kw-uc-1)) {
                        sum += pixels2[offset - u] * kernelPixels[i++]
                    }
                }
                pixels[idx++] = sum.toFloat()
            }
            if(paddingType == Padding.PADDING_NONE){
                idx += 2*padsize
            }
        }

        return FloatProcessor(iw, ih, pixels, null)
    }
}
