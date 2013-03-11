package Watershed;

import ij.process.ImageProcessor;
import java.util.Collections;
import java.util.Vector;

/*
 * Watershed algorithm
 *
 * Copyright (c) 2003 by Christopher Mei (christopher.mei@sophia.inria.fr)
 *
 * This plugin is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this plugin; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/**
 * WatershedStructure contains the pixels of the image ordered according to
 * their grayscale value with a direct access to their neighbours.
 *
 *
 */
public class WatershedStructure {

    private Vector watershedStructure;

    public WatershedStructure(ImageProcessor ip) {
        byte[] pixels = (byte[]) ip.getPixels();
        int width = ip.getWidth(), height = ip.getHeight();
        int offset, topOffset, bottomOffset, i;

        watershedStructure = new Vector(width * height);

        /**
         * The structure is filled with the pixels of the image. *
         */
        for (int y = 0; y < height; y++) {
            offset = y * width;

            for (int x = 0; x < width; x++) {
                i = offset + x;

                watershedStructure.add(new WatershedPixel(x, y, pixels[i]));
            }
        }

        /**
         * The WatershedPixels are then filled with the reference to their
         * neighbours. *
         */
        for (int y = 0; y < height; y++) {

            offset = y * width;
            topOffset = offset + width;
            bottomOffset = offset - width;

            for (int x = 0; x < width; x++) {
                WatershedPixel currentPixel = (WatershedPixel) watershedStructure.get(x + offset);

                if (x + 1 < width) {
                    currentPixel.addNeighbour((WatershedPixel) watershedStructure.get(x + 1 + offset));

                    if (y - 1 >= 0) {
                        currentPixel.addNeighbour((WatershedPixel) watershedStructure.get(x + 1 + bottomOffset));
                    }

                    if (y + 1 < height) {
                        currentPixel.addNeighbour((WatershedPixel) watershedStructure.get(x + 1 + topOffset));
                    }
                }

                if (x - 1 >= 0) {
                    currentPixel.addNeighbour((WatershedPixel) watershedStructure.get(x - 1 + offset));

                    if (y - 1 >= 0) {
                        currentPixel.addNeighbour((WatershedPixel) watershedStructure.get(x - 1 + bottomOffset));
                    }

                    if (y + 1 < height) {
                        currentPixel.addNeighbour((WatershedPixel) watershedStructure.get(x - 1 + topOffset));
                    }
                }

                if (y - 1 >= 0) {
                    currentPixel.addNeighbour((WatershedPixel) watershedStructure.get(x + bottomOffset));
                }

                if (y + 1 < height) {
                    currentPixel.addNeighbour((WatershedPixel) watershedStructure.get(x + topOffset));
                }
            }
        }

        Collections.sort(watershedStructure);
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();

        for (int i = 0; i < watershedStructure.size(); i++) {
            ret.append(((WatershedPixel) watershedStructure.get(i)).toString());
            ret.append("\n");
            ret.append("Neighbours :\n");

            Vector neighbours = ((WatershedPixel) watershedStructure.get(i)).getNeighbours();

            for (int j = 0; j < neighbours.size(); j++) {
                ret.append(((WatershedPixel) neighbours.get(j)).toString());
                ret.append("\n");
            }
            ret.append("\n");
        }
        return ret.toString();
    }

    public int size() {
        return watershedStructure.size();
    }

    public WatershedPixel get(int i) {
        return (WatershedPixel) watershedStructure.get(i);
    }
}
