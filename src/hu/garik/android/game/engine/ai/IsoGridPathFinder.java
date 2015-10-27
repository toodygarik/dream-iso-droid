package hu.garik.android.game.engine.ai;

import hu.garik.android.game.engine.IsoSprite;
import hu.garik.android.game.engine.LineDrawer;
import hu.garik.android.game.engine.Point;
import hu.garik.android.game.engine.Transformer;
import hu.garik.android.game.engine.Vec3;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL11;


public class IsoGridPathFinder implements IPathFinder {

	private AGrid[][] grid;
	private int gridXLength, gridYLength;
	private float worldWidth, worldHeight;
	private int gridSize;

	private Vec3 coordOffset;
	
	
	public IsoGridPathFinder(int worldDrawWidth,int worldDrawHeight,int gridSize) {
		Vec3[] corners = Transformer.convertBoxDrawCornersToIsoCorners(new Vec3(0,0,0), new Vec3(worldDrawWidth, worldDrawHeight, 0));
		
		this.worldWidth = corners[1].x;
		this.worldHeight = corners[1].y;
		this.gridSize = gridSize;
		this.coordOffset = corners[0];
		createGrid(corners[0]);
	}
	
	
	private void createGrid(Vec3 coordOffset) {
		gridXLength = Math.round(((worldWidth - coordOffset.x) / gridSize)) + 1;
		gridYLength = Math.round(((worldHeight - coordOffset.y) / gridSize)) + 1;
		
		grid = new AGrid[gridYLength][gridXLength];
		
		for(int i=0; i<gridYLength; i++)
			for(int j=0; j<gridXLength; j++)
				grid[i][j] = new AGrid(j,i);
	}
	
	
	@SuppressWarnings("unused")
	private void clearGridData() {
		for(int i=0; i<gridYLength; i++)
			for(int j=0; j<gridXLength; j++)
				grid[i][j].clear();
	}
	
	@Override
	public float getWorldIsoHeight() {return worldHeight;}
	@Override
	public float getWorldIsoWidth() {return worldWidth;}
	
	@Override
	public void addObstacle(IsoSprite obstacle) {
		int startX, startY, endX, endY;
		
		startX = (int)((obstacle.getIminX() - coordOffset.x) / gridSize);
		startY = (int)((obstacle.getIminY() - coordOffset.y) / gridSize);
		endX = (int)((obstacle.getImaxX() - coordOffset.x) / gridSize);
		endY = (int)((obstacle.getImaxY() - coordOffset.y) / gridSize);
		
		
		for(int i=startY; i<=endY; i++)
			for(int j=startX; j<=endX; j++)
				if(i >= 0 && j >= 0 && i < gridYLength && j < gridXLength)
					grid[i][j].isBlocked = true;
	}

	
	@Override
	public void removeObstacle(IsoSprite obstacle) {
		int startX, startY, endX, endY;
		
		startX = (int)((obstacle.getIminX() - coordOffset.x) / gridSize);
		startY = (int)((obstacle.getIminY() - coordOffset.y) / gridSize);
		endX = (int)((obstacle.getImaxX() - coordOffset.x) / gridSize);
		endY = (int)((obstacle.getImaxY() - coordOffset.y) / gridSize);

		for(int i=startY; i<=endY; i++)
			for(int j=startX; j<=endX; j++)
				if(i >= 0 && j >= 0 && i < gridYLength && j < gridXLength)
					grid[i][j].isBlocked = false;
	}
	
	@Override
	public Vec3[] getPath(IsoSprite mover, int toX, int toY) {
		// get start
		Vec3 moverCenter = mover.getCenter();
		Vec3 start = new Vec3();
		start.x = Math.round((moverCenter.x - coordOffset.x) / gridSize);
		start.y = Math.round((moverCenter.y - coordOffset.y) / gridSize);
		
		// get to position in grid
		Point toIsoPos = Transformer.plotToIso(toX, toY);
		Vec3 toPos = new Vec3();
		toPos.x = Math.round((toIsoPos.x - coordOffset.x) / gridSize);
		toPos.y = Math.round((toIsoPos.y - coordOffset.y) / gridSize);
		
		
		if(toPos.x < 0 || toPos.y < 0)
			return null;

		Vec3[] path = aStar(start, (int)toPos.x, (int)toPos.y);
		
		return path;
	}


	private Vec3[] aStar(Vec3 start, int toX, int toY) {
		// if the target is blocked won't do anything
		if(grid[toY][toX].isBlocked)
			return null;
		
		ArrayList<Vec3> nodes = new ArrayList<Vec3>();
		ArrayList<AGrid> openlist = new ArrayList<AGrid>();
		ArrayList<AGrid> closelist = new ArrayList<AGrid>();
		
		// 1.
		grid[(int) start.y][(int) start.x].clear();
		openlist.add(grid[(int) start.y][(int) start.x]);
		
		// 2.
		boolean find = false;
		while(!find) {
			
			// if empty and we didn't find the goal we have no path
			if(openlist.size() == 0)
				break;
			
			// find less weight
			AGrid lessWeight = openlist.get(0);
			for(int i=1; i<openlist.size(); i++)  
				if(openlist.get(i).getWeight() < lessWeight.getWeight())
					lessWeight = openlist.get(i);
			
			// switch into close list
			openlist.remove(lessWeight);
			closelist.add(lessWeight);
			
			// is this the goal
			if(lessWeight.x == toX && lessWeight.y == toY) {
				find = true;
				break;
			}
			
			//adjacent grids
			for(int y=lessWeight.y - 1; y <= lessWeight.y + 1; y++) {
				if(y<0 || y >= gridYLength)
					continue;
				
				for(int x=lessWeight.x - 1; x <= lessWeight.x + 1; x++) {
					// the parent grid
					if(x == lessWeight.x && y == lessWeight.y)
						continue;

					if(x<0 || x >= gridXLength)
						continue;
					
					if(grid[y][x].isBlocked || closelist.contains(grid[y][x]))
						continue;

					// calc cost
					int addMoveCost = 10;
					//sarokbont
					if( Math.abs(x - lessWeight.x) +  Math.abs(y - lessWeight.y) == 2) 
						addMoveCost = 14;
					
					int newMoveCost = lessWeight.moveCost + addMoveCost;
					
					
					if(openlist.contains(grid[y][x])) {
						
						if(grid[y][x].moveCost > newMoveCost) {
							grid[y][x].moveCost = newMoveCost;
							grid[y][x].fromX = lessWeight.x;
							grid[y][x].fromY = lessWeight.y;
						}
					
					}else  {
						openlist.add(grid[y][x]);
						
						grid[y][x].moveCost = newMoveCost;
						grid[y][x].fromX = lessWeight.x;
						grid[y][x].fromY = lessWeight.y;
						
						// set distance cost
						int xDist = Math.abs(toX - x);
						int yDist = Math.abs(toY - y);
						
						grid[y][x].distanceCost = (xDist + yDist) * 10;
					}
			
				}
			}
			
			
		}
		
		if(!find)
			return null;
		
		// create path
		AGrid cur = grid[toY][toX];
		while(cur.fromX != -1 && cur.fromY != -1){
			nodes.add(new Vec3(cur.x*gridSize + gridSize/2 + coordOffset.x , cur.y*gridSize + gridSize/2 + coordOffset.y , 0));
			
			cur = grid[cur.fromY][cur.fromX];
		}
		nodes.add(new Vec3(cur.x*gridSize + gridSize/2 + coordOffset.x , cur.y*gridSize + gridSize/2 + coordOffset.y , 0));
		
		
		
		return nodes.toArray(new Vec3[0]);
	}
	

	public void drawMap(GL11 gl) {
		for(int i=0; i<gridYLength; i++) {
			for(int j=0; j<gridXLength; j++)
				if(grid[i][j].isBlocked) {
					
					Point a = Transformer.isoToPlot(j*gridSize + coordOffset.x,	i*gridSize + coordOffset.y);
					Point b = Transformer.isoToPlot(j*gridSize + coordOffset.x + gridSize,	i*gridSize + coordOffset.y + gridSize);
					
					LineDrawer.drawLine(gl, a.x, a.y, b.x, b.y);
					
					a = Transformer.isoToPlot(j*gridSize + coordOffset.x + gridSize,	i*gridSize + coordOffset.y);
					b = Transformer.isoToPlot(j*gridSize + coordOffset.x,	i*gridSize + coordOffset.y + gridSize);
					
					LineDrawer.drawLine(gl, a.x, a.y, b.x, b.y);
				}
		}
	}
}
