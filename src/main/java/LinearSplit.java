
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class LinearSplit implements OverflowHeuristic, Serializable {

    public void divideTree(int nodeId) throws IOException, ClassNotFoundException {
        /*
         * Para cada dimensión, se determina el rectángulo con el valor máximo del lado bajo. (ej. aquel cuyo lado
         * izquierdo está más a la derecha) y aquel con el valor mínimo del lado alto (ej. aquel cuyo lado derecho
         * está más a la izquierda). Se almacena la separación entre estos lados.
         */


        RTree node = RTree.readNode(nodeId);
        OverflowHeuristic heuristic = node.getHeuristic();
        int fatherId = node.getFather();
        int M = node.getM();

        if (fatherId == -2) {
            fatherId = nodeId;
        }

        // Para X

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
        Rectangle2D r1, r2;
        if (separation_x > separation_y) {
            r1 = r1_x; r2 = r2_x;
        } else {
            r1 = r1_y; r2 = r2_y;
        }

        int leftId = RTree.makeRTree(r1, M, heuristic, fatherId);
        int rightId = RTree.makeRTree(r2, M, heuristic, fatherId);

        // Por cada otro nodo en este nodo se agrega a r1 o a r2 dependiendo cual hace crecer el area menos.

        ArrayList<Rectangle2D> rectangles = new ArrayList<>(node.getRectangles());
        ArrayList<Rectangle2D> leftRects = new ArrayList<>();
        ArrayList<Rectangle2D> rightRects = new ArrayList<>();

        node = null;

        for (int i = 0; i < rectangles.size(); i++) {
            Rectangle2D rect = rectangles.get(i);
            //int id = node.getChildren().get(i);
            Rectangle2D candidate1 = r1.createUnion(rect);
            Rectangle2D candidate2 = r2.createUnion(rect);

            double area1 = candidate1.getWidth() * candidate1.getHeight();
            double area2 = candidate2.getWidth() * candidate2.getHeight();

            if (area1 < area2) {
                leftRects.add(rect);
            }
            else {
                rightRects.add(rect);
            }
        }

        RTree.insertDirectly(leftId, leftRects);
        RTree.insertDirectly(rightId, rightRects);

        node = RTree.readNode(nodeId);

        // Reseteamos los hijos y agregamos los 2 nuevos nodos
        node.resetChildren();
        node.resetRectangles();
        RTree.writeNode(node);

        // Guardamos en disco el nodo y los hijos
        //y el padre

        Rectangle2D leftMBR = (Rectangle2D) RTree.readNode(leftId).getMBR().clone();
        Rectangle2D rightMBR = (Rectangle2D) RTree.readNode(rightId).getMBR().clone();

        if (fatherId != nodeId){
            RTree.addChild(fatherId, new int[]{leftId, rightId}, new Rectangle2D[]{leftMBR, rightMBR});
            RTree.deleteNode(nodeId, fatherId);
        }
        else {
            RTree.addChild(nodeId, new int[]{leftId, rightId}, new Rectangle2D[]{leftMBR, rightMBR});
        }
    }
    public String toString() {
        return "LinearSplit";
    }
}
