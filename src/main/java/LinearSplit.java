import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Serializable;

public class LinearSplit implements OverflowHeuristic, Serializable {

    public void divideTree(RTree node) throws IOException, ClassNotFoundException {
        /*
         * Para cada dimensión, se determina el rectángulo con el valor máximo del lado bajo. (ej. aquel cuyo lado
         * izquierdo está más a la derecha) y aquel con el valor mínimo del lado alto (ej. aquel cuyo lado derecho
         * está más a la izquierda). Se almacena la separación entre estos lados.
         */

        // Para X

        System.out.println("Spliting tree " + node.getId());

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
        RTree leftNode = new RTree(r1, node.getM(), node.getHeuristic(), node.getFather());
        RTree rightNode = new RTree(r2, node.getM(), node.getHeuristic(), node.getFather());

        // Por cada otro nodo en este nodo se agrega a r1 o a r2 dependiendo cual hace crecer el area menos.
        for (int i = 0; i < node.getRectangles().size(); i++) {
            Rectangle2D rect = node.getRectangles().get(i);
            //int id = node.getChildren().get(i);
            Rectangle2D candidate1 = r1.createUnion(rect);
            Rectangle2D candidate2 = r2.createUnion(rect);

            double area1 = candidate1.getWidth() * candidate1.getHeight();
            double area2 = candidate2.getWidth() * candidate2.getHeight();

            if (area1 < area2) {
                leftNode.insert(rect);
            }
            else {
                rightNode.insert(rect);
            }
        }

        // Reseteamos los hijos y agregamos los 2 nuevos nodos
        node.resetChildren();
        node.resetRectangles();
        node.insert(leftNode.getMBR());
        node.insert(rightNode.getMBR());

        // Guardamos en disco el nodo y los hijos
        //y el padre

        if (node.getFather() != node)
            RTree.deleteNode(node);
        RTree.writeNode(leftNode);
        RTree.writeNode(rightNode);
        RTree.writeNode(node.getFather());
    }
}
