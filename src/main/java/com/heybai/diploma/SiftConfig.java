package com.heybai.diploma;

/**
 * Created by heybai on 5/8/14.
 */
public class SiftConfig {
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
