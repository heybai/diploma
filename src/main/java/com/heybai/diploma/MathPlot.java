package com.heybai.diploma;

import org.math.plot.Plot2DPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by heybai on 5/7/14.
 */
public class MathPlot {

    public void features(List<Features> featureses) {
        plot(featuresToPlot(featureses), "frame", "nFeatures", "Features stats");
    }

    public List<Point> featuresToPlot(List<Features> featureses) {
        List<Point> points = new ArrayList<Point>();
        for (int i = 0; i < featureses.size(); ++i) {
            points.add(new Point(i, featureses.get(i).nFeatures()));
        }
        return points;
    }

    public void matches(List<Matches> matcheses) {
        plot(matchesToPlot(matcheses), "frame", "nMatches", "Matches stats");
    }

    public List<Point> matchesToPlot(List<Matches> matcheses) {
        List<Point> points = new ArrayList<Point>();
        for (int i = 0; i < matcheses.size(); ++i) {
            int n = matcheses.get(i).nMatches();
//            points.add(new Point(i, n > 200 ? 100 : n));
            points.add(new Point(i, n));
        }
        return points;
    }

    private void plot(List<Point> points, String xName, String yName, String plotName) {
        double[] x = new double[points.size()];
        double[] y = new double[points.size()];

        for (int i = 0; i < points.size(); ++i) {
            x[i] = points.get(i).getX();
            y[i] = points.get(i).getY();
        }

        // create your PlotPanel (you can use it as a JPanel)
        Plot2DPanel plot = new Plot2DPanel();
        plot.setAxisLabel(0, xName);
        plot.setAxisLabel(1, yName);

        // add a line plot to the PlotPanel
        plot.addLinePlot(plotName, x, y);

        // put the PlotPanel in a JFrame, as a JPanel
        JFrame frame = new JFrame("a plot panel");
        frame.setContentPane(plot);
        frame.setVisible(true);
        frame.setSize(1100, 700);
    }

}
