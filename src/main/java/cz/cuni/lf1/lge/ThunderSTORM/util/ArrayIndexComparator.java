package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.util.Comparator;

// --------------------------------------- //
// From http://stackoverflow.com/a/4859279 //
// --------------------------------------- //
public class ArrayIndexComparator implements Comparator<Integer> {
    private final double[] array;

    public ArrayIndexComparator(double[] array) {
        this.array = array;
    }

    public Integer[] createIndexArray()
    {
        Integer[] indexes = new Integer[array.length];
        for (int i = 0; i < array.length; i++)
            indexes[i] = i;
        return indexes;
    }

    @Override
    public int compare(Integer index1, Integer index2) {
        return (int)Math.ceil(array[index1] - array[index2]);
    }
}
