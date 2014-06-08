package com.heybai.diploma.model;

import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_features2d.*;

import java.util.List;

/**
 * Created by heybai on 5/7/14.
 */
public class Frame {

    private int idx;

    private IplImage img;

    private boolean unique;

    // Features
    private KeyPoint keyPoints;
    private Mat descriptors;

    // Matches
    private List<Match> matches;        // not for last frame
    private List<Triple> triples;       // not for last two

    // Center
    private com.heybai.diploma.model.Point pipeCenter = new com.heybai.diploma.model.Point(329, 377);

    // Position
    // Distance to the next frame
    private double dz;                  // not for last frame
    private double z;

    public Frame(int idx, IplImage img) {
        this.idx = idx;
        this.img = img;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public int width() {
        return img.width();
    }

    public int height() {
        return img.height();
    }

    public IplImage img() {
        return img;
    }

    public Mat mat() {
        return new Mat(img);
    }

    public void setFeatures(KeyPoint keyPoints, Mat descriptors) {
        this.keyPoints = keyPoints;
        this.descriptors = descriptors;
    }

    public int nFeatures() {
        return keyPoints.capacity();
    }

    public KeyPoint getKeyPoints() {
        return keyPoints;
    }

    public Mat getDescriptors() {
        return descriptors;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public int nMatches() {
        return matches.size();
    }

    public List<Triple> getTriples() {
        return triples;
    }

    public void setTriples(List<Triple> triples) {
        this.triples = triples;
    }

    public int nTriples() {
        return triples.size();
    }

    public com.heybai.diploma.model.Point getPipeCenter() {
        return pipeCenter;
    }

    public void setPipeCenter(com.heybai.diploma.model.Point pipeCenter) {
        this.pipeCenter = pipeCenter;
    }

    public double getDz() {
        return dz;
    }

    public void setDz(double dz) {
        this.dz = dz;
    }
}
