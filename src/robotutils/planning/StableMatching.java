/*
 *  The MIT License
 *
 *  Copyright 2010 Prasanna Velagapudi <psigen@gmail.com>.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package robotutils.planning;

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

    public Map match(final List<Molecule> suitors, final List<Molecule> reviewers) {

        // Create a free list of suitors (and use it to store their proposals)
        Queue<MTuple> freeSuitors = new LinkedList();
        for (Molecule suitor : suitors) {
            assert(suitor.neighbors != null) : "Suitors must always have list of prefered reviewers!";
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

    private class MTuple {
        final Molecule suitor;
        final Queue<Molecule> prefs;

        public MTuple(Molecule s, Queue<Molecule> p) {
            suitor = s;
            prefs = p;
        }
    }

    public Comparator<Molecule> suitorPreference(final Molecule suitor) {
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

    public Comparator<Molecule> reviewerPreference(final Molecule reviewer) {
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