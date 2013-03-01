package Watershed;

/*
 * Watershed plugin
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

import java.util.*;
import ij.*;

/** This class implements a FIFO queue that
 *  uses the same formalism as the Vincent
 *  and Soille algorithm (1991)
 **/

public class WatershedFIFO {
    private LinkedList watershedFIFO;

    public WatershedFIFO() {
	watershedFIFO = new LinkedList();
    }

    public void fifo_add(WatershedPixel p) {
	watershedFIFO.addFirst(p);
    }

    public WatershedPixel fifo_remove() {
	return (WatershedPixel) watershedFIFO.removeLast();
    }

    public boolean fifo_empty() {
	return watershedFIFO.isEmpty();
    }

    public void fifo_add_FICTITIOUS() {
	watershedFIFO.addFirst(new WatershedPixel());
    }

    public String toString() {
	StringBuffer ret = new StringBuffer();
	for(int i=0; i<watershedFIFO.size(); i++) {
	    ret.append( ((WatershedPixel)watershedFIFO.get(i)).toString() );
	    ret.append( "\n" );
	}
	
	return ret.toString();
    }
}
