package hu.garik.android.game.engine;

import hu.garik.android.game.engine.ai.IPathFinder;
import hu.garik.android.game.engine.collition.CollisionDetector;
import android.util.Log;


public class IsoSprite extends AnimatedSprite implements IElement {

	/** Moving animations array contants. */
	public static final int MOVE_LEFT 		= 0;
	public static final int MOVE_LEFT_UP	= 1;
	public static final int MOVE_UP 		= 2;
	public static final int MOVE_RIGHT_UP 	= 3;
	public static final int MOVE_RIGHT 		= 4;
	public static final int MOVE_RIGHT_DOWN = 5;
	public static final int MOVE_DOWN 		= 6;
	public static final int MOVE_LEFT_DOWN 	= 7;
	
	
	
	/**Package level flag variable to Viewports sorting algorithms. */
	private boolean compared;
	
	/**The bounding box's X ISO coordinates. */
	private float ImaxX, IminX;	
	/**The bounding box's Y ISO coordinates. */
	private float ImaxY, IminY;	
	/**The bounding box's Z ISO coordinates. */
	private float ImaxZ, IminZ;
	/**Last position before move */
	private Vec3 lastIMinCorner, lastIMaxCorner;
	/** The tile base points in plot coordinates. */
	private Point tileA, tileB;
	/**The index of this element in the map storer. */
	private int mapIndex = -1;
	/** Viewport which contains the element. */
	private Viewport ownerVp;
	/** Collision detector which contains the element. */
	private CollisionDetector collDetector;
	/** The type bitmap of the collisions. */
	private int collisionTypes;
	/** The layer map which store this element. */
	private Map ownerLayer;
	/** Is this sprite a shadow. */
	private boolean shadow;
	/** A segment of the isometric element. */
	private int aSegment;	
	/** Height of the element in isometric. */
	private int bodyHeight;
	/** Identifying name of the element. */
	private String name;
	/** Debugging draw flag. */
	private boolean isDebugMode = false;
	
	/** Move to parameters. */
	/** Moving velocity in pixel / millisec. */
	private float moveVelocoty; 
	/** Distance remains to the target position. */
	private float distanceX, distanceY, distanceZ;
	/** Move animation keys. */
	private String[] moveAnimations;
	
	/** VBO buffer index */
	protected int VBOIndex;
	
	/** Path finding data */
	protected Vec3[] path;
	protected int pathIdx = -1;
	private float pathVelocity;
	private boolean stopAnimationAtMoveTo = true;
	/** Last moving direction. */
	private int lastDirection;
	/** Flag doesn't let start new pathfinding before the last finished. */
	private boolean doingPath = false;
	/** The units discover radius on fog of war*/
	private int discoverRadius = 0;
	
	
	
	public IsoSprite(String image, int x, int y) {
		super(image, x, y);
		tileA = new Point();
		tileB = new Point();
		collisionTypes = 0;
		moved = false;
	}
	
	public IsoSprite(String image) {
		this(image, 0, 0);
	}

	public IsoSprite(String image, boolean isShadow) {
		this(image);
		this.shadow = isShadow;
	}
	
	/* Copy constructor */
	public IsoSprite(IsoSprite original) {
		super(original.getTexture(), original.getX(), original.getY());
		this.setWidth(original.getWidth());
		this.setHeight(original.getHeight());
		this.name = original.getName();
		this.animations = original.animations;
		
		this.initIsoParameters((int)original.getTileA().x, (int)original.getTileA().y, 
				(int)original.getTileB().x, (int)original.getTileB().y, (int)original.getIminZ(), original.getBodyHeight());
	}
	
	@Override
	public float getDrawX() {	return this.getX(); }
	@Override
	public float getDrawY() {	return this.getY(); }
	@Override
	public float getImaxX() {	return this.ImaxX; }
	@Override
	public float getImaxY() {	return this.ImaxY; }
	@Override
	public float getImaxZ() {	return this.ImaxZ; }
	@Override
	public float getIminX() {	return this.IminX; }
	@Override
	public float getIminY() { return this.IminY; }
	@Override
	public float getIminZ() {	return this.IminZ; }
	@Override
	public Point getTileA() { return this.tileA; }
	@Override
	public Point getTileB() { return this.tileB; }
	@Override
	public int getMapIndex() { return this.mapIndex; }
	@Override
	public int getImageWidth() { return (int) this.getWidth(); }
	@Override
	public int getImageHeight() { return (int) this.getHeight(); }
	@Override
	public boolean isCompared() { return this.compared; }
	@Override
	public Viewport getViewport() { return this.ownerVp; }
	@Override
	public CollisionDetector getCollisionDetector() { return this.collDetector; }
	@Override
	public Map getLayer() { return this.ownerLayer; }
	@Override
	public boolean isShadow() { return shadow; }
	@Override
	public int getASegment() { return aSegment;	}
	@Override
	public int getBodyHeight() { return bodyHeight; }
	@Override
	public String getName() { return name; }
	@Override
	public int getCollisionTypeBitmap() { return collisionTypes; }
	@Override
	public int getDiscoverRadius() { return discoverRadius; }
	@Override
	public boolean hasCollisionType(int type) { return (collisionTypes & type) == type ? true : false; }
	
	public Vec3[] getPath() { return this.path; }
	
	public boolean isDebugMode() {return isDebugMode; }
	
	/*
	@Override
	public void setDrawX(int drawX) { this.setX((float)drawX);	}
	@Override
	public void setDrawY(int drawY) { this.setY((float)drawY);	}
	@Override
	public void setImaxX(int imaxX) { this.ImaxX = imaxX; }
	@Override
	public void setImaxY(int imaxY) { this.ImaxY = imaxY; }
	@Override
	public void setImaxZ(int imaxZ) { this.ImaxZ = imaxZ; }
	@Override
	public void setIminX(int iminX) { this.IminX = iminX; }
	@Override
	public void setIminY(int iminY) { this.IminY = iminY; }
	@Override
	public void setIminZ(int iminZ) { this.IminZ = iminZ; }
	*/
	@Override
	public void setName(String name) { this.name = name; }
	@Override
	public void setViewport(Viewport vp) { this.ownerVp = vp; }
	@Override
	public void setCollisionDetector(CollisionDetector cd) { this.collDetector = cd; }
	@Override
	public void setLayer(Map layer) { this.ownerLayer = layer; }
	@Override
	public void setMapIndex(int index) { this.mapIndex = index; }
	@Override
	public void setCompared(boolean comp) { this.compared = comp; }
	@Override
	public void setShadow(boolean shadow) { this.shadow = shadow; }
	@Override
	public void setDiscoverRadius(int radiusPx) { this.discoverRadius = radiusPx; }
	@Override
	public void addCollisionType(int type) { this.collisionTypes |= type; }
	
	@Override
	public void removeCollisionType(int type) {
		//TODO
		try {
			throw new Exception("NOT IMPLEMENTED JET!!! :)");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setDebugMode(boolean isDebugMode) {	this.isDebugMode = isDebugMode;	}

	public void setMoveAnimationNames(String[] names) { this.moveAnimations = names; }
	
	@Override
	public IsoSprite initIsoParameters( int ax, int ay, int bx, int by, int startZ, int bodyHeight) {
	
		tileA = new Point();
		tileB = new Point();
		
		this.tileA.x = ax;
		this.tileA.y = ay;
		this.tileB.x = bx;
		this.tileB.y = by;
		
		this.bodyHeight = bodyHeight;
		
		this.IminZ = startZ;
		this.ImaxZ = startZ + bodyHeight;
		
		Point p = new Point(this.getX()+ax, this.getY() + ay);
		p = Transformer.plotToIso(p);
		this.IminX = p.x;
		this.ImaxY = p.y;
		
		p = new Point(this.getX()+ bx, this.getY() + by);
		p = Transformer.plotToIso(p);
		this.ImaxX = p.x;
		this.IminY = p.y;
		
		lastIMinCorner = getIMinCorner();
		lastIMaxCorner = getIMaxCorner();
		
		//TODO GL Z általában redundáns mert az vp.addElement is meghívja a layer index miatt
		//updateZ();
		
		return this;
	}
	
	
	/**
	 * Z must be between -0.1 and -1.1. -0.1 is near
	 */
	protected void updateZ() {
		/*
		Point downDot = Transformer.isoToPlot(this.ImaxX, this.ImaxY);
		
		this.setZ(-100.0f + (downDot.y / 10000.0f));
		*/
		
		float dist = ImaxX + IminX + ImaxY + IminY + IminZ /*+ ImaxZ*/;
		/*
		float dist = (float) Math.sqrt(ImaxX + ImaxY + IminZ);
		float dist2 = (float) Math.sqrt(IminX + IminY + IminZ);
		*/
		
		float dividor = 900000000.0f / 99.0f;
		
		float Zplus = (dist) / dividor;
		
		// layer index parting (true layering)
		int layerIdx = 1;
		if(ownerLayer != null)
			layerIdx = ownerLayer.getLayerIdx() + 1;
		
		this.setZ((-0.1f + Zplus) - (1.1f - (layerIdx * 0.1f)));
	
		//Log.d("Z", "z: "+getZ() +" layeridx: "+ownerLayer);
	}
	
	
/*************************************** MOVE **********************************/	
	@Override
	public void moveInIso(float plusIx, float plusIy, float plusIz) {
		boolean beforMoveOverlap = ownerVp != null ? overlapWithViewport() : true;
		
		lastIMinCorner = getIMinCorner();
		lastIMaxCorner = getIMaxCorner();
		
		this.ImaxX += plusIx;
		this.IminX += plusIx;
		
		this.ImaxY += plusIy;
		this.IminY += plusIy;
		
		this.ImaxZ += plusIz;
		this.IminZ += plusIz;
		
		/*
		Point PMoveVektor = Transformer.isoToPlot(plusIx, plusIy);
		this.setX(this.getY() + PMoveVektor.x);
		this.setY(this.getY() + PMoveVektor.y - plusIz);
		*/

		Point drawPoint = Transformer.isoToPlot(IminX, IminY);
		this.setX(drawPoint.x - tileA.x);
		this.setY(drawPoint.y - tileA.y - IminZ);
		
		// if this sprite is move in or out from viewport
		if(ownerLayer != null && (overlapWithViewport() != beforMoveOverlap))
			ownerLayer.setDirty(true);
		
		//GL Z
		updateZ();
		
		// move sprite at the collision world
		if(collDetector != null)
			collDetector.moveSprite(this);
		
		// set moved on VP
		moved = true;
		if(ownerVp != null)
			ownerVp.addToMovedList(this);
	}
	
	
	@Override
	public void moveInPlot(float plusPx, float plusPy, float plusPz) {
		boolean beforMoveOverlap = ownerVp != null ? overlapWithViewport() : true;
			
		lastIMinCorner = getIMinCorner();
		lastIMaxCorner = getIMaxCorner();
		
		this.setX(this.getX() + plusPx);
		this.setY(this.getY() + plusPy - plusPz);
		
		Point IMoveVektor = Transformer.plotToIso(plusPx, plusPy);		
		this.ImaxX += IMoveVektor.x;
		this.IminX += IMoveVektor.x;
		
		this.ImaxY += IMoveVektor.y;
		this.IminY += IMoveVektor.y;
		
		this.ImaxZ += plusPz;
		this.IminZ += plusPz;
		
		// if this sprite is move in or out from viewport
		if(ownerLayer != null && (overlapWithViewport() != beforMoveOverlap))
			ownerLayer.setDirty(true);
		
		//GL Z
		updateZ();
		
		// move sprite at the collision world
		if(collDetector != null)
			collDetector.moveSprite(this);
		
		// set moved on VP
		moved = true;
		if(ownerVp != null)
			ownerVp.addToMovedList(this);
	}
	
	public void moveTo(float drawX, float drawY, float z, float velocity) {
		Point p = Transformer.plotToIso(drawX, drawY);
		moveTo(p.x, p.y, z, velocity, true);
	}
	
	protected void moveTo(float isoX, float isoY, float isoZ, float velocity, boolean stopAnimationAtEnd) {
		moveVelocoty = velocity / 1000.0f;
		
		Vec3 center = getCenter();
		distanceX = isoX - center.x;
		distanceY = isoY - center.y;
		distanceZ = z - this.z;
		stopAnimationAtMoveTo = stopAnimationAtEnd;
		
		// automatic direction animation
		startAnimationByDegree(distanceX, distanceY);
	}
	
	public void movePathTo(final IPathFinder finder, final float drawX, final float drawY, float velocity) {
		if(finder == null)
			return;
		
		pathVelocity = velocity;
		if(!doingPath) {
			// path finding running in thread
			new Thread() { 
				public void run() {
					long start, end;
					start = System.currentTimeMillis();
					Log.i("IsoSprite", "Path find thread STARTED!");
					doingPath = true;
					IsoSprite.this.path = finder.getPath(IsoSprite.this, (int)drawX, (int)drawY);
					doingPath = false;
					end = System.currentTimeMillis();
					Log.i("IsoSprite", "Path find thread runs: "+(end-start)+"ms");
					
					if(path == null)
						return;
					IsoSprite.this.pathIdx = path.length-2;
	
				}
			}.start();
		}
	}
	
	private void startAnimationByDegree(float distanceX, float distanceY) {
		if(moveAnimations == null)
			return;
		
		float degree = Transformer.getRealDegree(distanceX, distanceY) - 45;
		if(degree < 0)
			degree = 360 + degree;
		int dir = -1;
		
		// choose animation by degree
		if(degree >= 22 && degree < 68) dir = MOVE_RIGHT_UP;
		else if(degree >=68  && degree < 112) dir = MOVE_UP;
		else if(degree >=112  && degree < 158) dir = MOVE_LEFT_UP;
		else if(degree >=158  && degree < 202) dir = MOVE_LEFT;
		else if(degree >=202  && degree < 248) dir = MOVE_LEFT_DOWN;
		else if(degree >=248  && degree < 292) dir = MOVE_DOWN;
		else if(degree >=292  && degree < 338) dir = MOVE_RIGHT_DOWN;
		else dir = MOVE_RIGHT;
		
		String anim = moveAnimations[dir];
		// if there's all animation it tries to load the next one
		if(anim == null || anim.equals(""))
			if(dir+1 < moveAnimations.length)
				anim = moveAnimations[dir+1];
			else
				anim = moveAnimations[0];
		
		// load animation
		if(stopAnimationAtMoveTo || !isAnimationRunning() || lastDirection != dir) {
			setAnimation(anim);
			setLoopAnimation(true);
			setAnimationFrame(0);
			startAnimation();
		}
		
		lastDirection = dir;
	}
	
	protected boolean overlapWithViewport() {
		if(x + w >= ownerVp.getX() && x  <= ownerVp.getX() + ownerVp.getWidth() &&
				y + h >= ownerVp.getY() && y <= ownerVp.getY() + ownerVp.getHeight())
			return true;
		
		return false;
	}
	
	public void onCollision(IsoSprite with, int collisionMask) {};
	
/****************** PREPARE DEFAULT MOVE METHODS ********************/
	@Override
	public void move(float dx, float dy) {
		//this.moveInPlot((int)dx, (int)dy, 0);
		this.moveInPlot(dx, dy, 0);
	}
	
	@Override
	public void moveX(float dx) {
		this.moveInPlot(dx, 0, 0);
	}
	
	@Override
	public void moveY(float dy) {
		this.moveInPlot(0, dy, 0);
	}
	
	/*
	@Override
	public void setX(float x) {
		int vpx = 0;
		if(ownerVp != null)
			vpx = ownerVp.getX();
		
		super.setX(x + vpx);
	}
	@Override
	public void setY(float y) {
		int vpy = 0;
		if(ownerVp != null)
			vpy = ownerVp.getY();
		
		super.setY(y + vpy);
	}
	*/
	
/******************************* DRAWER OVERRIDE ************************************/

	/*
	private void debugDraw( GL10 gl ) {
		
		// draw bounding box
		Point a,b,c,d,au,bu,cu,du;
		
		a = new Point(getIminX(), getIminY());
		b = new Point(getImaxX(), getIminY());
		c = new Point(getImaxX(), getImaxY());
		d = new Point(getIminX(), getImaxY());
		a = Transformer.isoToPlot(a);
		b = Transformer.isoToPlot(b);
		c = Transformer.isoToPlot(c);
		d = Transformer.isoToPlot(d);
		a.y -= getIminZ();
		b.y -= getIminZ();
		c.y -= getIminZ();
		d.y -= getIminZ();
		
		au = new Point(getIminX(), getIminY());
		bu = new Point(getImaxX(), getIminY());
		cu = new Point(getImaxX(), getImaxY());
		du = new Point(getIminX(), getImaxY());
		au = Transformer.isoToPlot(au);
		bu = Transformer.isoToPlot(bu);
		cu = Transformer.isoToPlot(cu);
		du = Transformer.isoToPlot(du);
		
		au.y -= getImaxZ();
		bu.y -= getImaxZ();
		cu.y -= getImaxZ();
		du.y -= getImaxZ();
		
		
		LineDrawer.drawLine(gl, a.x, a.y, -0.2f, b.x, b.y, -0.2f);
		LineDrawer.drawLine(gl, b.x, b.y, -0.2f, c.x, c.y, -0.2f);
		LineDrawer.drawLine(gl, c.x, c.y, -0.2f, d.x, d.y, -0.2f);
		LineDrawer.drawLine(gl, d.x, d.y, -0.2f, a.x, a.y, -0.2f);
		
		LineDrawer.drawLine(gl, au.x, au.y, -0.2f, bu.x, bu.y, -0.2f);
		LineDrawer.drawLine(gl, bu.x, bu.y, -0.2f, cu.x, cu.y, -0.2f);
		LineDrawer.drawLine(gl, cu.x, cu.y, -0.2f, du.x, du.y, -0.2f);
		LineDrawer.drawLine(gl, du.x, du.y, -0.2f, au.x, au.y, -0.2f);
		
		LineDrawer.drawLine(gl, a.x, a.y, -0.2f, au.x, au.y, -0.2f);
		LineDrawer.drawLine(gl, b.x, b.y, -0.2f, bu.x, bu.y, -0.2f);
		LineDrawer.drawLine(gl, c.x, c.y, -0.2f, cu.x, cu.y, -0.2f);
		LineDrawer.drawLine(gl, d.x, d.y, -0.2f, du.x, du.y, -0.2f);
	
	}
	*/
/********************************* AUTO UPDATE ********************************/
	
	@Override
	protected void autoUpdate(long elapsedTime) {
		super.autoUpdate(elapsedTime);
		
		pathMoveUpdate(elapsedTime);
		
		if(moveVelocoty != 0.0f) 
			moveToUpdate(elapsedTime);
	}
	
	private void moveToUpdate(long elapsedTime) {
		float move = elapsedTime * moveVelocoty;
		
		double dist = Math.sqrt(distanceX*distanceX + distanceY*distanceY + distanceZ*distanceZ);
		
		double step = dist / move;
		
		if(Math.abs(step) < 1.0f) {
			moveVelocoty = 0.0f;
			
			if(stopAnimationAtMoveTo) {
				setLoopAnimation(false);
				setAnimationFrame(0);
				stopAnimation();
			}
			
			moveInIso((float)distanceX, (float)distanceY, (float)distanceZ);
			return;
		}
		
		double moveX = distanceX / step;
		double moveY = distanceY / step;
		double moveZ = distanceZ / step;
		
		distanceX -= moveX;
		distanceY -= moveY;
		distanceZ -= moveZ;
		
		moveInIso((float)moveX, (float)moveY, (float)moveZ);
	}
	
	
	private void pathMoveUpdate(long elapsedTime) {
		if(pathIdx > -1 && path != null) {
			
			if(moveVelocoty == 0.0f) {
				Vec3 target = path[pathIdx];
				//Point p = Transformer.isoToPlot(target.x, target.y);
				
				moveTo(target.x, target.y, z, pathVelocity, false);
				pathIdx--;
			}
			
			if(pathIdx == -1) {
				path = null;
				
				setLoopAnimation(false);
				setAnimationFrame(0);
				stopAnimation();
			}
		}
	}
	
/************************************ OTHER ***********************************/
	@Override
	public String toString() {
		return name;
	}
	
	public Vec3 getIMinCorner() {
		return new Vec3(IminX, IminY, IminZ);
	}
	
	public Vec3 getIMaxCorner() {
		return new Vec3(ImaxX, ImaxY, ImaxZ);
	}
	
	public Vec3 getLastIMinCorner() {
		return lastIMinCorner;
	}
	
	public Vec3 getLastIMaxCorner() {
		return lastIMaxCorner;
	}
	
	public Vec3 getCenter() {
		return new Vec3(IminX + (ImaxX - IminX)/2, IminY + (ImaxY - IminY)/2, IminZ+(ImaxZ - IminZ)/2);
	}
}
