import com.esri.core.geometry.Envelope;

import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

public class RTree implements Serializable {

    private int id;
    private ArrayList children;
    private ArrayList<Rectangle2D> rectangles;
    private boolean empty;
    private int M;
    private Rectangle2D MBR;
    private OverflowHeuristic heuristic;

    private int father; //RTree node id

    private boolean isLeaf;


    public RTree (int M, OverflowHeuristic h) {
        this.children = new ArrayList<>();
        this.rectangles = new ArrayList<Rectangle2D>();
        this.id = -1;
        this.empty = true;
        this.heuristic = h;
        this.MBR = new Rectangle2D.Double();
        this.M = M;

        this.father = -2;
        isLeaf = true;
    }

    public RTree(Envelope env, int M, OverflowHeuristic h, int father) throws IOException, ClassNotFoundException {
        this(rectFromEnvelope(env), M, h, father);
    }

    public RTree(Rectangle2D rect, int M, OverflowHeuristic h, int father) throws IOException, ClassNotFoundException {
        this.id = rect.hashCode();
        this.children = new ArrayList<>();
        this.rectangles = new ArrayList<Rectangle2D>();

        this.rectangles.add(rect);
        this.MBR = rect;
        this.heuristic = h;
        this.M = M;
        
        this.father = father;
        isLeaf = true;

        RTree fat  = RTree.readNode(father);
        fat.children.add(id);
        RTree.writeNode(fat);
    }


    public static int makeRTree(Rectangle2D rect, int M, OverflowHeuristic h, int father) throws IOException, ClassNotFoundException {
        RTree node = new RTree(rect, M, h, father);
        int id = node.getId();
        writeNode(node);
        return id;
    }


    public int getM() {
        return M;
    }

    public static ArrayList<Rectangle2D> search (int rootId, Rectangle2D rect) throws ClassNotFoundException, IOException {
        ArrayList<Rectangle2D> res = new ArrayList<>();
        RTree root = RTree.readNode(rootId);
        if (root.children.isEmpty()) {
            if (root.MBR.contains(rect)) {
                for (Rectangle2D rectangle : root.rectangles) {
                    if (rectangle.contains(rect)) {
                        res.add(rectangle);
                    }
                }
            }
        } else {
            ArrayList<Integer> rootChildren = new ArrayList<>(root.children);
            root = null;
            for (Integer child : rootChildren) {
                res.addAll(RTree.search(child, rect));
            }
        }

        return res;
    }

    public static void insert(int nodeId, Envelope env) throws IOException, ClassNotFoundException {
        /* Inserta un rectangulo en la lista de MBRs. Aquí se debe verificar si existe overflow */
        Rectangle2D rect = rectFromEnvelope(env);
        insert(nodeId, rect);
    }

    public static void insert(int nodeId, Rectangle2D rect) throws IOException, ClassNotFoundException {
        RTree node = readNode(nodeId);
        if (node.children.isEmpty()) {
            node.rectangles.add(rect);
            node.MBR = node.MBR.createUnion(rect);
            writeNode(node);

            if (node.father != -2) {
                RTree father = readNode(node.father);
                writeNode(node);
                Rectangle2D nodeMBR = (Rectangle2D) node.MBR.clone();
                node = null;
                father.updateChildMBR(nodeId, nodeMBR);
                writeNode(father);
                node = readNode(nodeId);
            }

            if (node.rectangles.size() >= node.M) {
                node.notLeaf();
                writeNode(node);
                OverflowHeuristic heuristic = node.heuristic;
                node = null;
                heuristic.divideTree(nodeId);
            }
        } else {
            double growth = -1;
            int lessGrowthNodeId = -2;
            ArrayList<Integer> children = new ArrayList<>(node.children);
            writeNode(node);
            node = null;

            for (Integer child : children) {
                RTree childNode = readNode(child);
                Rectangle2D union = (childNode.MBR).createUnion(rect);
                double childNodeArea = (childNode.MBR).getWidth() * (childNode.MBR).getHeight();
                double unionArea = union.getHeight() * union.getWidth();
                if (growth < 0 || (unionArea - childNodeArea) < growth) {
                    lessGrowthNodeId = child;
                    growth = unionArea - childNodeArea;
                }
            }
            insert(lessGrowthNodeId, rect);
        }

    }

    public static void addChild(int nodeId, int childId) throws IOException, ClassNotFoundException {
        RTree node = readNode(nodeId);
        node.children.add(childId);
        writeNode(node);
    }

    public static void addChild(int nodeId, int[] children) throws IOException, ClassNotFoundException {
        RTree node = readNode(nodeId);
        for (int child : children) {
            node.children.add(child);
        }
        writeNode(node);
    }

	// Getters
    public int getFather() {
        return father;
    }

    public ArrayList getChildren() {
        return children;
    }

    public ArrayList<Rectangle2D> getRectangles() {
        return rectangles;
    }

    public OverflowHeuristic getHeuristic() {
        return heuristic;
    }

    public Rectangle2D getMBR() {
        return MBR;
    }

    public int getId() {
        return id;
    }

    // Setters
    public void resetChildren() {
        children = new ArrayList<>();
    }

    public void resetRectangles() {
        rectangles = new ArrayList<Rectangle2D>();
    }

    public void notLeaf() {
        isLeaf = false;
    }

    private void updateChildMBR(int id, Rectangle2D newMBR) {
        int idx = children.indexOf(id);
        try {
            rectangles.set(idx, newMBR);
        } catch (IndexOutOfBoundsException e) {
            rectangles.add(idx, newMBR);
        }
        MBR = MBR.createUnion(newMBR);
    }

    // Funciones estaticas
    public static void writeNode(RTree node) throws IOException {
        IOCount counter = IOCount.getInstance();
        counter.write();
        String filename = node.id + ".ser";
        FileOutputStream file = new FileOutputStream(filename);
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeObject(node);
        out.close();
        file.close();
        //System.out.println(filename + " written.");
    }

    public static RTree readNode(int id) throws IOException, ClassNotFoundException {
        IOCount counter = IOCount.getInstance();
        counter.read();
        String filename = id + ".ser";
        FileInputStream file = new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(file);
        RTree node = (RTree) in.readObject();
        in.close();
        file.close();
        return node;
    }

    public static void deleteNode(RTree node) {
        String filename = node.id + ".ser";
        File file = new File(filename);
        file.delete();
        node = null;
    }

    public static void deleteNode(int nodeId) {
        String filename = nodeId + ".ser";
        File file = new File(filename);
        file.delete();
    }

    private static Rectangle2D rectFromEnvelope(Envelope env) {
        double x = env.getXMin();
        double y = env.getYMax();
        double width = env.getWidth();
        double height = env.getHeight();

        return new Rectangle2D.Double(x, y, width, height);

    }

}
