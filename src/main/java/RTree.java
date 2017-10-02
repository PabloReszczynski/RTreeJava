import com.esri.core.geometry.Envelope;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

public class RTree implements Serializable {

    private int id;
    private ArrayList<Integer> children;
    private ArrayList<Rectangle2D> rectangles;
    private boolean empty;
    private int M;
    private Rectangle2D MBR;
    private OverflowHeuristic heuristic;

    private RTree father;

    private boolean isLeaf;


    // FIXME: Pasar de 2 arreglos a un Map

    public RTree (int M, OverflowHeuristic h) {
        this.children = new ArrayList<Integer>();
        this.rectangles = new ArrayList<Rectangle2D>();
        this.id = -1;
        this.empty = true;
        this.heuristic = h;
        this.MBR = new Rectangle2D.Double();
        this.M = M;

        this.father = null;
        isLeaf = true;
    }
    public RTree(Envelope env, int M, OverflowHeuristic h, RTree father) {
        Rectangle2D rect = rectFromEnvelope(env);

        this.id = env.hashCode();
        this.children = new ArrayList<Integer>();
        this.rectangles = new ArrayList<Rectangle2D>();

        this.rectangles.add(rect);
        this.MBR = rect;
        this.heuristic = h;
        this.M = M;

        this.father = father;

        isLeaf = true;

    }

    public RTree(Rectangle2D rect, int M, OverflowHeuristic h, RTree father) {
        this.id = rect.hashCode();
        this.children = new ArrayList<Integer>();
        this.rectangles = new ArrayList<Rectangle2D>();

        this.rectangles.add(rect);
        this.MBR = rect;
        this.heuristic = h;
        this.M = M;
        
        this.father = father;
        isLeaf = true;
    }


    public int getM() {
        return M;
    }

    public void insert(Envelope env) throws IOException, ClassNotFoundException {
        /* Inserta un rectangulo en la lista de MBRs. Aquí se debe verificar si existe overflow */
        Rectangle2D rect = rectFromEnvelope(env);
        insert(rect);
    }

    public void insert(Rectangle2D rect) throws IOException, ClassNotFoundException {
    	//si es una hoja
    	if (children.isEmpty()){
    		rectangles.add(rect);
    		
    		//obtengo nodo padre
    		if (getFather()!=null){
    			father.children.add(getId());
    		}
    		
            //children.add(id);
            MBR = MBR.createUnion(rect);

            if (rectangles.size() >= M) {
                heuristic.divideTree(this);
                notLeaf();
            }
    	}
    	else { // si no es hoja
    		double growth=-1;
    		RTree lessGrowthNode = null;
    		
    		//escojo el MBR (entre los hijos)  que deba crecer lo menos posible
    		for (Integer child : children){
    			//obtengo nodo
                try {

                    System.out.println("child " + child);
                    RTree childNode = readNode(child);
                    Rectangle2D union = (childNode.MBR).createUnion(rect); //MBR de la union
                    double childNodeArea = (childNode.MBR).getWidth() * (childNode.MBR).getHeight();
                    double unionArea = union.getHeight() * union.getWidth();
                    if (growth < 0 || (unionArea - childNodeArea) < growth) {
                        lessGrowthNode = childNode;
                        growth = unionArea - childNodeArea;
                    }
                } catch (FileNotFoundException e) {
                    continue;
                } catch (ClassNotFoundException e) {
                    continue;
                }
    		}
            assert lessGrowthNode != null;
            lessGrowthNode.insert(rect);
    		
    		if (lessGrowthNode != null) {
                lessGrowthNode.insert(rect);
            }
    	}
        
    }

    public RTree getFather() {
		return father;
	}

	// Getters
    public ArrayList<Integer> getChildren() {
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
        children = new ArrayList<Integer>();
    }

    public void resetRectangles() {
        rectangles = new ArrayList<Rectangle2D>();
    }

    public void notLeaf() {
        isLeaf = false;
    }

    // Funciones estaticas
    public static void writeNode(RTree node) throws IOException {
        String filename = node.id + ".ser";
        FileOutputStream file = new FileOutputStream(filename);
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeObject(node);
        out.close();
        file.close();
        System.out.println(filename + " written.");
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

    public static void deleteNode(RTree node) throws IOException {
        String filename = node.id + ".ser";
        File file = new File(filename);
        file.delete();
        node = null;
    }

    private static Rectangle2D rectFromEnvelope(Envelope env) {
        double x = env.getXMin();
        double y = env.getYMax();
        double width = env.getWidth();
        double height = env.getHeight();

        return new Rectangle2D.Double(x, y, width, height);

    }

}
