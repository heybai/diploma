package com.heybai.diploma.view;

import com.heybai.diploma.model.Frame;
import org.bytedeco.javacv.CanvasFrame;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 * Created by heybai on 5/7/14.
 */
public class Canvas {

    public static void img(IplImage img) throws InterruptedException {
        CanvasFrame cf1 = new CanvasFrame("Default");
        cf1.showImage(img);
        cf1.waitKey();
        cf1.dispose();
    }

    public static void frame(Frame frame, String name) throws InterruptedException {
        CanvasFrame cf1 = new CanvasFrame(name);
        cf1.showImage(frame.img());
        cf1.waitKey();
        cf1.dispose();
    }

    public static void frames(Frame f1, String n1, Frame f2, String n2) throws InterruptedException {
        CanvasFrame cf1 = new CanvasFrame(n1);
        CanvasFrame cf2 = new CanvasFrame(n2);
        cf1.showImage(f1.img());
        cf2.showImage(f2.img());
        cf1.waitKey();
        cf1.dispose();
        cf2.dispose();
    }

    public static void frames(IplImage f1, String n1, IplImage f2, String n2) throws InterruptedException {
        CanvasFrame cf1 = new CanvasFrame(n1);
        CanvasFrame cf2 = new CanvasFrame(n2);
        cf1.showImage(f1);
        cf2.showImage(f2);
        cf1.waitKey();
        cf1.dispose();
        cf2.dispose();
    }

}
