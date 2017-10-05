import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;


public class GreeneSplit implements OverflowHeuristic, Serializable {

	@Override
	public void divideTree(int nodeId) throws IOException, ClassNotFoundException {

	    RTree node = RTree.readNode(nodeId);
	    int M = node.getM();
	    int father = node.getFather();
	    if (father == -2)
	        father = nodeId;
	    OverflowHeuristic heuristic = node.getHeuristic();

		Rectangle2D r1_x = node.getRectangles().get(0);
        Rectangle2D r2_x = node.getRectangles().get(node.getRectangles().size() - 1);
        for (Rectangle2D rect : node.getRectangles()) {
            if (rect.getMinX() < r1_x.getMinX()) {
                r1_x = rect;
            }
            else if (rect.getMaxX() > r2_x.getMaxX()) {
                r2_x = rect;
            }
        }
        double separation_x = r2_x.getMaxX() - r1_x.getMinX();

        // Para Y
        Rectangle2D r1_y = node.getRectangles().get(0);
        Rectangle2D r2_y = node.getRectangles().get(node.getRectangles().size() - 1);
        for (Rectangle2D rect : node.getRectangles()) {
            if (rect.getMinY() < r1_y.getMinY()) {
                r1_y = rect;
            }
            else if (rect.getMaxY() > r2_y.getMaxY()) {
                r2_y = rect;
            }
        }
        double separation_y = r2_y.getMaxY() - r1_y.getMinY();


        // Se escoje el par de nodos con separación más grande
        // falta normalizar!!!!!
        Rectangle2D r1, r2;
        String separacionCorte;
        if (separation_x > separation_y) {
            r1 = r1_x;
            r2 = r2_x;
            separacionCorte = "y";
        } else {
            r1 = r1_y;
            r2 = r2_y;
            separacionCorte = "x";
        }
        
        ArrayList<Rectangle2D> rectangles = new ArrayList<>(node.getRectangles());

        node = null;

        int leftId = RTree.makeRTree(r1, M, heuristic, father);
        int rightId = RTree.makeRTree(r2, M, heuristic, father);

        for (int i=0; i<=((M/2) -1); i++){
        	RTree.insert(leftId, rectangles.get(i));
        }
        for (int i=(M/2); i<(M); i++){
        	RTree.insert(rightId, rectangles.get(i));
        }

        node = RTree.readNode(nodeId);
        
        // Reseteamos los hijos y agregamos los 2 nuevos nodos
        node.resetChildren();
        node.resetRectangles();
        RTree.writeNode(node);

        // Guardamos en disco el nodo y los hijos
        //y el padre

        Rectangle2D leftMBR = (Rectangle2D) RTree.readNode(leftId).getMBR().clone();
        Rectangle2D rightMBR = (Rectangle2D) RTree.readNode(rightId).getMBR().clone();

        if (father != nodeId){

            RTree.insert(father, leftMBR);
            RTree.insert(father, rightMBR);

            RTree.addChild(father, new int[]{leftId, rightId});

            RTree.deleteNode(node);
        }
        else{
            RTree.insert(nodeId, leftMBR);
            RTree.insert(nodeId, rightMBR);

            RTree.addChild(nodeId, new int[]{leftId, rightId});

            RTree.writeNode(node);
        }
	}
	public String toString() {
	    return "GreeneSplit";
    }

}
