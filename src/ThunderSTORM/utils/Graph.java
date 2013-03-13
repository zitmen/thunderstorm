package ThunderSTORM.utils;

import static ThunderSTORM.utils.Math.mean;
import static ThunderSTORM.utils.Math.sum;
import java.util.LinkedList;
import java.util.Vector;

public class Graph {

    public static class ConnectedComponent {

        public Vector<Point> points = new Vector<>();

        public Point centroid() {
            int npts = points.size();
            double[] xarr = new double[npts];
            double[] yarr = new double[npts];
            double[] valarr = new double[npts];
            
            for(int i = 0, im = npts; i < im; i++) {
                Point p = points.elementAt(i);
                xarr[i] = p.getX().doubleValue();
                yarr[i] = p.getY().doubleValue();
                valarr[i] = p.getVal().doubleValue();
            }
            return new Point(mean(xarr), mean(yarr), sum(valarr));
        }
    }
    
    public static final int CONNECTIVITY_4 = 4;
    public static final int CONNECTIVITY_8 = 8;

    // TODO: very slow because of too many allocations!
    // TODO: pixel.val is now a component id! it should be real intensity of the pixel --> this may be solved by calling the method with different image and with `thr` parameter
    public static Vector<ConnectedComponent> getConnectedComponents(ij.process.ImageProcessor ip, int connectivity) {
        assert (ip != null);
        assert ((connectivity == CONNECTIVITY_4) || (connectivity == CONNECTIVITY_8));

        int[][] map = new int[ip.getWidth()][ip.getHeight()];
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                map[x][y] = 0; // member of no component
            }
        }

        Point<Integer> p;
        ConnectedComponent c;
        Vector<ConnectedComponent> components = new Vector<>();
        LinkedList<Point<Integer>> queue = new LinkedList<>();
        
        int counter = 0;
        boolean n, s, w, e;
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                if (map[x][y] > 0) continue; // already member of another component
                if (ip.getPixelValue(x, y) == 0.0f) continue;    // disabled pixel
                
                // new component
                counter++;
                queue.clear();
                queue.push(new Point<>(x, y));
                c = new ConnectedComponent();

                while (!queue.isEmpty()) {
                    p = queue.pop();
                    int px = p.getX().intValue();
                    int py = p.getY().intValue();
                    
                    if (map[px][py] > 0) continue; // already member of another component
                    if (ip.getPixelValue(px, py) == 0.0f) continue;    // disabled pixel
                    
                    map[px][py] = counter;
                    
                    c.points.add(new Point<>(p.getX().floatValue(), p.getY().floatValue(), ip.getPixelValue(px, py)));

                    w = (px > 0);  // west
                    n = (py > 0);  // north
                    e = (px < (map.length - 1));  // east
                    s = (py < (map[px].length - 1));  // south

                    if(w) queue.push(new Point<>(px - 1, py));  // west
                    if(n) queue.push(new Point<>(px, py - 1));  // north
                    if(e) queue.push(new Point<>(px + 1, py));  // east
                    if(s) queue.push(new Point<>(px, py + 1));  // south
                    
                    if(connectivity == CONNECTIVITY_8) {
                        if(n && w) queue.push(new Point<>(px - 1, py - 1));  // north west
                        if(n && e) queue.push(new Point<>(px + 1, py - 1));  // north east
                        if(s && w) queue.push(new Point<>(px - 1, py + 1));  // south west
                        if(s && e) queue.push(new Point<>(px + 1, py + 1));  // south east
                    }
                }
                
                components.add(c);
            }
        }

        return components;
    }
}
