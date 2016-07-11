package hu.garik.android.game.engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import senseit.games.spellcraft.NativeCopier;

public class LineDrawer {

	private static FloatBuffer vertsBuff;
	private static ShortBuffer indsBuff;
	private static short[] indices = {
			0, 1
	};
	
	public static void drawLine( GL11 gl, float x1, float y1, float x2, float y2){
		
		//gl.glLoadIdentity();
		
		
		float[] verts = {
				x1, y1, -1.0f,
				x2, y2, -1.0f
		};
		
		if(vertsBuff == null) {
			vertsBuff =  ByteBuffer.allocateDirect( 24 ).order( ByteOrder.nativeOrder() ).asFloatBuffer();
		}
		vertsBuff.position(0);
		NativeCopier.copy(verts, vertsBuff, verts.length);
		
		vertsBuff.position( 0 );

		if(indsBuff == null) {
			ByteBuffer ibb = ByteBuffer.allocateDirect( 4 );
			ibb.order( ByteOrder.nativeOrder() );
			indsBuff = ibb.asShortBuffer();
			
			indsBuff.put( indices );
		}
		
		indsBuff.position( 0 );
		
		gl.glColor4f( 1.0f, 0.0f, 0.0f, 1.0f );
		
		gl.glDisable( GL11.GL_TEXTURE_2D );
		
		gl.glEnableClientState( GL11.GL_VERTEX_ARRAY );
		
		gl.glDisable(GL11.GL_DEPTH_TEST);
		gl.glBindBuffer( GL11.GL_ARRAY_BUFFER, 0 );
		gl.glVertexPointer( 3, GL10.GL_FLOAT, 0, vertsBuff );
		gl.glDrawElements( GL10.GL_LINES, 2, GL10.GL_UNSIGNED_SHORT, indsBuff );
		gl.glEnable(GL11.GL_DEPTH_TEST);
		
		gl.glDisableClientState( GL11.GL_VERTEX_ARRAY );
		
		gl.glEnable( GL11.GL_TEXTURE_2D );
		
		gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
		
	}
	
}
