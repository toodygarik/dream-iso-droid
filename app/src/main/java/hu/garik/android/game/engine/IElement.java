package hu.garik.android.game.engine;

import hu.garik.android.game.engine.collition.CollisionDetector;

public interface IElement {

	public String getName();
	public float getDrawX();
	public float getDrawY();
	public float getImaxX();
	public float getIminX();
	public float getImaxY();
	public float getIminY();
	public float getImaxZ();
	public float getIminZ();
	public Point getTileA();
	public Point getTileB();
	public int getASegment();
	public int getBodyHeight();
	public int getMapIndex();
	public int getImageWidth();
	public int getImageHeight();
	public int getDiscoverRadius();
	public boolean isShadow();
	public Viewport getViewport();
	public CollisionDetector getCollisionDetector();
	public Map getLayer();
	public int getCollisionTypeBitmap();

	public boolean isCompared();
	public boolean hasCollisionType(int type);
	
/*
	public void setID(int iD);
	public void setDrawX(int drawX);
	public void setDrawY(int drawY);
	public void setImaxX(int imaxX);
	public void setIminX(int iminX);
	public void setImaxY(int imaxY);
	public void setIminY(int iminY);
	public void setImaxZ(int imaxZ);
	public void setIminZ(int iminZ);
*/
	public void setName(String name);
	public void setMapIndex(int index);
	public void setCompared(boolean comp);
	public void setViewport(Viewport vp);
	public void setCollisionDetector(CollisionDetector cd);
	public void setLayer(Map layer);
	public void setShadow(boolean shadow);
	public void setDiscoverRadius(int radiusPx);
	public void addCollisionType(int type);
	
	public void removeCollisionType(int type);
	
	
	public IsoSprite initIsoParameters( int ax, int ay, int bx, int by, int startZ, int bodyHeight );
	
	public void moveInPlot(float plusPx, float plusPy, float plusPz);
	public void moveInIso(float plusIx, float plusIy, float plusIz);
}
