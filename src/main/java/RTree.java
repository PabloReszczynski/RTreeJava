import com.esri.core.geometry.Envelope;

import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;

public class RTree implements Serializable {

    private int id;
    private ArrayList<Integer> children;
    private ArrayList<Rectangle2D> rectangles;
    private boolean empty;

    public RTree () {
        this.children = new ArrayList<Integer>();
        this.id = -1;
        this.empty = true;
    }

    public RTree(Envelope env) {
        double x = env.getXMin();
        double y = env.getYMax();
        double width = env.getWidth();
        double height = env.getHeight();

        Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);

        this.id = env.hashCode();
        this.children = new ArrayList<Integer>();
        this.rectangles = new ArrayList<Rectangle2D>();

        this.rectangles.add(rect);
    }


    public void insert(RTree branch) {

    }

    public static void writeNode(RTree node) throws IOException {
        String filename = node.id + ".ser";
        FileOutputStream file = new FileOutputStream(filename);
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeObject(node);
        out.close();
        file.close();
    }

    public static RTree readNode(int id) throws IOException, ClassNotFoundException {
        String filename = id + ".ser";
        FileInputStream file = new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(file);
        RTree node = (RTree) in.readObject();
        in.close();
        file.close();
        return node;
    }
}
