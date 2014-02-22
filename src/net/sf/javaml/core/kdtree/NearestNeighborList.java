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
package net.sf.javaml.core.kdtree;

// Bjoern Heckel's solution to the KD-Tree n-nearest-neighbor problem

class NearestNeighborList {

   public static int REMOVE_HIGHEST = 1;
   public static int REMOVE_LOWEST = 2;

   PriorityQueue m_Queue = null;
   int m_Capacity = 0;

   // constructor
   public NearestNeighborList(int capacity) {
       m_Capacity = capacity;
       m_Queue = new PriorityQueue(m_Capacity,Double.POSITIVE_INFINITY);
   }

   public double getMaxPriority() {
       if (m_Queue.length()==0) {
           return Double.POSITIVE_INFINITY;
       }
       return m_Queue.getMaxPriority();
   }

   public boolean insert(Object object,double priority) {
       if (m_Queue.length()<m_Capacity) {
           // capacity not reached
           m_Queue.add(object,priority);
           return true;
       }
       if (priority>m_Queue.getMaxPriority()) {
           // do not insert - all elements in queue have lower priority
           return false;
       }
       // remove object with highest priority
       m_Queue.remove();
       // add new object
       m_Queue.add(object,priority);
       return true;
   }

   public boolean isCapacityReached() {
       return m_Queue.length()>=m_Capacity;
   }

   public Object getHighest() {
       return m_Queue.front();
   }

   public boolean isEmpty() {
       return m_Queue.length()==0;
   }

   public int getSize() {
       return m_Queue.length();
   }

   public Object removeHighest() {
       // remove object with highest priority
       return m_Queue.remove();
   }
}
