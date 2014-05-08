package com.heybai.diploma;

import org.bytedeco.javacpp.opencv_core;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 * Created by heybai on 5/7/14.
 */
public class Frame {

    private IplImage img;

    public Frame(IplImage img) {
        this.img = img;
    }

    public IplImage img() {
        return img;
    }

    public Mat mat() {
        return new Mat(img);
    }
}
