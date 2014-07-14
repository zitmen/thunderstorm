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

    public static Map match(final List<Molecule> suitors) {

        // Create a free list of suitors (and use it to store their proposals)
        Queue<MTuple> freeSuitors = new LinkedList();
        for (Molecule suitor : suitors) {
            assert(suitor.neighbors != null) : "Suitors must always have list of prefered reviewers (neighbors)!";
            LinkedList<Molecule> prefs = new LinkedList(suitor.neighbors);
            Collections.sort(prefs, suitorPreference(suitor));
            freeSuitors.add(new MTuple(suitor, prefs));
        }

        // Create an initially empty map of engagements
        Map<Molecule, MTuple> engagements = new HashMap();

        // As per wikipedia algorithm
        while (!freeSuitors.isEmpty()) {
            
            // The next free suitor who has a reviewer to propose to
            MTuple m = freeSuitors.peek();
            
            // m's highest ranked such woman who he has not proposed to yet
            Molecule w = m.prefs.poll();
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
                MTuple mPrime = engagements.get(w);
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
        HashMap<Molecule,Molecule> matches = new HashMap();
        for (Map.Entry<Molecule, MTuple> entry : engagements.entrySet())
            matches.put(entry.getKey(), entry.getValue().suitor);
        return matches;
    }

    private static class MTuple {
        final Molecule suitor;
        final Queue<Molecule> prefs;

        public MTuple(Molecule s, Queue<Molecule> p) {
            suitor = s;
            prefs = p;
        }
    }

    private static Comparator<Molecule> suitorPreference(final Molecule suitor) {
        return new Comparator<Molecule>() {
            @Override
            public int compare(Molecule a, Molecule b) {
                double diff = suitor.getDist2(a) - suitor.getDist2(b);
                if(diff < 0.0) return -1;
                if(diff > 0.0) return +1;
                return 0;
            }
        };
    }

    private static Comparator<Molecule> reviewerPreference(final Molecule reviewer) {
        return new Comparator<Molecule>() {
            @Override
            public int compare(Molecule a, Molecule b) {
                double diff = reviewer.getDist2(a) - reviewer.getDist2(b);
                if(diff < 0.0) return -1;
                if(diff > 0.0) return +1;
                return 0;
            }
        };
    }
}