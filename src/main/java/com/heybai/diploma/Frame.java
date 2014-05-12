package com.heybai.diploma;

import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_features2d.*;

import java.util.List;

/**
 * Created by heybai on 5/7/14.
 */
public class Frame {

    private IplImage img;

    // Features
    private KeyPoint keyPoints;
    private Mat descriptors;

    // Matches
    private List<Match> matches;        // not for last frame
    private List<Triple> triples;       // not for last two

    // Center
    private Point pipeCenter = new Point(329, 377);

    public Frame(IplImage img) {
        this.img = img;
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

    public Point getPipeCenter() {
        return pipeCenter;
    }

    public void setPipeCenter(Point pipeCenter) {
        this.pipeCenter = pipeCenter;
    }
}
