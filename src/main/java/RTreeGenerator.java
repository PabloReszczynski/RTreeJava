import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Polygon;
import com.esri.shp.ShpReader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.*;

public class RTreeGenerator {
    /*
     *  Generates an RTree from a SHP file
     */

    public static int readConfigFile(String filename) throws IOException, SAXException, ParserConfigurationException {
        final File file = new File(filename);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder= factory.newDocumentBuilder();
        Document document = docBuilder.parse(file);

        return Integer.parseInt(document.getElementsByTagName("M").item(0).getTextContent());
    }

    public static RTree RTreeGenerator(String filename) throws IOException, ParserConfigurationException, SAXException, ClassNotFoundException {

       final File file = new File(filename);
       final FileInputStream fis = new FileInputStream(file);

       int M;

        try {
            M = readConfigFile("config.xml");
        } catch (Exception e) {
            throw e;
        }

        RTree root = new RTree(M, new LinearSplit()); // ¡instruccion de qué heurística a usar !

       try {
           final Envelope envelope = new Envelope();
           final Polygon polygon = new Polygon();
           final ShpReader shpReader = new ShpReader(new DataInputStream(new BufferedInputStream(fis)));

           while (shpReader.hasMore()) {
               shpReader.queryPolygon(polygon);
               polygon.queryEnvelope(envelope);

               System.out.println(envelope);
               root.insert(envelope);
           }
           RTree.writeNode(root);
       } catch (IOException e) {
           e.printStackTrace();
       }

       return root;
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, ClassNotFoundException {
        RTree root = RTreeGenerator("shape");
    }
}
