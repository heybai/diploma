package com.heybai.diploma;

import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_features2d.*;

/**
 * Created by heybai on 5/7/14.
 */
public class Features {

    private KeyPoint keyPoints;
    private Mat descriptors;

    public Features(KeyPoint keyPoints, Mat descriptors) {
        this.keyPoints = keyPoints;
        this.descriptors = descriptors;
    }
}
