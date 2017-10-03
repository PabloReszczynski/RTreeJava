import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import net.iryndin.jdbf.writer.DbfWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class RectangleCreator {
    /**
     * Crea un archivo con un número grande (2^9, 2^25) de rectángulos en un archivo.
     */

    public static void makeRectangles(long n, int M, int idx) throws IOException {
        final FileOutputStream out = new FileOutputStream("rectangles" + idx + ".csv");
        final Appendable appendable = new OutputStreamWriter(out);
        final CSVPrinter printer = CSVFormat.DEFAULT.withHeader("x", "y", "w", "h").print(appendable);
        Random rng = new Random();
        ArrayList<String[]> data = new ArrayList<String[]>();
        for (long i = 0; i < n; i++) {
            String x = rng.nextDouble() + "";
            String y = rng.nextDouble() + "";
            String w = rng.nextDouble() + "";
            String h = rng.nextDouble() + "";

            data.add(new String[]{ x, y, w, h });

            if (data.size() >= M) {
                printer.printRecords(data);
                data = new ArrayList<String[]>();
            }
        }
        printer.printRecords(data);
        printer.close();
        out.close();
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
        for (int i = 9; i <= 25; i++) {
            makeRectangles((long) Math.pow(2, i), 4096, i);
        }
        System.out.println("Made all rects in " + ((System.currentTimeMillis() - t1) / 1000.0) + " time");
    }
}
