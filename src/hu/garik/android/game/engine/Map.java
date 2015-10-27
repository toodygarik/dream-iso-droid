package hu.garik.android.game.engine;

import java.util.*;


/*WARNING
 * Érdemes lenne, a remove-nál figyelni, hogy a holes mennyire nagy és egy bizonyos szám után összehuzatni a teljes storert. 
 */

public class Map {

	private IsoSprite[] storer;
	private Stack<Integer> holes;	// the holes in the storer
	private int firstempty;	// pointer of the first empty place in storer
	private int attachlimit;	// if have to resize the storer it will make bigger with this variable
	
	private boolean isDirty;	// this shows that the map has any change before last 
	private int maxImgWidth, maxImgHeight; // the biggest image width and height which this map stores

	private int layerIdx;	// shows which layer is this
	
	public Map(int startcapacity) {
		this.attachlimit = 10;
		storer = new IsoSprite[startcapacity];
		holes = new Stack<Integer>();
		firstempty = 0;
		
		isDirty = true;
		maxImgWidth = -1;
		maxImgHeight = -1;
		
		layerIdx = -1;
	}

	public int getAttachlimit() { return attachlimit; }
	public void setAttachlimit(int attachlimit) { this.attachlimit = attachlimit; }

	public void add(IsoSprite e) {
		
		// set max image width and height
		if(e.getImageWidth() > maxImgWidth)
			maxImgWidth = e.getImageWidth();
		if(e.getImageHeight() > maxImgHeight)
			maxImgHeight = e.getImageHeight();
		
		// van lyuk
		if(!holes.empty()) {
			int hole = holes.pop();
			
			storer[hole] = e;
			e.setMapIndex(hole);
			
		}else {
			// nincs elég hely az új elemnek
			if(firstempty == storer.length) {
				IsoSprite[] newstorer = new IsoSprite[storer.length + attachlimit];
				System.arraycopy(storer, 0, newstorer, 0, storer.length);
				storer = newstorer;
			}
			
			e.setMapIndex(firstempty);
			storer[firstempty] = e;
			firstempty++;
		}
	}

	public boolean remove(IsoSprite e) {
		if(e.getMapIndex() == -1)
			return false;
		
		int delidx = e.getMapIndex();
		storer[delidx] = null;
		holes.push(delidx);
		
		return true;
	}
	
	public IsoSprite[] getArea(int x, int y, int width, int height) {
		Vector<IsoSprite> area = new Vector<IsoSprite>();
		
		// set the safety area
		x -= maxImgWidth;
		y -= maxImgHeight;
		width += maxImgWidth;
		height += maxImgHeight;
		
		IsoSprite cur;
		for(int i=0; i<this.firstempty; i++) {
			if(storer[i] == null ) // if it's a hole
				continue;
			
			cur = storer[i];
			// if overlapped by area
			if(cur.getDrawX() <= x + width && cur.getDrawX() + cur.getImageWidth() >= x &&
				cur.getDrawY() <= y + height && cur.getDrawY() + cur.getImageHeight() >= y) {
				
				area.add(cur);
			}
		}
		
		return area.toArray(new IsoSprite[0]);
	}
	
	public IsoSprite[] getAllSprites() { return storer; }
	
	public boolean isDirty() { return isDirty; }
	protected void setDirty(boolean isDirty) { this.isDirty = isDirty; };
	
	
	public void preOrderMap() {
		IsoSprite[] ordered = new IsoSprite[storer.length];
		int i=0;
		
		// collect elements
		for(int j=0; j < storer.length; j++) {
			if(storer[j] != null) {
				ordered[i] = storer[j];
				i++;
			}
		}
		
		// sort
		Arrays.sort(ordered, 0, i);
		storer = ordered;
		
		// set map variables
		holes = new Stack<Integer>();
		firstempty = i;
		
		isDirty = true;
	}
	
	public void setLayerIdx(int idx) {
		this.layerIdx = idx;
	}
	
	public int getLayerIdx() {
		return layerIdx;
	}
}
