import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import net.iryndin.jdbf.writer.DbfWriter;

public class RectangleCreator {
    /**
     * Crea un archivo con un número grande (2^9, 2^25) de rectángulos en un archivo.
     */

    public static void makeRectangles(long n, int M) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("rectangles")));
        Random rng = new Random();
        ArrayList<Rectangle2D> rects = new ArrayList<Rectangle2D>();
        for (long i = 0; i < n; i++) {
            double x = rng.nextDouble();
            double y = rng.nextDouble();
            double w = rng.nextDouble();
            double h = rng.nextDouble();

            rects.add(new Rectangle2D.Double(x, y, w, h));

            if (rects.size() >= M) {
                StringBuilder end = new StringBuilder();
                for (Rectangle2D rect : rects) {
                    end.append(String.format("%f,%f,%f,%f\n", rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
                }
                writer.write(end.toString());
                rects = new ArrayList<Rectangle2D>();
            }
        }
        writer.close();
    }

    public static Rectangle2D readLine(String input) {
        Scanner s = new Scanner(input);
        double x = s.nextDouble();
        double y = s.nextDouble();
        double w = s.nextDouble();
        double h = s.nextDouble();

        return new Rectangle2D.Double(x, y, w, h);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        long t1 = System.currentTimeMillis();
        makeRectangles((long) 2e9, 4096);
        System.out.println("Made all rects in " + ((System.currentTimeMillis() - t1) / 1000) + " time");
    }
}
