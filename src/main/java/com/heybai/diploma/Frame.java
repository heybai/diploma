package com.heybai.diploma;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 * Created by heybai on 5/7/14.
 */
public class Frame {

    private IplImage img;

    public Frame(IplImage img) {
        this.img = img;
    }
}
