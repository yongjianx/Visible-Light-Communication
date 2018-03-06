package ht.bean;

import android.support.annotation.NonNull;

/**
 * Created by dn on 2018/3/3.
 */

public class LedLine implements Comparable<LedLine> {
    private int mXMin;
    private int mXMax;

    public LedLine(int xMin, int xMax, int yMid) {
        mXMin = xMin;
        mXMax = xMax;
        mYMid = yMid;
    }

    public int getXMin() {
        return mXMin;
    }

    public void setXMin(int XMin) {
        mXMin = XMin;
    }

    public int getXMax() {
        return mXMax;
    }

    public void setXMax(int XMax) {
        mXMax = XMax;
    }

    public int getYMid() {
        return mYMid;
    }

    public void setYMid(int YMid) {
        mYMid = YMid;
    }

    private int mYMid;

    @Override
    public int compareTo(@NonNull LedLine o) {
        if ((this.mXMax - this.mXMin) > (o.mXMax - o.mXMin)) {
            return -1;
        } else if ((this.mXMax - this.mXMin) < (o.mXMax - o.mXMin)) {
            return 1;
        } else {
            return 0;
        }
    }
}
