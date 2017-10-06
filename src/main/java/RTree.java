import com.esri.core.geometry.Envelope;

import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class RTree implements Serializable {

    private int id;
    //private ArrayList children;
    private ArrayList<Rectangle2D> rectangles;
    private boolean empty;
    private int M;
    private Rectangle2D MBR;
    private OverflowHeuristic heuristic;
    private HashMap<Integer, Rectangle2D> childrenMBR;

    private int father; //RTree node id

    private boolean isLeaf;


    public RTree (int M, OverflowHeuristic h) {
        //this.children = new ArrayList<>();
        this.rectangles = new ArrayList<Rectangle2D>();
        this.id = -1;
        this.empty = true;
        this.heuristic = h;
        this.MBR = new Rectangle2D.Double();
        this.M = M;

        this.father = -2;
        isLeaf = true;
        this.childrenMBR = new HashMap<>();
    }

    public RTree(Envelope env, int M, OverflowHeuristic h, int father) throws IOException, ClassNotFoundException {
        this(rectFromEnvelope(env), M, h, father);
    }

    public RTree(Rectangle2D rect, int M, OverflowHeuristic h, int father) throws IOException, ClassNotFoundException {
        this.id = rect.hashCode() * father;
        //this.children = new ArrayList<>();
        this.rectangles = new ArrayList<Rectangle2D>();

        this.rectangles.add(rect);
        this.MBR = rect;
        this.heuristic = h;
        this.M = M;
        
        this.father = father;
        isLeaf = true;
        this.childrenMBR = new HashMap<>();
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
        if (root.isLeaf) {
            for (Rectangle2D rectangle : root.rectangles) {
                if (rectangle.intersects(rect)) {
                    res.add(rectangle);
                }
            }
        } else {
            ArrayList<Integer> rootChildren = new ArrayList<>(root.childrenMBR.keySet());
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
        if (node.isLeaf) {
            node.rectangles.add(rect);
            node.MBR = node.MBR.createUnion(rect);
            writeNode(node);

            int newSize = node.rectangles.size();
            int fatherId = node.father;
            int M = node.M;
            OverflowHeuristic heuristic = node.heuristic;
            Rectangle2D newMBR = node.MBR;

            node = null;

            updateChildMBR(nodeId, fatherId, newMBR);

            if (newSize >= M) {
                System.out.println("divide leaf " + nodeId);
                heuristic.divideTree(nodeId);
            }

        } else {
            double growth = -1;
            int lessGrowthNodeId = -2;
            ArrayList<Integer> children = new ArrayList<>(node.childrenMBR.keySet());
            node = null;

           // System.out.println(children);
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

    public static void insertDirectly(int nodeId, ArrayList<Rectangle2D> rects) throws IOException, ClassNotFoundException {
        RTree node = readNode(nodeId);
        for (Rectangle2D rect : rects) {
            node.rectangles.add(rect);
        }
        writeNode(node);
    }

    public static void addChild(int nodeId, int childId, Rectangle2D MBR) throws IOException, ClassNotFoundException {
        RTree node = readNode(nodeId);
        node.notLeaf();
        node.childrenMBR.put(childId, MBR);
        node.setMBR(node.getMBR().createUnion(MBR));
        int fatherId = node.father;
        Rectangle2D newMBR = node.MBR;
        writeNode(node);
        updateChildMBR(nodeId, fatherId, newMBR);
    }

    public static void addChild(int nodeId, int[] children, Rectangle2D[] rects) throws IOException, ClassNotFoundException {
        RTree node = readNode(nodeId);
        node.notLeaf();
        for (int i = 0; i < children.length; i++) {
            node.childrenMBR.put(children[i], rects[i]);
            node.setMBR(node.getMBR().createUnion(rects[i]));
        }

        writeNode(node);

        OverflowHeuristic heuristic = node.heuristic;
        if (node.childrenMBR.size() >= node.M) {
            System.out.println("divide node " + nodeId);
            node = null;
            heuristic.divideTree(nodeId);
        } else {
            int fatherId = node.father;
            Rectangle2D newMBR = node.MBR;
            writeNode(node);
            node = null;
            updateChildMBR(nodeId, fatherId, newMBR);

        }
    }

    // Getters
    public int getFather() {
        return father;
    }

//    public ArrayList getChildren() {
//        return children;
//    }

    public ArrayList<Rectangle2D> getRectangles() {
        if (isLeaf) {
            return rectangles;
        } else {
            return new ArrayList<>(childrenMBR.values());
        }
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
    public void setMBR(Rectangle2D MBR) {
        this.MBR = MBR;
    }
    public void resetChildren() {
        //children = new ArrayList<>();
        childrenMBR = new HashMap<Integer, Rectangle2D>();
    }

    public void resetRectangles() {
        rectangles = new ArrayList<Rectangle2D>();
    }

    public void notLeaf() {
        isLeaf = false;
    }

    public static void notLeaf(int nodeId) throws IOException, ClassNotFoundException {
        RTree node = readNode(nodeId);
        node.notLeaf();
        writeNode(node);
    }

    private static void updateChildMBR(int childId, int fatherId, Rectangle2D newMBR) throws IOException, ClassNotFoundException {

        while (fatherId != -2) {
            RTree father = readNode(fatherId);
            father.childrenMBR.put(childId, newMBR);
            father.MBR = father.MBR.createUnion(newMBR);
            writeNode(father);

            childId = fatherId;
            fatherId = father.father;
            newMBR = father.MBR;
        }
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

    public static void deleteNode(RTree node) throws IOException, ClassNotFoundException {
        int id = node.id;
        int fatherId = node.getFather();
        node = null;
        deleteNode(id, fatherId);
    }

    public static void deleteNode(int nodeId, int fatherId) throws IOException, ClassNotFoundException {
        RTree father = readNode(fatherId);
        father.childrenMBR.remove(nodeId);
        writeNode(father);
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
