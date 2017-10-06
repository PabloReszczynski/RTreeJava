import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;

public class main {
    public static void main(int argc, String argv[]) {
        //RTreeGenerator.main();
    }

    public static long getRectangleSize() throws IOException {
        Rectangle2D rect = new Rectangle2D.Double();
        FileOutputStream fileos = new FileOutputStream("rectangle.ser");
        ObjectOutputStream out = new ObjectOutputStream(fileos);
        out.writeObject(rect);
        out.close();
        fileos.close();
        FileInputStream file = new FileInputStream("rectangle.ser");
        long size = file.getChannel().size();
        file.close();
        return size;
    }

    public static long getNodeSize() throws IOException {
        RTree node = new RTree(1, new LinearSplit());
        RTree.writeNode(node);
        FileInputStream file= new FileInputStream("-1.ser");
        long size = file.getChannel().size();
        file.close();
        return size;
    }

    public static void main(String[] args) throws IOException {
        long rectangleSize = getRectangleSize();
        System.out.println("rect size " + rectangleSize);
        long nodeSize = getNodeSize();
        System.out.println("empty node size " + nodeSize);
    }
}
