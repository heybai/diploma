package com.heybai.diploma;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_nonfree;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_features2d.*;
import static org.bytedeco.javacpp.opencv_nonfree.*;

/**
 * Created by heybai on 5/7/14.
 */
public class FeaturesExtractor {

    static { Loader.load(opencv_nonfree.class); }

    public List<Features> extract(Video video) {
        return extract(video, new SiftConfig());
    }

    public List<Features> extract(Video video, SiftConfig sc) {
        List<Features> featureses = new ArrayList<Features>();
        for (Frame frame : video.getFrames()) {
            featureses.add(extract(video.getFrames().get(0)));
        }
        return featureses;
    }

    public Features extract(Frame frame) {
        return extract(frame, new SiftConfig());
    }

    public Features extract(Frame frame, SiftConfig sc) {
        SIFT sift = new SIFT(0, 3, 0.04, 10, 1.6);

        KeyPoint keypoints = new KeyPoint();
        Mat descriptors = new Mat();
        sift.detect(frame.getAsMat(), keypoints);
        sift.compute(frame.getAsMat(), keypoints, descriptors);

        return new Features(keypoints, descriptors);
    }

    public static class SiftConfig {
        public int nfeatures = 0;
        public int nOctaveLayers = 3;
        public double contrastThreshold = 0.04;
        public double edgeThreshold = 10;
        public double sigma = 1.6;

        public SiftConfig() {
        }

        public SiftConfig(int nfeatures, int nOctaveLayers, double contrastThreshold, double edgeThreshold, double sigma) {
            this.nfeatures = nfeatures;
            this.nOctaveLayers = nOctaveLayers;
            this.contrastThreshold = contrastThreshold;
            this.edgeThreshold = edgeThreshold;
            this.sigma = sigma;
        }
    }

}
