package hu.garik.android.game.engine;


/**
 * The Transformer class stores all method to transform points, plot to iso,
 * or iso to plot.<br>
 * This methods are static!
 *  
 * @author Garik
 * @version 0.1
 */
public class Transformer {

	/**
	 * Transform the ISO based coordinates to Plot (where have to draw) 
	 * coordinates.<br>
	 * (It transforms to Plot based from ISO based.)
	 * @param ix ISO x coordinate
	 * @param iy ISO y coordinate
	 * @return where have to draw the point on the screen.
	 */
	public static Point isoToPlot(float ix, float iy) {
		Point pos = new Point();
		/*
		pos.x = ix + iy;
		pos.y = (int)((iy - ix) * 0.5f);
		*/
		
		pos.x = (ix - iy) * 0.5f;
		pos.y = (ix + iy) * 0.25f;
		
		return pos;
	}
	
	
	/**
	 * Transform the ISO based coordinates to Plot (where have to draw) 
	 * coordinates.<br>
	 * (It transforms to Plot based from ISO based.)
	 * @param ix ISO x coordinate
	 * @param iy ISO y coordinate
	 * @return where have to draw the point on the screen.
	 */
	public static Point isoToPlot(Point ipos) {
		return isoToPlot(ipos.x, ipos.y);
	}
	
	
	/**
	 * Transform the Plot based coordinates to ISO (what ISO point is here)
	 * coordinates.<br>
	 * (It transforms to ISO based from Plot based.)
	 * @param px Plot x coordinate
	 * @param py Plot y coordinate
	 * @return what ISO point is in this Plot coordinate.
	 */
	public static Point plotToIso(float px, float py) {
		Point pos = new Point();
		
		/*
		pos.y = py + px/2;
		pos.x = px - pos.y;
		*/
		
		pos.x = px + 2*py;
		pos.y = 2 * py - px;
		
		return pos;
	}
	
	
	/**
	 * Transform the Plot based coordinates to ISO (what ISO point is here)
	 * coordinates.<br>
	 * (It transforms to ISO based from Plot based.)
	 * @param px Plot x coordinate
	 * @param py Plot y coordinate
	 * @return what ISO point is in this Plot coordinate.
	 */
	public static Point plotToIso(Point ppos) {
		return plotToIso(ppos.x, ppos.y);
	}
	
	
	public static float getRealDegree(float x, float y) {
		double degree;
		
		double rad = Math.atan2(y, x);
		
		if(rad < 0)
			rad *= -1;
		else if(rad > 0)
			rad = 2*Math.PI - rad;
		
		degree = Math.toDegrees(rad);
		
		
		return (float) degree;
	}
	

	public static Vec3[] convertBoxDrawCornersToIsoCorners(Vec3 drawCorner1, Vec3 drawCorner2) {
		Vec3[] corners = new Vec3[2];
		
		Point p0,p1,p2,p3;
		
		p0 = Transformer.plotToIso(drawCorner1.x, drawCorner1.y);
		p1 = Transformer.plotToIso(drawCorner2.x, drawCorner1.y);
		p2 = Transformer.plotToIso(drawCorner2.x, drawCorner2.y);
		p3 = Transformer.plotToIso(drawCorner1.x, drawCorner2.y);
		
		corners[0] = new Vec3(p0.x, p1.y, drawCorner1.z);
		corners[1] = new Vec3(p2.x, p3.y, drawCorner2.z);
		
		return corners;
	}
	
	public static Vec3[] convertBoxIsoCornersToDrawCorners(Vec3 isoCorner1, Vec3 isoCorner2) {
		Vec3[] corners = new Vec3[2];
		
		Point p0,p1,p2,p3;
		
		p0 = Transformer.plotToIso(isoCorner1.x, isoCorner1.y);
		p1 = Transformer.plotToIso(isoCorner2.x, isoCorner1.y);
		p2 = Transformer.plotToIso(isoCorner2.x, isoCorner2.y);
		p3 = Transformer.plotToIso(isoCorner1.x, isoCorner2.y);
		
		corners[0] = new Vec3(p3.x, p0.y, isoCorner1.z);
		corners[1] = new Vec3(p1.x, p2.y, isoCorner2.z);
		
		return corners;
	}
}
