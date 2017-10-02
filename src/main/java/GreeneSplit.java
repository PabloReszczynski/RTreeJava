import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;


public class GreeneSplit implements OverflowHeuristic, Serializable {

	@Override
	public void divideTree(RTree node) throws IOException, ClassNotFoundException {
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
        
        ArrayList<Rectangle2D> rectangles = node.getRectangles();
        Collections.sort(rectangles, new RectComp(separacionCorte));
        
        RTree leftNode = new RTree(r1, node.getM(), node.getHeuristic(), node.getFather());
        RTree rightNode = new RTree(r2, node.getM(), node.getHeuristic(), node.getFather());
        
        for (int i=0; i<=((node.getM()/2) -1); i++){
        	leftNode.insert(rectangles.get(i));
        }
        for (int i=(node.getM()/2); i<=(node.getM() + 1); i++){
        	rightNode.insert(rectangles.get(i));
        }
        
        
	}

}
