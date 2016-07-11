package hu.garik.android.game.engine;

import android.content.Context;
import android.util.Log;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.microedition.khronos.opengles.GL11;

import hu.garik.android.game.engine.ai.IPathFinder;
import hu.garik.android.game.engine.collition.CollisionDetector;
import senseit.games.spellcraft.NativeCopier;

public abstract class GameScene implements OnKeyListener, OnTouchListener {

	protected Viewport vp;
	protected CollisionDetector collitionDetector;
	protected IPathFinder pathFinder;
	protected FogOfWar fow;
	
	private float[] vertsUvs = null;
	private int bufferIdx = -1;
	private FloatBuffer vbb = null;
	private float[] animUvs = null;
	/** The block ends of the VBO textures */
	private ArrayList<Integer> blockEnds = null;
	/** The texture IDs for the blocks */
	private ArrayList<String> blockTextures = new ArrayList<String>();
	
	private FloatBuffer mvbb = null;
	private float[] moveVertUvs = null;
	
	private float zoom = 1.0f;
	
	/** Size of the owner GLCanvas */
	protected int surfaceWidth, surfaceHeight;
	/** Timer for fog of war update limitation. */
	private Timer fogupdateTimer = new Timer();
	/** Flag to not clear moved sprite list at first time, to draw the discovery radius at start. */
	private boolean firstUpdate = true;
	
	
	
	public GameScene(Context context, int surfaceWidth, int surfaceHeight) {
		this.surfaceWidth = surfaceWidth;
		this.surfaceHeight = surfaceHeight;
		
		vp = createViewport();
		collitionDetector = createCollisionDetector();
		pathFinder = createPathFinder();
		fow = createFogOfWar();
		
		fogupdateTimer.start();
		
		System.gc();
		init(context);
	}
	
	
	public Viewport getViewport() {
		return vp;
	}
	
	public CollisionDetector getCollisionDetector() {
		return collitionDetector;
	}

	protected abstract Viewport createViewport();
	protected abstract CollisionDetector createCollisionDetector();
	protected abstract IPathFinder createPathFinder();
	protected abstract FogOfWar createFogOfWar();
	
	public abstract void init(Context context);
	
	public abstract void update(long elapsedTime);
	
	void autoUpdate(long elapsedTime ) {
		
		if(vp != null) {
			// set the moved list empty if it's not the first run
			if(!firstUpdate) 
				vp.moved.clear();
			firstUpdate = false;
			
			// walk through on layers and call autoUpdate
			ListIterator<Map> layerIter = vp.getLayers().listIterator();
			while(layerIter.hasNext()) {
				IsoSprite[] allSprite = layerIter.next().getAllSprites();
				
				for(int i=0; i < allSprite.length; i++) {
					if(allSprite[i] != null)
						allSprite[i].autoUpdate(elapsedTime);
				}
			}
		}
		
		update(elapsedTime);
	}
	
	public void render( GL11 gl) {

		
		// draw all on Viewport
		if(vp != null) {
			gl.glEnableClientState( GL11.GL_VERTEX_ARRAY );
			gl.glEnableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
			
			// set zoom level
			gl.glLoadIdentity();
			gl.glScalef(zoom, zoom, 1.0f);
			gl.glTranslatef(-vp.getX(), -vp.getY(), 0.0f);
			
			// collect drawing data
			DrawElements elements = vp.getElementsInOrder();
			
			if(elements != null) {
				Log.w("GameScene", "REGEN");
				blockEnds = elements.blockEnds;
				blockTextures.clear();
				generateVBO(gl, elements);
				vp.setOffsetZero();
			}else {
				
				gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, bufferIdx);
				changeMovedVBO(gl, bufferIdx);
			}
			
			//draw blocks
			gl.glTexCoordPointer( 2, GL11.GL_FLOAT, 32, 12);
			gl.glVertexPointer(3, GL11.GL_FLOAT, 32, 0); 
			
			int t = 0;
			int start = 0;
			for(Integer end : blockEnds) {
				//Log.e("GameSceen", "Block start: "+start +" end: "+end +" count: "+ (end-start));
				//Log.e("GameSceen", "Block text: "+blockTextures.get(t));
				TextureFactory.getInstance().bind(blockTextures.get(t));
				t++;
				
				// draw array
				gl.glDrawArrays(GL11.GL_TRIANGLES, start * 6, (end - start) * 6);
				
				start = end;
			}
			
			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
			
			gl.glDisableClientState( GL11.GL_VERTEX_ARRAY );
			gl.glDisableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
			
			// draw fog of war
			if(fow != null) {
				TextureFactory.getInstance().bind(fow.getFoxTextureName());
				// add moved sprite to the collection
				fow.addToMoved(vp.moved);
				
				// draw visible areas around moved update only one per sec
				if(fogupdateTimer.getTicks() >= 500) {
					fow.updateFog(gl);
					fogupdateTimer.start();
				}
				
				// draw full fog
				fow.drawFog(gl);
			}
		}
		
		//((IsoGridPathFinder)pathFinder).drawMap(gl);
		
	}

	private void generateVBO(GL11 gl, DrawElements elements) {

		// generate the vertex and uv structure array
		if(vertsUvs == null)
			vertsUvs = new float[ 10000 * 48 ];
		
		
		int verts_length = elements.sprites.length * 48;
		
		// collect block drawing data
		String blockTexture = "";
		int vi = 0;
		for(int i = 0; i <  elements.sprites.length; i++) {
			if(elements.sprites[i] != null) {
				IsoSprite s = elements.sprites[i];
				// set the VBO buffer index
				s.VBOIndex = vi;
				//Log.e("GameScene", s+ " VBOIdx: "+vi);
				
				animUvs = s.getCurrentAnimation().uvCoords.get(s.getCurrentAnimationFrame());
				
				// collect textures
				if(!s.getTexture().equals(blockTexture)) {
					blockTexture = s.getTexture();
					blockTextures.add(blockTexture);
				}
				
				vertsUvs[vi] = s.x;
				vertsUvs[vi + 1] = s.y;
				vertsUvs[vi + 2] = s.z;
				
				vertsUvs[vi + 3] = animUvs[0];
				vertsUvs[vi + 4] = animUvs[1];
				
				// 3 padding
				vertsUvs[vi + 5] = 0.0f;
				vertsUvs[vi + 6] = 0.0f;
				vertsUvs[vi + 7] = 0.0f;
				
				
				
				vertsUvs[vi + 8] = s.x + s.w;
				vertsUvs[vi + 9] = s.y;
				vertsUvs[vi + 10] = s.z;
				
				vertsUvs[vi + 11] = animUvs[2];
				vertsUvs[vi + 12] = animUvs[3];
				
				// 3 padding
				vertsUvs[vi + 13] = 0.0f;
				vertsUvs[vi + 14] = 0.0f;
				vertsUvs[vi + 15] = 0.0f;
				
				
				
				
				vertsUvs[vi + 16] = s.x + s.w;
				vertsUvs[vi + 17] = s.y + s.h;
				vertsUvs[vi + 18] = s.z;
				
				vertsUvs[vi + 19] = animUvs[4];
				vertsUvs[vi + 20] = animUvs[5];
				
				// 3 padding
				vertsUvs[vi + 21] = 0.0f;
				vertsUvs[vi + 22] = 0.0f;
				vertsUvs[vi + 23] = 0.0f;
				
				
				
				vertsUvs[vi + 24] = s.x;
				vertsUvs[vi + 25] = s.y;
				vertsUvs[vi + 26] = s.z;
				
				vertsUvs[vi + 27] = animUvs[6];
				vertsUvs[vi + 28] = animUvs[7];
				
				// 3 padding
				vertsUvs[vi + 29] = 0.0f;
				vertsUvs[vi + 30] = 0.0f;
				vertsUvs[vi + 31] = 0.0f;
				
				
				
				vertsUvs[vi + 32] = s.x + s.w;
				vertsUvs[vi + 33] = s.y + s.h;
				vertsUvs[vi + 34] = s.getZ();
				
				vertsUvs[vi + 35] = animUvs[8]; 
				vertsUvs[vi + 36] = animUvs[9];
				
				// 3 padding
				vertsUvs[vi + 37] = 0.0f;
				vertsUvs[vi + 38] = 0.0f;
				vertsUvs[vi + 39] = 0.0f;
				
				
				
				vertsUvs[vi + 40] = s.x;
				vertsUvs[vi + 41] = s.y + s.h;
				vertsUvs[vi + 42] = s.z;
				
				vertsUvs[vi + 43] = animUvs[10];
				vertsUvs[vi + 44] = animUvs[11];
				
				// 3 padding
				vertsUvs[vi + 45] = 0.0f;
				vertsUvs[vi + 46] = 0.0f;
				vertsUvs[vi + 47] = 0.0f;
				
				
				vi += 48;
			}
		}
		
		// generating buffer
		if(vbb == null) 
			vbb = ByteBuffer.allocateDirect(vertsUvs.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		NativeCopier.copy(vertsUvs, vbb, verts_length);
		vbb.position(0);
		
		if(bufferIdx == -1) {
			int[] index = new int[1];
			gl.glGenBuffers(1, index, 0);
			this.bufferIdx = index[0];
		}
		
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, bufferIdx);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, verts_length * 4, null, GL11.GL_DYNAMIC_DRAW);
		gl.glBufferSubData(GL11.GL_ARRAY_BUFFER, 0, verts_length * 4, vbb);
			
	}
	
	private void changeMovedVBO(GL11 gl, int bufferIdx) {
		if(vp == null || vp.moved.size() == 0)
			return;
		
		// create buffers at first
		if(moveVertUvs == null) 
			moveVertUvs = new float[48];
		
		if(mvbb == null) 
			mvbb = ByteBuffer.allocateDirect(moveVertUvs.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		for(IsoSprite s : vp.moved) {
			if(!s.overlapWithViewport())
				continue;
			
			animUvs = s.getCurrentAnimation().uvCoords.get(s.getCurrentAnimationFrame());
			
			moveVertUvs[0] = s.x;
			moveVertUvs[1] = s.y;
			moveVertUvs[2] = s.z;
			
			moveVertUvs[3] = animUvs[0];
			moveVertUvs[4] = animUvs[1];
			
			// 3 padding
			moveVertUvs[5] = 0.0f;
			moveVertUvs[6] = 0.0f;
			moveVertUvs[7] = 0.0f;
			
			
			
			moveVertUvs[8] = s.x + s.w ;
			moveVertUvs[9] = s.y;
			moveVertUvs[10] = s.z;
			
			moveVertUvs[11] = animUvs[2];
			moveVertUvs[12] = animUvs[3];
			
			// 3 padding
			moveVertUvs[13] = 0.0f;
			moveVertUvs[14] = 0.0f;
			moveVertUvs[15] = 0.0f;
			
			
			
			
			moveVertUvs[16] = s.x + s.w;
			moveVertUvs[17] = s.y + s.h;
			moveVertUvs[18] = s.z;
			
			moveVertUvs[19] = animUvs[4];
			moveVertUvs[20] = animUvs[5];
			
			// 3 padding
			moveVertUvs[21] = 0.0f;
			moveVertUvs[22] = 0.0f;
			moveVertUvs[23] = 0.0f;
			
			
			
			moveVertUvs[24] = s.x;
			moveVertUvs[25] = s.y;
			moveVertUvs[26] = s.z;
			
			moveVertUvs[27] = animUvs[6];
			moveVertUvs[28] = animUvs[7];
			
			// 3 padding
			moveVertUvs[29] = 0.0f;
			moveVertUvs[30] = 0.0f;
			moveVertUvs[31] = 0.0f;
			
			
			
			moveVertUvs[32] = s.x + s.w;
			moveVertUvs[33] = s.y + s.h;
			moveVertUvs[34] = s.getZ();
			
			moveVertUvs[35] = animUvs[8]; 
			moveVertUvs[36] = animUvs[9];
			
			// 3 padding
			moveVertUvs[37] = 0.0f;
			moveVertUvs[38] = 0.0f;
			moveVertUvs[39] = 0.0f;
			
			
			
			moveVertUvs[40] = s.x;
			moveVertUvs[41] = s.y + s.h;
			moveVertUvs[42] = s.z;
			
			moveVertUvs[43] = animUvs[10];
			moveVertUvs[44] = animUvs[11];
			
			// 3 padding
			moveVertUvs[45] = 0.0f;
			moveVertUvs[46] = 0.0f;
			moveVertUvs[47] = 0.0f;
			
			//move into BUFFER
			NativeCopier.copy(moveVertUvs, mvbb, moveVertUvs.length);
			gl.glBufferSubData(GL11.GL_ARRAY_BUFFER, s.VBOIndex * 4, moveVertUvs.length * 4, mvbb);
		}
	}
	
	public void checkCollision() {
		
		if(collitionDetector != null)
			collitionDetector.handleCollisions();
	}
	
	public void setZoom(float zoom) {
		this.zoom = zoom;
	}
	
	public float getZoom() {
		return zoom;
	}
	
}
