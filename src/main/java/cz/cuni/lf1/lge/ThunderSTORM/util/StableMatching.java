/**
 * First version was taken from the package roboutils.planning
 * written by Prasanna Velagapudi <psigen@gmail.com>.
 * 
 * However there was an error in the algorithm which was fixed.
 * Since the error wasn't fixed in the original package, not even two months
 * after reporting the issue, it has been moved directly into the ThunderSTORM.
 * Then the algorithm was slightly changed for the purpose of ThunderSTORM.
 */
package cz.cuni.lf1.lge.ThunderSTORM.util;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Gale-Shapley algorithm
 */
public class StableMatching {

    public static <T extends IMatchable> Map<T, T> match(final List<T> suitors) {

        // Create a free list of suitors (and use it to store their proposals)
        Queue<MTuple<T>> freeSuitors = new LinkedList<MTuple<T>>();
        for (T suitor : suitors) {
            assert(suitor.getNeighbors() != null) : "Suitors must always have list of prefered reviewers (neighbors)!";
            @SuppressWarnings("unchecked") LinkedList<T> prefs = new LinkedList<T>(suitor.getNeighbors());
            Collections.sort(prefs, suitorPreference(suitor));
            freeSuitors.add(new MTuple<T>(suitor, prefs));
        }

        // Create an initially empty map of engagements
        Map<T, MTuple<T>> engagements = new HashMap<T, MTuple<T>>();

        // As per wikipedia algorithm
        while (!freeSuitors.isEmpty()) {
            
            // The next free suitor who has a reviewer to propose to
            MTuple<T> m = freeSuitors.peek();
            
            // m's highest ranked such woman who he has not proposed to yet
            T w = m.prefs.poll();
            if(w == null) {
                freeSuitors.poll();
                continue;
            }
            
            // Tf w is free
            if (!engagements.containsKey(w)) {
                // (m, w) become engaged
                engagements.put(w, m);
                freeSuitors.poll();
            } else {
                // Some pair (m', w) already exists
                MTuple<T> mPrime = engagements.get(w);
                if (reviewerPreference(w).compare(mPrime.suitor, m.suitor) < 0) {
                    // (m, w) become engaged
                    engagements.put(w, m);
                    freeSuitors.poll();
                    
                    // m' becomes free
                    freeSuitors.add(mPrime);
                }
            }

        }
        // Convert internal data structure to mapping
        HashMap<T, T> matches = new HashMap<T, T>();
        for (Map.Entry<T, MTuple<T>> entry : engagements.entrySet())
            matches.put(entry.getValue().suitor, entry.getKey());
        return matches;
    }

    private static class MTuple<T> {
        final T suitor;
        final Queue<T> prefs;

        public MTuple(T s, Queue<T> p) {
            suitor = s;
            prefs = p;
        }
    }

    private static <T extends IMatchable> Comparator<T> suitorPreference(final T suitor) {
        return new Comparator<T>() {
            @Override
            public int compare(T a, T b) {
                double diff = suitor.getDist2(a) - suitor.getDist2(b);
                if(diff < 0.0) return -1;
                if(diff > 0.0) return +1;
                return 0;
            }
        };
    }

    private static <T extends IMatchable> Comparator<T> reviewerPreference(final T reviewer) {
        return new Comparator<T>() {
            @Override
            public int compare(T a, T b) {
                double diff = reviewer.getDist2(a) - reviewer.getDist2(b);
                if(diff < 0.0) return -1;
                if(diff > 0.0) return +1;
                return 0;
            }
        };
    }
}