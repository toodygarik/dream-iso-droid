package hu.garik.android.game.engine.ai;

public class AGrid {	
	
	public int x,y;
	
	public boolean isBlocked = false;
	public int moveCost = 0;
	public int distanceCost = 0;
	public int fromX = -1, fromY = -1;
	
	public AGrid(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void clear() {
		moveCost = 0;
		distanceCost = 0;
		
		fromX = -1;
		fromY = -1;
	}
	
	public int getWeight() { return moveCost + distanceCost; }
	
	public String toString() {return "A* Grid x: "+x+" y: "+y; }
}