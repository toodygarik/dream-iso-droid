package hu.garik.android.game.engine;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL11;

import senseit.games.spellcraft.NativeCopier;

public class FogOfWar {

	private String fogTex;
	private int texWidth, texHeight;
	private int worldWidth, worldHeight;
	
	private float pixelScaleX, pixelScaleY;

	int[] pixels;
	private IntBuffer pxBuff;

	private ArrayList<IsoSprite> moved;
	
	// GL buffers
	private static float[] verts;
	private static FloatBuffer vertsBuff;
	
	private static short[] indices = {
		0, 1, 2,
		0, 2, 3
	};
	private static ShortBuffer indsBuff;
	
	private static float[] default_uvcoords = {
		0.0f, 0.0f,
		1.0f, 0.0f,
		1.0f, 1.0f,
		0.0f, 1.0f
	};
	public static FloatBuffer default_uvbuff;
	
	
	public FogOfWar(String fogTex, int texWidth, int texHeight, int worldWidth, int worldHeight) {
		this.fogTex = fogTex;
		this.texWidth = texWidth;
		this.texHeight = texHeight;
		this.worldWidth = worldWidth;
		this.worldHeight = worldHeight;
		
		pixelScaleX = (float)texWidth / (float)worldWidth;
		pixelScaleY = (float)texHeight / (float)worldHeight;
		
		pixels = new int[texWidth * texHeight];
		
		moved = new ArrayList<IsoSprite>();
		
		createBuffers();
	}
	
	private void createBuffers() {

		// pixels
		for(int i=0; i< (texHeight*texWidth); i++)
			pixels[i] = 0xFF000000;

		pxBuff = ByteBuffer.allocateDirect( texWidth*texHeight*4 ).order(ByteOrder.nativeOrder()).asIntBuffer();
		pxBuff.put(pixels);
		pxBuff.position(0);
		
		// vertex
		verts = new float[12];
		
		verts[0] = 0.0f;
		verts[1] = 0.0f;
		verts[2] = -1.0f;
		
		verts[3] = worldWidth;
		verts[4] = 0.0f;
		verts[5] = -1.0f;
		
		verts[6] = worldWidth;
		verts[7] = worldHeight;
		verts[8] = -1.0f;
		
		verts[9] = 0.0f;
		verts[10] = worldHeight;
		verts[11] = -1.0f;

		
		vertsBuff = ByteBuffer.allocateDirect( verts.length * 4 ).order(ByteOrder.nativeOrder()).asFloatBuffer();
		NativeCopier.copy(verts, vertsBuff, verts.length);
		vertsBuff.position(0);
		
		indsBuff = ByteBuffer.allocateDirect( indices.length * 2 ).order(ByteOrder.nativeOrder()).asShortBuffer();
		indsBuff.put(indices);
		indsBuff.position(0);
		
		default_uvbuff = ByteBuffer.allocateDirect( default_uvcoords.length * 4 ).order( ByteOrder.nativeOrder() ).asFloatBuffer();
		NativeCopier.copy(default_uvcoords, default_uvbuff, default_uvcoords.length);
		default_uvbuff.position( 0 );
	}
	
	
	public String getFoxTextureName() { return fogTex; }
	
	protected void drawVisibleArea(GL11 gl, int centX, int centY, int radiusPx) {
		int x0,y0, r;
		
		x0 = (int) (centX * pixelScaleX);
		y0 = (int) (centY * pixelScaleY);
		r = (int) (radiusPx * pixelScaleX);
		
		//Log.e("FOG", "x0= "+x0 + " y0="+y0+" r="+r);
		
		for (int x = -r; x < r ; x++)
		{
		    int height = (int)Math.sqrt(r * r - x * x);

		    for (int y = -height; y < height; y++) {
		    	int yy = y+y0;
		    	int xx = x+x0;
		    	if(xx < 0 || xx >= texWidth) continue;
		    	if(yy < 0 || yy >= texHeight) continue;
		    	
		    	int idx = yy * texWidth + xx;
		    	if(idx >= 0 && idx < pixels.length)
		    		pixels[ idx ] = 0;
		    }
		}
		
		pxBuff.put( pixels );
		pxBuff.position( 0 );
		
		gl.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, texWidth, texHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pxBuff);
		
		int code = gl.glGetError();
		if(code != 0)
			Log.w( "GL_ERROR", "Code: " +  code);
		
	}
	
	
	public void drawFog(GL11 gl) {
		//Log.e("FOW", "Draw fog");
		gl.glEnableClientState( GL11.GL_VERTEX_ARRAY );
		gl.glEnableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
		
		gl.glEnable(GL11.GL_TEXTURE_2D);
		
		gl.glDisable(GL11.GL_DEPTH_TEST);
		
		gl.glTexCoordPointer( 2, GL11.GL_FLOAT, 0, default_uvbuff);
		gl.glVertexPointer( 3, GL11.GL_FLOAT, 0, vertsBuff );
		gl.glDrawElements( GL11.GL_TRIANGLES, indices.length, GL11.GL_UNSIGNED_SHORT, indsBuff );
		
		gl.glEnable(GL11.GL_DEPTH_TEST);
		gl.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		
		//Log.e("FOW", "END Draw fog");
	}
	
	public void addToMoved(ArrayList<IsoSprite> moved) {
		for(IsoSprite ms : moved)
			if(!this.moved.contains(ms))
				this.moved.add(ms);
	}
	
	public void updateFog(GL11 gl) {
		for(IsoSprite ms : moved) {
			Vec3 c = ms.getCenter();
			Point cent = Transformer.isoToPlot(c.x, c.y);
			drawVisibleArea(gl, (int)cent.x, (int)cent.y, ms.getDiscoverRadius());
		}
		moved.clear();
	}
}
