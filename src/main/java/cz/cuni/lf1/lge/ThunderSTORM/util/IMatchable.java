package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.util.List;

public interface IMatchable<T extends IMatchable<T>> {

    double getX();
    double getY();
    double getZ();

    void setX(double x);
    void setY(double y);
    void setZ(double z);

    double getDist2(IMatchable m);

    List<T> getNeighbors();

    /**
     * Note that `neighbors` are ignored by the cloning operation!
     */
    T clone();
}
