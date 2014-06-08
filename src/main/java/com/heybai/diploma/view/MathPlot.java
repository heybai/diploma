package com.heybai.diploma.view;

import com.heybai.diploma.model.Point;
import org.math.plot.Plot2DPanel;

import javax.swing.*;
import java.util.List;

/**
 * Created by heybai on 5/7/14.
 */
public class MathPlot {

    public static void plot(String plotName, String xName, String yName, List<Point>... pointses) {
        // create your PlotPanel (you can use it as a JPanel)
        Plot2DPanel plot = new Plot2DPanel();
        plot.setAxisLabel(0, xName);
        plot.setAxisLabel(1, yName);

        // add a line plot to the PlotPanel
        for (List<Point> points : pointses) {
            double[] x = new double[points.size()];
            double[] y = new double[points.size()];

            for (int i = 0; i < points.size(); ++i) {
                x[i] = points.get(i).getX();
                y[i] = points.get(i).getY();
            }

            plot.addLinePlot(plotName, x, y);
        }

        // put the PlotPanel in a JFrame, as a JPanel
        JFrame frame = new JFrame(plotName);
        frame.setContentPane(plot);
        frame.setVisible(true);
        frame.setSize(1100, 700);
    }

}
