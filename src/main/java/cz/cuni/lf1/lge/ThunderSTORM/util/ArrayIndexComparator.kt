package cz.cuni.lf1.lge.ThunderSTORM.util

import java.util.Comparator

class ArrayIndexComparator(private val mArray: DoubleArray) : Comparator<Int> {

    fun createIndexArray(): Array<Int> {
        return mArray.indices.map { i -> i }.toTypedArray()
    }

    override fun compare(index1: Int, index2: Int): Int {
        return Math.ceil(mArray[index1] - mArray[index2]).toInt()
    }
}
