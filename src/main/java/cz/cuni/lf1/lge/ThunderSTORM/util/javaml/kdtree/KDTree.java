/**
 * This file is part of the Java Machine Learning Library
 * 
 * The Java Machine Learning Library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * The Java Machine Learning Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Java Machine Learning Library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Copyright (c) 2006-2009, Thomas Abeel
 * 
 * Project: http://java-ml.sourceforge.net/
 * 
 * 
 * based on work by Simon Levy
 * http://www.cs.wlu.edu/~levy/software/kd/
 */
package cz.cuni.lf1.lge.ThunderSTORM.util.javaml.kdtree;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

 /**
  * KDTree is a class supporting KD-tree insertion, deletion, equality
  * search, range search, and nearest neighbor(s) using double-precision
  * floating-point keys.  Splitting dimension is chosen naively, by
  * depth modulo K.  Semantics are as follows:
  *
  * <UL>
  * <LI> Two different keys containing identical numbers should retrieve the 
  *      same value from a given KD-tree.  Therefore keys are cloned when a 
  *      node is inserted.
  * <BR><BR>
  * <LI> As with Hashtables, values inserted into a KD-tree are <I>not</I>
  *      cloned.  Modifying a value between insertion and retrieval will
  *      therefore modify the value stored in the tree.
  *</UL>
  *
  * @author      Simon Levy, Bjoern Heckel
  * @version     %I%, %G%
  * @since JDK1.2 
  */
public class KDTree <T> {

    // K = number of dimensions
    private int m_K;

    // root of KD-tree
    private KDNode<T> m_root;

    // count of nodes
    private int m_count;
    
    /**
     * Creates a KD-tree with specified number of dimensions.
     *
     * @param k number of dimensions
     */
    public KDTree(int k) {

	m_K = k;
	m_root = null;
    }


   /** 
    * Insert a node in a KD-tree.  Uses algorithm translated from 352.ins.c of
    *
    *   <PRE>
    *   &#064;Book{GonnetBaezaYates1991,                                   
    *     author =    {G.H. Gonnet and R. Baeza-Yates},
    *     title =     {Handbook of Algorithms and Data Structures},
    *     publisher = {Addison-Wesley},
    *     year =      {1991}
    *   }
    *   </PRE>
    *
    * @param key key for KD-tree node
    * @param value value at that key
    *
    * @throws KeySizeException if key.length mismatches K
    * @throws KeyDuplicateException if key already in tree
    */
    public void insert(double [] key, T value) 
	throws KeySizeException, KeyDuplicateException {

	if (key.length != m_K) {
	    throw new KeySizeException();
	}

	else try {
	    m_root = KDNode.<T>ins(new HPoint(key), value, m_root, 0, m_K);
	}

	catch (KeyDuplicateException e) {
	    throw e;
	}

	m_count++;
    }

   /** 
    * Find  KD-tree node whose key is identical to key.  Uses algorithm 
    * translated from 352.srch.c of Gonnet & Baeza-Yates.
    *
    * @param key key for KD-tree node
    *
    * @return object at key, or null if not found
    *
    * @throws KeySizeException if key.length mismatches K
    */
    public T search(double [] key) throws KeySizeException {

	if (key.length != m_K) {
	    throw new KeySizeException();
	}

	KDNode<T> kd = KDNode.srch(new HPoint(key), m_root, m_K);

	return (kd == null ? null : kd.v);
    }


   /** 
    * Delete a node from a KD-tree.  Instead of actually deleting node and
    * rebuilding tree, marks node as deleted.  Hence, it is up to the caller
    * to rebuild the tree as needed for efficiency.
    *
    * @param key key for KD-tree node
    *
    * @throws KeySizeException if key.length mismatches K
    * @throws KeyMissingException if no node in tree has key
    */
    public void delete(double [] key) 
	throws KeySizeException, KeyMissingException {

	if (key.length != m_K) {
	    throw new KeySizeException();
	}

	else {

	    KDNode<T> t = KDNode.srch(new HPoint(key), m_root, m_K);
	    if (t == null) {
		throw new KeyMissingException();
	    }
	    else {
		t.deleted = true;
	    }

	    m_count--;
	}
    }

    /**
    * Find KD-tree node whose key is nearest neighbor to
    * key. Implements the Nearest Neighbor algorithm (Table 6.4) of
    *
    * <PRE>
    * &#064;techreport{AndrewMooreNearestNeighbor,
    *   author  = {Andrew Moore},
    *   title   = {An introductory tutorial on kd-trees},
    *   institution = {Robotics Institute, Carnegie Mellon University},
    *   year    = {1991},
    *   number  = {Technical Report No. 209, Computer Laboratory, 
    *              University of Cambridge},
    *   address = {Pittsburgh, PA}
    * }
    * </PRE>
    *
    * @param key key for KD-tree node
    *
    * @return object at node nearest to key, or null on failure
    *
    * @throws KeySizeException if key.length mismatches K

    */
    public T nearest(double [] key) throws KeySizeException {
	
	return nearest(key, 1).get(0);
    }

    /**
    * Find KD-tree nodes whose keys are <I>n</I> nearest neighbors to
    * key. Uses algorithm above.  Neighbors are returned in ascending
    * order of distance to key. 
    *
    * @param key key for KD-tree node
    * @param n how many neighbors to find
    *
    * @return objects at node nearest to key, or null on failure
    *
    * @throws KeySizeException if key.length mismatches K
    * @throws IllegalArgumentException if <I>n</I> is negative or
    * exceeds tree size 
    */
    public List<T> nearest(double [] key, int n) 
	throws KeySizeException, IllegalArgumentException {

	if (n < 0 || n > m_count) {
	    throw new IllegalArgumentException("Number of neighbors cannot" +
            " be negative or greater than number of nodes");
	}

	if (key.length != m_K) {
	    throw new KeySizeException();
	}

	LinkedList<T> nbrs = new LinkedList<T>();
	NearestNeighborList nnl = new NearestNeighborList(n);

	// initial call is with infinite hyper-rectangle and max distance
	HRect hr = HRect.infiniteHRect(key.length);
	double max_dist_sqd = Double.MAX_VALUE;
	HPoint keyp = new HPoint(key);

	KDNode.<T>nnbr(m_root, keyp, hr, max_dist_sqd, 0, m_K, nnl);

	for (int i=0; i<n; ++i) {
	    KDNode<T> kd = (KDNode<T>)nnl.removeHighest();
	    nbrs.addFirst(kd.v);
	}

	return nbrs;
    }


   /** 
    * Range search in a KD-tree.  Uses algorithm translated from
    * 352.range.c of Gonnet & Baeza-Yates.
    *
    * @param lowk lower-bounds for key
    * @param uppk upper-bounds for key
    *
    * @return array of Objects whose keys fall in range [lowk,uppk]
    *
    * @throws KeySizeException on mismatch among lowk.length, uppk.length, or K
    */
    public List<T> range(double [] lowk, double [] uppk) 
	throws KeySizeException {

	if (lowk.length != uppk.length) {
	    throw new KeySizeException();
	}

	else if (lowk.length != m_K) {
	    throw new KeySizeException();
	}

	else {
	    Vector<KDNode<T>> v = new Vector<KDNode<T>>();
	    KDNode.<T>rsearch(new HPoint(lowk), new HPoint(uppk), 
			   m_root, 0, m_K, v);
	    ArrayList<T> o = new ArrayList<T>();
	    for (int i=0; i<v.size(); ++i) {
		KDNode<T> n = v.elementAt(i);
		o.add(n.v);
	    }
	    return o;
	}
    }

    public String toString() {
	return m_root.toString(0);
    }
    
    /**
     * Ball query in a kdtree. Returns all points that are closer(based on euclidean distance) than maxDistance to the queryPoint.
     * @param queryPoint
     * @param maxDistance
     * @return 
     * @throws KeySizeException 
     */
    public List<DistAndValue<T>> ballQuery(double[] queryPoint, double maxDistance) throws KeySizeException {
        if(queryPoint.length != m_K){
            throw new KeySizeException();
        }
        
        double [] lower = new double[queryPoint.length];
        double [] upper = new double[queryPoint.length];
        for(int i = 0; i< queryPoint.length; i++){
            lower[i] = queryPoint[i]-maxDistance;
            upper[i] = queryPoint[i]+maxDistance;
        }
        
        HPoint wrappedQueryPoint = new HPoint(queryPoint);
        double squaredMaxDistance = maxDistance*maxDistance;
        
	    Vector<KDNode<T>> v = new Vector<KDNode<T>>();
        KDNode.<T>rsearch(new HPoint(lower), new HPoint(upper),
                m_root, 0, m_K, v);
        ArrayList<DistAndValue<T>> retVal = new ArrayList<DistAndValue<T>>();
        for(int i = 0; i < v.size(); ++i) {
            KDNode<T> n = v.elementAt(i);
            double sqrdist = HPoint.sqrdist(n.k, wrappedQueryPoint);
            if(sqrdist < squaredMaxDistance){
                retVal.add(new DistAndValue<T>(Math.sqrt(sqrdist), n.v));
            }
        }
        return retVal;
    }
    
    static public class DistAndValue<T>{
        public double dist;
        public T value;

        public DistAndValue(double dist, T value) {
            this.dist = dist;
            this.value = value;
        }
    }
}
