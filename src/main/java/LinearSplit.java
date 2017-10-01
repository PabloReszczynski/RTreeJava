import java.awt.*;
import java.awt.geom.Rectangle2D;

public class LinearSplit extends OverflowHeuristic {
    public LinearSplit(RTree node) {
        super(node);
    }

    @Override
    public void divideTree() {
        /*
         * Para cada dimensión, se determina el rectángulo con el valor máximo del lado bajo. (ej. aquel cuyo lado
         * izquierdo está más a la derecha) y aquel con el valor mínimo del lado alto (ej. aquel cuyo lado derecho
         * está más a la izquierda). Se almacena la separación entre estos lados.
         */

        // Para X
        Rectangle2D r1_x = node.rectangles.get(0);
        Rectangle2D r2_x = node.rectangles.get(node.rectangles.size() - 1);
        for (Rectangle2D rect : node.rectangles) {
            if (rect.getMinX() < r1_x.getMinX()) {
                r1_x = rect;
            }
            else if (rect.getMaxX() > r2_x.getMaxX()) {
                r2_x = rect;
            }
        }
        double separation_x = r2_x.getMaxX() - r1_x.getMinX();

        // Para Y
        Rectangle2D r1_y = node.rectangles.get(0);
        Rectangle2D r2_y = node.rectangles.get(node.rectangles.size() - 1);
        for (Rectangle2D rect : node.rectangles) {
            if (rect.getMinY() < r1_y.getMinY()) {
                r1_y = rect;
            }
            else if (rect.getMaxY() > r2_y.getMaxY()) {
                r2_y = rect;
            }
        }
        double separation_y = r2_y.getMaxY() - r1_y.getMinY();

        // Normalizando
    }
}
