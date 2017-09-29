import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Polygon;
import com.esri.shp.ShpReader;

import java.io.*;

public class RTreeGenerator {
    /*
     *  Generates an RTree from a BDF file
     */

    public static RTree RTreeGenerator(String filename) throws FileNotFoundException {

       final File file = new File(filename);
       final FileInputStream fis = new FileInputStream(file);

       RTree root = new RTree();

       try {
           final Envelope envelope = new Envelope();
           final Polygon polygon = new Polygon();
           final ShpReader shpReader = new ShpReader(new DataInputStream(new BufferedInputStream(fis)));

           while (shpReader.hasMore()) {
               shpReader.queryPolygon(polygon);
               polygon.queryEnvelope(envelope);

               System.out.println(envelope);
               root.insert(new RTree(envelope));
           }
       } catch (IOException e) {
           e.printStackTrace();
       }

       return root;
    }

    public static void main(String[] args) throws FileNotFoundException {
        RTree root = RTreeGenerator("shape");
    }
}
