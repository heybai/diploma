package com.heybai.diploma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * Created by heybai on 5/12/14.
 */
public class ObjProducer {

    private static Logger LOG = LoggerFactory.getLogger(ObjProducer.class);

    private static double scale = 0.1;

    private static double[][] v = new double[][] {
            {0, 0, 0},
            {0, 1, 0},
            {1, 1, 0},
            {1, 0, 0},
            {0, 0, 1},
            {0, 1, 1},
            {1, 1, 1},
            {1, 0, 1}
    };

    private static int[][] f = new int[][] {
            {1, 2, 3, 4},
            {5, 6, 7, 8},
            {1, 2, 6, 5},
            {2, 3, 7, 6},
            {3, 4, 8, 7},
            {4, 1, 5, 8}
    };

    public static void createObj(List<Point3D> points, String fileName) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));

            for (Point3D p : points) {
                for (int i = 0; i < v.length; ++i) {
                    bw.write(String.format("v %s %s %s\n",
                            scale * v[i][0] + p.getX(),
                            scale * v[i][1] + p.getY(),
                            scale * v[i][2] + p.getZ()
                    ));
                }
            }

            int idx = 0;
            for (Point3D p : points) {
                for (int i = 0; i < f.length; ++i) {
                    bw.write(String.format("f %s %s %s %s\n",
                            idx + f[i][0],
                            idx + f[i][1],
                            idx + f[i][2],
                            idx + f[i][3]
                    ));
                }
                idx += 8;
            }

            bw.close();
            LOG.info("File pipe.obj created");
        } catch (FileNotFoundException e) {
            LOG.error("Error creating .obj file");
        } catch (IOException e) {
            LOG.error("Error 2 creating .obj file");
        }
    }

}
