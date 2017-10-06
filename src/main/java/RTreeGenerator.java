import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RTreeGenerator {
    /*
     *  Generates an RTree from a SHP file
     */

    public static int readConfigFile(String filename) throws IOException, SAXException, ParserConfigurationException {
        final File file = new File(filename);
        final FileInputStream in = new FileInputStream(filename);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder= factory.newDocumentBuilder();
        Document document = docBuilder.parse(file);

        return Integer.parseInt(document.getElementsByTagName("M").item(0).getTextContent());
    }

    public static void deleteSerFiles() {
        // Borra todos los archivos .ser generados por RTree
        File folder = new File(".");
        File files[] = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".ser");
            }
        });
        for (File file : files) {
            file.delete();
        }
    }

    public static void RTreeGenerator(String filename, OverflowHeuristic heuristic) throws IOException, ParserConfigurationException, SAXException, ClassNotFoundException {

       final FileReader in = new FileReader(filename);
       final CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(in);

       int M;

        try {
            M = readConfigFile("config.xml");
        } catch (Exception e) {
            throw e;
        }

        RTree root = new RTree(M, heuristic); // ¡instruccion de qué heurística a usar !
        int rootId = root.getId();
        RTree.writeNode(root);
        root = null;

       try {
           //final Envelope envelope = new Envelope();
           //final Polygon polygon = new Polygon();
           //final ShpReader shpReader = new ShpReader(new DataInputStream(new BufferedInputStream(fis)));
           Rectangle2D rect;

           for (CSVRecord record : parser) {
               double x = Double.parseDouble(record.get("x"));
               double y = Double.parseDouble(record.get("y"));
               double w = Double.parseDouble(record.get("w"));
               double h = Double.parseDouble(record.get("h"));
               rect = new Rectangle2D.Double(x, y, w, h);
               RTree.insert(rootId, rect);
           }
       } catch (IOException e) {
           e.printStackTrace();
       }

       return;
    }

    public static ArrayList<Rectangle2D> searchOne() throws IOException, ClassNotFoundException {
        Random rng = new Random();
        double x = rng.nextDouble() * 500000;
        double y = rng.nextDouble() * 500000;
        double w = rng.nextDouble() * 100.0;
        double h = rng.nextDouble() * 100.0;
        Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);
        IOCount counter = IOCount.getInstance();
        counter.reset();
        long time1 = System.nanoTime();
        ArrayList<Rectangle2D> result = RTree.search(-1, rect);
        long diff = TimeUnit.SECONDS.convert(System.nanoTime() - time1, TimeUnit.NANOSECONDS);
        long writes = counter.getWrites();
        long reads = counter.getReads();
        System.out.println("Took " + diff + " seconds to search " + rect.toString());
        System.out.println("Took " + (writes+reads) + " IO to search");
        return result;
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, ClassNotFoundException {
        IOCount counts = IOCount.getInstance();
        OverflowHeuristic heuristic = new LinearSplit();
        for (int number = 9; number <= 25; number++) {
            deleteSerFiles();
            long time1 = System.nanoTime();
            RTreeGenerator("rectangles" + number + ".csv", heuristic);
            long diff = TimeUnit.SECONDS.convert(System.nanoTime() - time1, TimeUnit.NANOSECONDS);
            System.out.println("Took " + diff + " seconds to build RTree with " + heuristic.toString() + " and 2^" + number + " rectangles");
            long reads = counts.getReads();
            long writes = counts.getWrites();
            System.out.println("Took " + (reads+writes) + " reads to disk");

            int i = 5;
            while (i-->0) {
                System.out.println("Buscando 5 rectangulos generados aleatoriamente");
                ArrayList<Rectangle2D> result = searchOne();
            }

        }

        for (int number = 9; number <= 25; number++) {
            deleteSerFiles();
            counts.reset();
            deleteSerFiles();
            heuristic = new GreeneSplit();
            long time1 = System.nanoTime();
            RTreeGenerator("rectangles" + number + ".csv", heuristic);
            long diff = TimeUnit.SECONDS.convert(System.nanoTime() - time1, TimeUnit.NANOSECONDS);
            System.out.println("Took " + diff + " seconds to build RTree with " + heuristic.toString() + " and 2^" + number + " rectangles");
            long reads = counts.getReads();
            long writes = counts.getWrites();
            System.out.println("Took " + (reads+writes) + " reads to disk");

            int i = 5;
            while (i-->0) {
                System.out.println("Buscando 5 rectangulos generados aleatoriamente");
                ArrayList<Rectangle2D> result = searchOne();
            }
        }
    }
}
