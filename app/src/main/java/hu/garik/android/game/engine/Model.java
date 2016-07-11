package hu.garik.android.game.engine;

public class Model {
/*
	private int x,y;
	private IsoSprite[] sprites;
	private int nextempty;
	
	
	public Model(int x, int y, int spritenumber) {
		this.x = x;
		this.y = y;
		sprites = new IsoSprite[spritenumber];
		nextempty = 0;
	}
	
	public boolean addSprite(int xoffset, int yoffset, IsoSprite sprite) {
		if(nextempty == sprites.length)
			return false;
		
		int xdif, ydif;
		
		xdif = (int) ((x+xoffset)-sprite.getDrawX());
		ydif = (int) ((y+yoffset)-sprite.getDrawY());
		sprite.moveInPlot(xdif, ydif, 0);
		sprites[nextempty] = sprite;
		nextempty++;
		
		return true;
	}
	
	public void setViewport(Viewport vp) {
		for(int i=0; i<nextempty; i++) {
			sprites[i].setViewport(vp);
			vp.addElement(sprites[i]);
		}
	}
	
	public void moveInPlot(int dx, int dy, int dz) {
		for(int i=0; i<nextempty; i++) {
			if(sprites[i].isShadow())
				sprites[i].moveInPlot(dx, dy, 0);
			else
				sprites[i].moveInPlot(dx, dy, dz);
		}
	}
	
	public void moveInIso(int dx, int dy, int dz) {
		for(int i=0; i<nextempty; i++) {
			if(sprites[i].isShadow())
				sprites[i].moveInPlot(dx, dy, 0);
			else
				sprites[i].moveInPlot(dx, dy, dz);
		}
	}
	
	public IsoSprite getSprite(int index) {
		if(index < nextempty)
			return sprites[index];
		else
			return null;
	}
	*/
}
