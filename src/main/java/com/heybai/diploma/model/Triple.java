package com.heybai.diploma.model;

import com.heybai.diploma.model.Point;

/**
 * Created by heybai on 5/12/14.
 */
public class Triple {

    private Point p1;
    private Point p2;
    private Point p3;

    public Triple(Point p1, Point p2, Point p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    public Point getP1() {
        return p1;
    }

    public Point getP2() {
        return p2;
    }

    public Point getP3() {
        return p3;
    }
}
