package hu.garik.android.game.engine;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class GLRenderer implements Renderer{

	
	private GLCanvas canvas;
	
	private int maxFps = 30;
	private Timer fpsTimer, updateTimer;
	
	private boolean isFpsCount = false;
	private int frameCounter = 0;
	private long lastFpsCountTime = 0;
	private int lastCountedFps;
	
	
	// native buffer copier library loading
	static{
		try{
			System.loadLibrary( "senseit-games-spellcraft" );
			Log.e("GLCanvas", "Librari loaded");
		}catch( UnsatisfiedLinkError e ){
			e.printStackTrace();
		}
	}
	
	
	
	public GLRenderer(GLCanvas canvas) {
		this.canvas = canvas;
		fpsTimer = new Timer();
		updateTimer = new Timer();
		updateTimer.start();
	}
	
	public void setFPS(int fps) {
		this.maxFps = fps;
	}
	
	public int getFPS() {
		return maxFps;
	}
	
	public void setFpsCount(boolean isFpsCount) {
		this.isFpsCount = isFpsCount;
	}
	
	public boolean isFpsCount() {
		return this.isFpsCount;
	}
	
	public int getCountedFps() {
		if(isFpsCount)
			return lastCountedFps;
		else
			return -1;
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );

		fpsTimer.start();
		
		// frame counting
		if(isFpsCount) {
			frameCounter++;
			
			if((System.currentTimeMillis() - lastFpsCountTime) > 1000 ) {
				lastFpsCountTime = System.currentTimeMillis();
				lastCountedFps = 1000 / frameCounter;
				
				Log.d("FPS", "FPS: "+frameCounter+"/"+maxFps);
				
				frameCounter = 0;
			}
		}
		
		// render and update calls
		if(canvas.getGameScene() != null) {
			
			canvas.getGameScene().checkCollision();
			
			canvas.getGameScene().autoUpdate( updateTimer.getTicks() );
			updateTimer.start();
			
			canvas.getGameScene().render((GL11)gl);
			
			// TEST
			//canvas.getGameScene().getCollisionDetector().debugDraw(gl);
		}
		
		// sleep
		if( fpsTimer.getTicks() < 1000 / maxFps ){
			
			try {
				long sleepTime = 1000 / maxFps - fpsTimer.getTicks();
				if(sleepTime > 0)
					Thread.sleep( sleepTime );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		
		gl.glMatrixMode( GL10.GL_PROJECTION );
		gl.glLoadIdentity();
		
		gl.glViewport( 0, 0, width, height );
		gl.glOrthof( 0, width, height, 0, 0.1f, 1.1f );
		
		gl.glMatrixMode( GL10.GL_MODELVIEW );
		gl.glLoadIdentity();
		
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		TextureFactory.getInstance().clear();
		TextureFactory.getInstance().setGl( (GL11) gl );
		
		gl.glEnable( GL10.GL_DEPTH_TEST ); // mélység tesztelés
		gl.glDepthFunc( GL10.GL_LEQUAL );
		gl.glDepthMask(true);
		//gl.glClearDepthf( 1.0f );
		
		gl.glClearColor( 0.0f, 0.0f, 0.0f, 1.0f );
		
		gl.glDisable( GL10.GL_DITHER );
		
		/*
		gl.glEnable( GL10.GL_BLEND ); // alpha testing
		gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
		*/
		
		gl.glEnable(GL10.GL_ALPHA_TEST);
		gl.glAlphaFunc(GL10.GL_GREATER, 0.01f);
		
		gl.glEnable( GL10.GL_TEXTURE_2D );
		
		gl.glEnableClientState( GL10.GL_VERTEX_ARRAY );
		gl.glEnableClientState( GL10.GL_TEXTURE_COORD_ARRAY );
		

	}

}
