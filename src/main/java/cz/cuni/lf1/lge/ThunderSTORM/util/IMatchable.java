package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.util.List;

public interface IMatchable<T extends IMatchable<T>> {

    double getX();
    double getY();
    double getZ();

    double getDist2(IMatchable m);

    List<T> getNeighbors();
}
