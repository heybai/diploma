package com.heybai.diploma.utils;

import com.heybai.diploma.model.*;
import com.heybai.diploma.model.Point;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 * Created by heybai on 5/7/14.
 */
public class ImgUtils {

    public static IplImage copy(IplImage src) {
        IplImage dst = IplImage.create(src.width(), src.height(), src.depth(), src.nChannels());
        cvCopy(src, dst);
        return dst;
    }

    public static Frame copy(Frame src) {
        return new Frame(src.getIdx(), copy(src.img()));
    }

    public static Frame emptyLike(Frame frame) {
        IplImage src = frame.img();
        IplImage dst = IplImage.create(cvGetSize(src), src.depth(), src.nChannels());
        return new Frame(frame.getIdx(), dst);
    }

    public static com.heybai.diploma.model.Point center(Video video) {
        IplImage img = video.get(0).img();
        return new Point(img.width() / 2, img.height() / 2);
    }

    public static float radius(Frame frame) {
        IplImage img = frame.img();
        return (float) Math.min(img.width(), img.height()) / 2;
    }

    public static CvPoint point(Point p) {
        return cvPoint(Math.round(p.getX()), Math.round(p.getY()));
    }

}
