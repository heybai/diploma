package com.heybai.diploma.model;

import com.heybai.diploma.model.Point;

/**
 * Created by heybai on 5/7/14.
 */
public class Match {

    private Point p1;
    private Point p2;

    public Match(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Point getP1() {
        return p1;
    }

    public Point getP2() {
        return p2;
    }
}
