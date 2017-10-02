import java.awt.geom.Rectangle2D;
import java.util.Comparator;


public class RectComp implements Comparator<Rectangle2D> {
	private String separacionCorte;
	public RectComp(String separacionCorte) {
		this.separacionCorte=separacionCorte;
	}

	@Override
	public int compare(Rectangle2D o1, Rectangle2D o2) {
		double a;
		double b;
		
		//o1 -->a
		//o2-->b
		if (separacionCorte=="y"){
			//comparo eje y , abajo
			a = o1.getMinY();
			b = o2.getMinY();
			
		}
		else{
			//comparo eje x, lado izq
			a = o1.getMinX();
			b = o2.getMinX();
			
		}
		return a > b ? +1 : a < b ? -1 : 0;
	}

}
