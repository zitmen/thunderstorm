
package ags.utils.dataStructures.trees.thirdGenKD;

import ags.utils.dataStructures.MaxHeap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

public class KdTreeTest {
    
    
    @Test
    public void testBallQuery() {
        
        List<CoordsAndString> points = new ArrayList<CoordsAndString>();
        KdTree<String> tree = new KdTree<String>(2);
        for(int i = 0; i < 1000; i++){
            double x = Math.random()*10;
            double y = Math.random()*10;
            double[] point = new double[]{x,y};
            String str = i + "";
            points.add(new CoordsAndString(str, point));
            tree.addPoint(point, str);
        }
        SquareEuclideanDistanceFunction distFun = new SquareEuclideanDistanceFunction();
        double maxDist = 2*2;
        double[] queryPoint = new double[]{5,5};
        
        //remove points that are further than maxDist
        for(Iterator<CoordsAndString> it = points.iterator();it.hasNext(); ){
            CoordsAndString val = it.next();
            if(distFun.distance(val.coords, queryPoint) > maxDist){
                it.remove();
            }
        }
        
        List<KdTree.DistAndValue<String>> queryResults = tree.ballQuery(queryPoint, maxDist, distFun);
        
        //check that the kdtree query result is the same as the above 
        while(queryResults.size() > 0){
            KdTree.DistAndValue<String> val = queryResults.remove(queryResults.size()-1);
            assertTrue(val.dist <= maxDist);
            CoordsAndString c = new CoordsAndString(val.value,null);
            assertTrue(points.contains(c));
            points.remove(c);
        }
        assertTrue(points.isEmpty());
    }
    
    class CoordsAndString{
        String str;
        double[] coords;

        public CoordsAndString(String str, double[] coords) {
            this.str = str;
            this.coords = coords;
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof CoordsAndString)){
                return false;
            }
            return str.equals(((CoordsAndString)obj).str);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 17 * hash + (this.str != null ? this.str.hashCode() : 0);
            return hash;
        }
    }

    
}
