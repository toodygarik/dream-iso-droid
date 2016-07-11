package hu.garik.android.game.engine.collition;

import android.util.Log;

import java.util.ArrayList;
import java.util.ListIterator;

import javax.microedition.khronos.opengles.GL10;

import hu.garik.android.game.engine.IsoSprite;
import hu.garik.android.game.engine.Vec3;

public class Octree {

	public static int MAX_DEPTH = 4;
	public static final int MIN_SPRITES_PER_OCTREE = 3;
	public static final int MAX_SPRITES_PER_OCTREE = 6; //6
	
	private static OnCollisionListener listener;
	
	private Vec3 corner1, corner2, center;
	private Octree[][][] children; 
	
	private boolean hasChildren;
	private ArrayList<IsoSprite> sprites;
	// stores the dirty nodes where something moved
	private static ArrayList<Octree> dirtyNodes;
	private int depth;
	protected int numSprites;
	
	
	public Octree(Vec3 corner1, Vec3 corner2, int depth) {
		this.corner1 = corner1;
		this.corner2 = corner2;
		center = new Vec3( (corner1.x + corner2.x) / 2, (corner1.y + corner2.y) / 2, (corner1.z + corner2.z) / 2);
		
		this.depth = depth;
		children = new Octree[2][2][2];
		sprites = new ArrayList<IsoSprite>();
		hasChildren = false;
		numSprites = 0;
		
		dirtyNodes = new ArrayList<Octree>();
	}
	
	
	
	private void fileSprite(IsoSprite sprite, Vec3 corner1, Vec3 corner2, boolean addSprite) {
		//Log.d("Octrre", "fileSprite");
		//Figure out in which child(ren) the ball belongs
        for(int x = 0; x < 2; x++) {
            if (x == 0) {
                if (corner1.x > center.x) {
                    continue;
                }
            }
            else if (corner2.x < center.x) {
                continue;
            }
            
            for(int y = 0; y < 2; y++) {
                if (y == 0) {
                    if (corner1.y> center.y) {
                        continue;
                    }
                }
                else if (corner2.y < center.y) {
                    continue;
                }
                
                for(int z = 0; z < 2; z++) {
                    if (z == 0) {
                        if (corner1.z > center.z) {
                            continue;
                        }
                    }
                    else if (corner2.z < center.z) {
                        continue;
                    }
                    
                    //Add or remove the ball
                    if (addSprite) {
                        children[x][y][z].add(sprite);
                    }
                    else {
                        children[x][y][z].remove(sprite);
                    }
                }
            }
        }
	}
	
	//Creates children of this, and moves the balls in this to the children
    private void divideTree() {
    	//Log.d("Octrre", "divideTree");
        for(int x = 0; x < 2; x++) {
        	float minX;
        	float maxX;
            if (x == 0) {
                minX = corner1.x;
                maxX = center.x;
            }
            else {
                minX = center.x;
                maxX = corner2.x;
            }
            
            for(int y = 0; y < 2; y++) {
            	float minY;
            	float maxY;
                if (y == 0) {
                    minY = corner1.y;
                    maxY = center.y;
                }
                else {
                    minY = center.y;
                    maxY = corner2.y;
                }
                
                for(int z = 0; z < 2; z++) {
                	float minZ;
                	float maxZ;
                    if (z == 0) {
                        minZ = corner1.z;
                        maxZ = center.z;
                    }
                    else {
                        minZ = center.z;
                        maxZ = corner2.z;
                    }
                    
                    children[x][y][z] = new Octree(new Vec3(minX, minY, minZ),
                                                   new Vec3(maxX, maxY, maxZ),
                                                   depth + 1);
                }
            }
        }
        
        //Remove all balls from "balls" and add them to the new children
        ListIterator<IsoSprite> iter = sprites.listIterator();
        while(iter.hasNext()) {
        	IsoSprite s = iter.next();
        	fileSprite(s, s.getIMinCorner(), s.getIMaxCorner(), true);
        	iter.remove();
        }
        
        hasChildren = true;
    }
	
	public void remove(IsoSprite sprite) {
		remove(sprite, sprite.getIMinCorner(), sprite.getIMaxCorner());
	}

	//Removes the specified ball at the indicated position
    private void remove(IsoSprite sprite, Vec3 corner1, Vec3 corner2) {
        numSprites--;
        
        if (hasChildren && numSprites < MIN_SPRITES_PER_OCTREE) {
            unDevide();
        }
        
        if (hasChildren) {
            fileSprite(sprite, corner1, corner2, false);
        }
        else {
            if( ! sprites.remove(sprite) ) {
            	Log.d("octree", "sprites: "+sprites);
            }
        }
    }
    
    //Destroys the children of this, and moves all balls in its descendants
    //to the "balls" set
    private void unDevide() {
        //Move all balls in descendants of this to the "balls" set
    	collectSprites(sprites);
        
        for(int x = 0; x < 2; x++) {
            for(int y = 0; y < 2; y++) {
                for(int z = 0; z < 2; z++) {
                    children[x][y][z] = null;
                }
            }
        }
        
        hasChildren = false;
    }

    private void collectSprites(ArrayList<IsoSprite> sprites) {
    	
    	if (hasChildren) {
            for(int x = 0; x < 2; x++) {
                for(int y = 0; y < 2; y++) {
                    for(int z = 0; z < 2; z++) {
                        children[x][y][z].collectSprites(sprites);
                    }
                }
            }
        }
        else {

	    	//Add all sprites
	    	ListIterator<IsoSprite> iter = this.sprites.listIterator();
	    	while(iter.hasNext()) {
	    		sprites.add(iter.next());
	    		iter.remove();
	    	}
        		
        }
    }

	public void add(IsoSprite sprite) {
		numSprites++;
		
		if (!hasChildren && depth < MAX_DEPTH &&
				numSprites > MAX_SPRITES_PER_OCTREE) {
		    divideTree();
		}
		
		if (hasChildren) {
		    fileSprite(sprite, sprite.getIMinCorner(), sprite.getIMaxCorner(), true);
		}
		else {
		//	Log.d("Collision ADD to dirty: ", "spriteID: "+sprite);
			sprites.add(sprite);
			
			// add this to the dirty nodes 
	    	if(!dirtyNodes.contains(this))
	    		dirtyNodes.add(this);
		}
	}
	
	//Changes the position of a ball in this from oldPos to ball->pos
    public void moveSprite(IsoSprite sprite) {
//        long start, end;
        
//       start = System.currentTimeMillis();
    	
        //FIXME valami gebasz van, mintha nem torlne ki mindig
    	remove(sprite, sprite.getLastIMinCorner(), sprite.getLastIMaxCorner());
    	add(sprite);
    	
    	
//    	end = System.currentTimeMillis();
//    	Log.d("Octree.Move", "time: "+(end-start)+"ms");
    }

    // check collisions only the dirty nodes
    protected void checkDirtyNodesOnly() {
    	
    	//Log.d("CHEK DIRTY", "Dirty number: "+dirtyNodes.size());

    	ListIterator<Octree> octreeIter = dirtyNodes.listIterator();
    	
    	while(octreeIter.hasNext()) {
    		paringCollitionTest(octreeIter.next().sprites);
    	}
    	
    	/*
    	for(int i=0; i<dirtyNodes.size(); i++)
    		paringCollitionTest(dirtyNodes.get(i).sprites);
    	*/
    	
    	// clear dirty nodes
    	dirtyNodes.clear();
    }
    
    //Adds potential ball-ball collisions to the specified set
    protected void potentialCollisionsOnFullTree() {
    	
        if (hasChildren) {
            for(int x = 0; x < 2; x++) {
                for(int y = 0; y < 2; y++) {
                    for(int z = 0; z < 2; z++) {
                        children[x][y][z].potentialCollisionsOnFullTree();
                    }
                }
            }
        }
        else {
        //	Log.d("potentialCollitions", "dpeth: "+depth+" sprites: "+sprites.size() +"("+corner1.x+";"+corner1.y+";"+corner1.z+"-"+corner2.x+";"+corner2.y+";"+corner2.z+")");
        	paringCollitionTest(this.sprites);
        	
        }
    }
    
    
    private void paringCollitionTest(ArrayList<IsoSprite> sprites) {
    	// check all pairs
    	for(int i=0; i<sprites.size(); i++) 
    		for(int j=i+1; j<sprites.size(); j++) {
    			/*
    			CollisionPair pair = new CollisionPair();
    			pair.sprite1 = sprites.get(i);
    			pair.sprite2 = sprites.get(j);
    			
    			collisions.add(pair);
    			
    			//Log.d("potentialCollitions", "Pair: "+pair);
    			 */
    			IsoSprite sprite1 = sprites.get(i);
    			IsoSprite sprite2 = sprites.get(j);
    			int collisionGroupMask = testCollision(sprite1, sprite2);
    			if(collisionGroupMask > 0) {
    				// call each Sprite collision handler
    	    		sprite1.onCollision(sprite2, collisionGroupMask);
    	    		sprite2.onCollision(sprite1, collisionGroupMask);
    	    		
    	    	//	Log.d("Collition Pairs", "pair: "+sprite1+" - "+sprite2);
    	    		
    	    		// call the listener if it has 
    	    		if(listener != null)
    	    			listener.handleCollision(sprite1, sprite2, collisionGroupMask);
    			}
    		}
    }

	public void debugDrow(GL10 gl) {
		if (hasChildren) {
            for(int x = 0; x < 2; x++) {
                for(int y = 0; y < 2; y++) {
                    for(int z = 0; z < 2; z++) {
                    	children[x][y][z].debugDrow(gl);
                    }
                }
            }
        }
        else {

	    	
        		
        }
	}
	
	public Vec3 getMinCorner() { return corner1; }
	public Vec3 getMaxCorner() { return corner2; }

	//Returns whether two sprite are colliding
	private int testCollision(IsoSprite s1, IsoSprite s2) {
	/*	Log.d("Coll: "+s1+"-"+s2, "s1.getImaxX("+s1.getImaxX()+") >= s2.getIminX("+s2.getIminX()+") ");
		Log.d("Coll: "+s1+"-"+s2, "s1.getIminX("+s1.getIminX()+") <= s2.getImaxX("+s2.getImaxX()+")");	
		Log.d("Coll: "+s1+"-"+s2, "s1.getImaxY("+s1.getImaxY()+") >= s2.getIminY("+s2.getIminY()+")");
		Log.d("Coll: "+s1+"-"+s2, "s1.getIminY("+s1.getIminY()+") <= s2.getImaxY("+s2.getImaxY()+")");
		Log.d("Coll: "+s1+"-"+s2, "s1.getImaxZ("+s1.getImaxZ()+") >= s2.getIminZ("+s2.getIminZ()+") ");
		Log.d("Coll: "+s1+"-"+s2, "s1.getIminZ("+s1.getIminZ()+") <= s2.getImaxZ("+s2.getImaxZ()+")");
		Log.d("Coll: "+s1+"-"+s2, "---------------------------");
	*/	
		
//		long start, end;
		
//		start = System.currentTimeMillis();
		
		// no moved element in the group
		if(!s1.hasMoved() && !s2.hasMoved())
			return 0;
		
		int collitionGroupMask = s1.getCollisionTypeBitmap() & s2.getCollisionTypeBitmap();
		
		// check has a same collision group
		if(collitionGroupMask == 0)
			return 0;
		
		/* If the checked Elements are overlapped */
		if(s1.getImaxX() >= s2.getIminX() && s1.getIminX() <= s2.getImaxX() &&		// X overlap
				s1.getImaxY() >= s2.getIminY() && s1.getIminY() <= s2.getImaxY() &&	// Y overlap
				s1.getImaxZ() >= s2.getIminZ() && s1.getIminZ() <= s2.getImaxZ()) {	// Z overlap
			
			return collitionGroupMask;
		}
		
//		end = System.currentTimeMillis();
		
//		Log.w("CollTest", "time: "+(end-start)+"ms");
		
	    return 0;
	}
	
	public void addOnCollisionListener(OnCollisionListener listener) {
		Octree.listener = listener;
	}
}
