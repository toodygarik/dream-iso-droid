package hu.garik.android.game.engine;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GLCanvas extends GLSurfaceView{

	private GLRenderer renderer;
	private GameScene scene;
	
	public GLCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public GLCanvas(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		renderer = new GLRenderer(this);
		renderer.setFpsCount(true);
		
		this.setRenderer(renderer);

		TextureFactory.getInstance().setContext( this.getContext() );
	}
	
	
	public void setGameScene(GameScene scene) {
		this.scene = scene;
		
		this.setOnTouchListener(scene);
		this.setOnKeyListener(scene);
	}
	
	public GameScene getGameScene() {
		return scene;
	}
	
	public void setFps(int fps) {
		if(renderer != null)
			renderer.setFPS(fps);
	}
	
	public int getFps() {
		if(renderer != null)
			return renderer.getFPS();
		else
			return -1;
	}
	
}
