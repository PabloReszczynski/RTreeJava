import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Polygon;
import com.esri.shp.ShpReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
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

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, ClassNotFoundException {
        String number = "12";
        OverflowHeuristic heuristic = new LinearSplit();
        deleteSerFiles();
        long time1 = System.nanoTime();
        RTreeGenerator("rectangles" + number + ".csv", heuristic);
        long diff = TimeUnit.SECONDS.convert(System.nanoTime() - time1, TimeUnit.NANOSECONDS);
        System.out.println("Took " + diff + " seconds to build RTree with " + heuristic.toString() + " and 2^" + number + " rectangles");

        deleteSerFiles();
        heuristic = new GreeneSplit();
        time1 = System.nanoTime();
        RTreeGenerator("rectangles" + number + ".csv", heuristic);
        diff = TimeUnit.SECONDS.convert(System.nanoTime() - time1, TimeUnit.NANOSECONDS);
        System.out.println("Took " + diff + " seconds to build RTree with " + heuristic.toString() + " and 2^" + number + " rectangles");
    }
}
