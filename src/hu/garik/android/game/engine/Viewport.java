package hu.garik.android.game.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;



public class Viewport {
	
	private ArrayList<Map> layers;
	
	private int x,y, width, height, world_width, world_height;
	private ArrayList<IsoSprite[]> orderedElements;
	protected ArrayList<IsoSprite> moved;
	
	protected int overLoadSize;
	/** Offsets from the last reload*/
	protected int xOffset, yOffset;
	
	
	public Viewport(int x,int y,int width,int height, int world_width, int world_height, float overLoadSizePercent) {
		layers = new ArrayList<Map>();
		orderedElements = new ArrayList<IsoSprite[]>();
		moved = new ArrayList<IsoSprite>();
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
		this.world_height = world_height;
		this.world_width = world_width;
		
		int maxSize = width;
		if(maxSize < height)
			maxSize = height;
		this.overLoadSize = (int) (maxSize * overLoadSizePercent);
	}
	
	
	public int addLayer(Map layer) {
		layers.add(layer);
		layer.setLayerIdx(layers.size()-1);
		orderedElements.add(new IsoSprite[0]);
		
		// returns the index of the added layer
		return layers.size()-1;
	}
	
	private void setAllLayerDirty() {
		for(int i=0; i<layers.size(); i++)
			layers.get(i).setDirty(true);
	}
	
	public ArrayList<Map> getLayers() { return layers; }
	
	public void move(int dx, int dy) {
		x += dx;
		if(x < 0) x = 0;
		if(x + width > world_width) x = world_width - width;
		
		y += dy;
		if(y < 0) y = 0;
		if(y + height > world_height) y = world_height - height;
		
		xOffset += dx;
		yOffset += dy;
		
		if(Math.abs(xOffset) >= overLoadSize - 10 ||
				Math.abs(yOffset) >= overLoadSize - 10)
			setAllLayerDirty();
	}
	
	public int getX() { return x; }
	public int getY() { return y; }
	
	public void addElement(IsoSprite e, int layerIdx) {
		Map layer = layers.get(layerIdx);
		layer.add(e);
		
		e.setViewport(this);
		e.setLayer(layer);
		e.updateZ();
		
		// adds to move list to draw it's discover radius on the fog at the start.
		if(e.getDiscoverRadius() > 0)
			moved.add(e);
	}
	
	/*
	public void addModel(Model m) {
		m.setViewport(this);
	}
	*/
	
	public boolean removeElement(IsoSprite e) {
		e.setLayer(null);
		
		return e.getLayer().remove(e);
	}
	
	
	public DrawElements getElementsInOrder() {
//		long start, end;
		
//		start = System.currentTimeMillis();
		
		int allsprite = 0;
		boolean wasDirty = false;
		for(int i=0; i<layers.size(); i++) {
			allsprite += layers.get(i).getAllSprites().length;
			if(layers.get(i).isDirty())
				wasDirty = true;
		}
		
		// if wasn't any new element in the viewport stop and return null;
		if(!wasDirty)
			return null;
		
		IsoSprite[] ordered = new IsoSprite[allsprite];
		int lastidx = 0;
		

		for(int i=0; i<layers.size(); i++) {
			if(layers.get(i).isDirty()) {
				IsoSprite[] txordered = sortCollectedElementsByTexture(layers.get(i).getArea(x-overLoadSize, y-overLoadSize, x+width+(2*overLoadSize), y+height+(2*overLoadSize)));
				orderedElements.set(i, txordered);
				layers.get(i).setDirty(false);
				wasDirty = true;
			}
			
			System.arraycopy(orderedElements.get(i), 0, ordered, lastidx, orderedElements.get(i).length);
			lastidx += orderedElements.get(i).length;
		}
		
		
		// collect blocks
		DrawElements elements = new DrawElements();
		elements.addOrderedElements(ordered);
		elements.wasDirty = wasDirty;
		
		
		
//		end = System.currentTimeMillis();
//		System.out.println("time: "+ (end-start) +"ms");
		
		
		return elements;
	}
	
	protected static IsoSprite[] sortCollectedElementsByTexture(IsoSprite[] t) {
		
		Arrays.sort(t);
		
		return t;
	}
	
	
	
	public IsoSprite[] getAllSprite(int layerIdx) {
		if(layerIdx < layers.size())
			return layers.get(layerIdx).getAllSprites();
		
		return null;
	}
	
	public void preOrderAllLayer() {
		ListIterator<Map> miter = layers.listIterator();
		while(miter.hasNext()) {
			miter.next().preOrderMap();
		}
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public void addToMovedList(IsoSprite movedSprite) {
		moved.add(movedSprite);
	}
	

	protected void setOffsetZero() {
		xOffset = 0;
		yOffset = 0;
	}
}

class DrawElements {
	public IsoSprite[] sprites;
	public ArrayList<Integer> blockEnds = new ArrayList<Integer>();
	public boolean wasDirty = false;
	
	public void addOrderedElements(IsoSprite[] elements) {
		this.sprites = elements;
		generateBlocks();
	}
	
	private void generateBlocks() {
		blockEnds = new ArrayList<Integer>();
		
		if(sprites == null || sprites.length == 0)
			return;
		
		String texture = sprites[0].getTexture();
		
		for(int i=1; i<sprites.length; i++) { 
			if(sprites[i] == null) {
				blockEnds.add(i);
				break;
			}
			
			if(!sprites[i].getTexture().equals(texture)) {
				blockEnds.add(i);
				texture = sprites[i].getTexture();
			}
		}
	}
}

