package ht.bean;

/**
 * Created by dn on 2018/1/23.
 */

public class Coordinate {
    private int mMin;
    private int mMax;
    private int mMiddle;

    public Coordinate(int min, int max, int middle) {
        mMin = min;
        mMax = max;
        mMiddle = middle;
    }

    public int getMin() {
        return mMin;
    }

    public void setMin(int min) {
        mMin = min;
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int max) {
        mMax = max;
    }

    public int getMiddle() {
        return mMiddle;
    }

    public void setMiddle(int middle) {
        mMiddle = middle;
    }
}
