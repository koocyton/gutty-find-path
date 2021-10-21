package com.doopp.findroute.pojo;

import org.ksdev.jps.Node;

/**
 * @author Kevin
 */
public class FTile extends Node {

    private boolean straightPoint = false;

    public void setStraightPoint(boolean straightPoint) {
        this.straightPoint = straightPoint;
    }

    public boolean isStraightPoint() {
        return straightPoint;
    }

    public FTile(int x, int y) {
        super(x, y);
    }

    @Override
    public String toString() {
        return "\nFTile{" +
                "x=" + x +
                ",  y=" + y +
                ",  p=" + straightPoint +
                '}';
    }
}
