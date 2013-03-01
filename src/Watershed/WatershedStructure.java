package Watershed;

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

import java.lang.*;
import java.util.*;
import ij.process.*;
import ij.*;
import java.awt.*;

/**
 *  WatershedStructure contains the pixels
 *  of the image ordered according to their
 *  grayscale value with a direct access to their
 *  neighbours.
 *  
 **/

public class WatershedStructure {
    private Vector watershedStructure;

    public WatershedStructure(ImageProcessor ip) {
	byte[] pixels = (byte[])ip.getPixels();
	Rectangle r = ip.getRoi();
	int width = ip.getWidth();
	int offset, topOffset, bottomOffset, i;

	watershedStructure = new Vector(r.width*r.height);

	/** The structure is filled with the pixels of the image. **/
	for (int y=r.y; y<(r.y+r.height); y++) {
	    offset = y*width;
	    
	    IJ.showProgress(0.1+0.3*(y-r.y)/(r.height));

	    for (int x=r.x; x<(r.x+r.width); x++) {
		i = offset + x;

		int indiceY = y-r.y;
		int indiceX = x-r.x;
		
		watershedStructure.add(new WatershedPixel(indiceX, indiceY, pixels[i]));
	    }
	}

	/** The WatershedPixels are then filled with the reference to their neighbours. **/
	for (int y=0; y<r.height; y++) {

	    offset = y*width;
	    topOffset = offset+width;
	    bottomOffset = offset-width;
	    
	    IJ.showProgress(0.4+0.3*(y-r.y)/(r.height));

	    for (int x=0; x<r.width; x++) {		
		WatershedPixel currentPixel = (WatershedPixel)watershedStructure.get(x+offset);

		if(x+1<r.width) {
		    currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x+1+offset));

		    if(y-1>=0)
			currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x+1+bottomOffset));
		
		    if(y+1<r.height)
			currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x+1+topOffset));
		}

		if(x-1>=0) {
		    currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x-1+offset));
		
		    if(y-1>=0)
			currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x-1+bottomOffset));
		
		    if(y+1<r.height)
			currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x-1+topOffset));
		}
		
		if(y-1>=0)
		    currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x+bottomOffset));
		
		if(y+1<r.height)
		    currentPixel.addNeighbour((WatershedPixel)watershedStructure.get(x+topOffset));
	    }
	} 

	Collections.sort(watershedStructure);
	//IJ.showProgress(0.8);
    }

    public String toString() {
	StringBuffer ret = new StringBuffer();

	for(int i=0; i<watershedStructure.size() ; i++) {
	    ret.append( ((WatershedPixel) watershedStructure.get(i)).toString() );
	    ret.append( "\n" );
	    ret.append( "Neighbours :\n" );

	    Vector neighbours = ((WatershedPixel) watershedStructure.get(i)).getNeighbours();

	    for(int j=0 ; j<neighbours.size() ; j++) {
		ret.append( ((WatershedPixel) neighbours.get(j)).toString() );
		ret.append( "\n" );
	    }
	    ret.append( "\n" );
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
