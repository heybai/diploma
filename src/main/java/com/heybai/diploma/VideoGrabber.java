package com.heybai.diploma;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heybai on 5/7/14.
 */
public class VideoGrabber {

    public Video grab(String videoPath) throws FrameGrabber.Exception {
        FrameGrabber grabber = OpenCVFrameGrabber.createDefault(videoPath);
        grabber.start();
        opencv_core.IplImage img;
        List<Frame> frames = new ArrayList<Frame>();
        while (true) {
            try {
                img = grabber.grab();
            } catch (Exception e) {
                break;
            }
            if (img == null) {
                break;
            } else {
                frames.add(new Frame(ImgUtils.copy(img)));
            }
        }
        grabber.stop();
        return new Video(frames);
    }

}
