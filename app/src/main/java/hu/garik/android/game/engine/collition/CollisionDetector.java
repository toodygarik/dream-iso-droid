package hu.garik.android.game.engine.collition;

import hu.garik.android.game.engine.IsoSprite;
import hu.garik.android.game.engine.Transformer;
import hu.garik.android.game.engine.Vec3;
import javax.microedition.khronos.opengles.GL10;
import android.util.Log;

public class CollisionDetector {

	
	private Octree octree;
	
	public CollisionDetector(Vec3 drawCorner1, Vec3 drawCorner2, Vec3 minBoxSize) {
		Vec3[] corners = Transformer.convertBoxDrawCornersToIsoCorners(drawCorner1, drawCorner2);
		octree = new Octree(corners[0], corners[1], 0);

		// Calculate the avarange optimum of the tree depth
		int x_depth = (int) ((Math.log10((corners[1].x - corners[0].x) / minBoxSize.x)) / Math.log10(2));
		int y_depth = (int) ((Math.log10((corners[1].y - corners[0].y) / minBoxSize.y)) / Math.log10(2));
		int z_depth = (int) ((Math.log10((corners[1].z - corners[0].z) / minBoxSize.z)) / Math.log10(2));
		
		Octree.MAX_DEPTH = Math.round(x_depth + y_depth + z_depth / 3.0f);
	}
	
	//Handles all collisions
	public void handleCollisions() {
//		long start, end;
		
//		start = System.currentTimeMillis();
		
	    //checklCollitionsOnFullTree();
	    checkCollisionOnlyDirtyNodes();
	    
//	    end = System.currentTimeMillis();
	    
//	    Log.d("Collision Detection", "time: "+(end-start)+"ms");
	}
	
	@SuppressWarnings("unused")
	private void checklCollitionsOnFullTree() {
		
		//Log.d("Octree", "all Sprites: "+octree.numSprites);
		// fast method
		octree.potentialCollisionsOnFullTree();
		
		 /*
	    //Slow method
	    for(int i = 0; i < sprites.size(); i++) {
	        for(int j = i + 1; j < sprites.size(); j++) {
	           	CollisionPair pair = new CollisionPair();
	           	pair.sprite1 = sprites.get(i);
	           	pair.sprite2 = sprites.get(j);
	           	
	           	collisions.add(pair);
	        }
	    }
	    */
	}
	
	private void checkCollisionOnlyDirtyNodes() {
		octree.checkDirtyNodesOnly();
	}
	
	
	public void addOnCollisionListener(OnCollisionListener listener) {
		octree.addOnCollisionListener(listener);
	}
	
	
	public void add(IsoSprite sprite) {
		//out of the collision world
		if(sprite.getImaxX() < octree.getMinCorner().x ||
				sprite.getImaxY() < octree.getMinCorner().y ||
				sprite.getImaxZ() < octree.getMinCorner().z) {
			Log.e("COLLISION", sprite+" Out of collition world");
			return;
		}
		
		if(sprite.getIminX() > octree.getMaxCorner().x || 
				sprite.getIminY() > octree.getMaxCorner().y ||
				sprite.getIminZ() > octree.getMaxCorner().z){
			Log.e("COLLISION", sprite+" Out of collition world");
			return;
		}
		
		
		octree.add(sprite);
		//sprites.add(sprite);
		sprite.setCollisionDetector(this);
	}
	
	public void remove(IsoSprite sprite) {
		octree.remove(sprite);
		sprite.setCollisionDetector(null);
	}
	
	public void moveSprite(IsoSprite sprite) {
		octree.moveSprite(sprite);
	}
	
	public void debugDraw(GL10 gl) {
		
		octree.debugDrow(gl);
		
	}

}
