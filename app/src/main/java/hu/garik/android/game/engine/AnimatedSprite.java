package hu.garik.android.game.engine;
import hu.garik.android.game.engine.io.SpriteDataParser;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;


public class AnimatedSprite implements Comparable<AnimatedSprite> {

	private static float[] verts = {
		
		0.0f, 0.0f, 0.0f,
		1.0f, 0.0f, 0.0f,
		1.0f, 1.0f, 0.0f,
		0.0f, 1.0f, 0.0f
		
	};
	private static FloatBuffer vertsBuff;
	
	private static short[] indices = {
		
		0, 1, 2,
		0, 2, 3
		
	};
	public static ShortBuffer indsBuff;

	protected HashMap< String, SpriteAnimation > animations;
	
	private static boolean isFirstLoaded = true;
	
	private String image;
	
	public float x, y, w, h;
	public float z = -1.0f;
	
	private static float[] default_uvcoords = {
	
		0.0f, 0.0f,
		1.0f, 0.0f,
		1.0f, 1.0f,
		0.0f, 0.0f,
		1.0f, 1.0f,
		0.0f, 1.0f
		
	};
	public static FloatBuffer default_uvbuff;
	
	// animation variables
	private SpriteAnimation currentAnimation;
	private int cur_animation_frame = 0;
	private int cur_start_anim_frame = 0;
	private boolean animate = false;
	private boolean loopAnimation = true;
	private long lastAnimation = 0;
	/** A flag which shows that in last frame was this sprite moved. */
	protected boolean moved;
	
	
	public AnimatedSprite(String image, float x, float y){
		
		setX(x);
		setY(y);
		setTexture(image);
		
		if( isFirstLoaded ){
			
			ByteBuffer vbb = ByteBuffer.allocateDirect( verts.length * 4 );
			vbb.order( ByteOrder.nativeOrder() );
			vertsBuff = vbb.asFloatBuffer();
			vertsBuff.put( verts );
			vertsBuff.position( 0 );
			
			ByteBuffer ibb = ByteBuffer.allocateDirect( indices.length * 2 );
			ibb.order( ByteOrder.nativeOrder() );
			indsBuff = ibb.asShortBuffer();
			indsBuff.put( indices );
			indsBuff.position( 0 );
			
			// lehet hogy nem csak egyszer kell!!!
			ByteBuffer ubb = ByteBuffer.allocateDirect( default_uvcoords.length * 4 );
			ubb.order( ByteOrder.nativeOrder() );
			default_uvbuff = ubb.asFloatBuffer();
			default_uvbuff.put( default_uvcoords );
			default_uvbuff.position( 0 );
		
			isFirstLoaded = false;
			
		}
		
		
		animations = new HashMap<String, SpriteAnimation>();

		// load default fullimage animation
		SpriteAnimation default_animation = new SpriteAnimation();
		default_animation.name = "default";
		default_animation.uvCoords.add(default_uvcoords);
		currentAnimation = default_animation;
		animations.put("default", default_animation);
		
		moved = false;
	}
	
	public AnimatedSprite(String image) {
		this(image, 0.0f, 0.0f);
	}
	
	
	
	public void setTexture( String image ){ this.image = image; }
	
	public void setX( float x ){ this.x = x; }
	public void setY( float y ){ this.y = y; }
	public void setZ( float z ){ 
		this.z = z; 
		if( z > -0.1f || z < -1.1f) 
			Log.w("JGIso Engine", "ERROR! z="+z+" is out of drawing -0.1 (Near) nad -1.1(Far) bounds!"); 
	}
	public void setWidth( float w ){ this.w = w; }
	public void setHeight( float h ){ this.h = h; }
	
	
	public float getX() { return x; }
	public float getY() { return y; }
	public float getZ() { return z; }
	public float getWidth() { return w; }
	public float getHeight() { return h; }
	public String getTexture() { return image; }
	public SpriteAnimation getCurrentAnimation() {return currentAnimation;}
	public int getCurrentAnimationFrame() { return cur_animation_frame;}
	
	
	public void setAnimation(String name, int startFrame, long stepTime) {
		if(animations.containsKey(name)) {
			currentAnimation = animations.get(name);
			cur_animation_frame = startFrame;
			cur_start_anim_frame = startFrame;
		}
	}
	
	public void setAnimation(String name) {
		SpriteAnimation anim = animations.get(name);
		this.setAnimation(name, anim.startFrame, anim.stepTime);
	}
	
	public void startAnimation() {
		animate = true;
	}
	
	public void stopAnimation() {
		animate = false;
	}
	
	public void setLoopAnimation(boolean isLoop) {
		this.loopAnimation = isLoop;
	}
	
	public boolean isLoopAnimation() {
		return loopAnimation;
	}
	
	public boolean isAnimationRunning() {
		return animate;
	}
	
	public int getAnimaationFrame() {
		return cur_animation_frame;
	}
	
	public void setAnimationFrame(int frameIdx) {
		this.cur_animation_frame = frameIdx;
	}
	
	public void setAnimationStartFrame(int frameIdx) {
		if(frameIdx < currentAnimation.uvCoords.size()) {
			cur_start_anim_frame = frameIdx;
			cur_animation_frame = frameIdx;
		}
	}
	
	public void nextFrame() {
		//animation step timing
		long elapsed = System.currentTimeMillis() - lastAnimation;
		if(currentAnimation.stepTime > elapsed )
			return;
		
		lastAnimation = System.currentTimeMillis();
		
		cur_animation_frame++;
		if(currentAnimation.uvCoords.size() == cur_animation_frame) 
			cur_animation_frame = 0;
		
		// stop animation
		if(cur_animation_frame == cur_start_anim_frame && !loopAnimation)
			animate = false;
	}
	
	public void draw( GL10 gl ){
		
		gl.glLoadIdentity();
		
		gl.glTranslatef( x, y, z );
		
		gl.glScalef( w, h, 1.0f );
		
		gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
		
		TextureFactory.getInstance().bind(image);

		gl.glTexCoordPointer( 2, GL10.GL_FLOAT, 0, default_uvbuff);
		gl.glVertexPointer( 3, GL10.GL_FLOAT, 0, vertsBuff );
		gl.glDrawElements( GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_SHORT, indsBuff );
		
	}
	
	protected void autoUpdate(long elapsedTime) {
		// disable the move flag
		moved = false;
		
		if(animate)
			nextFrame();
		
		update();
	}
	
	public void update() {}
	
	
	public void move(float dx, float dy) {
		moveX(dx);
		moveY(dy);		
	}
	
	public void moveX(float dx) {
		this.x += dx;
		moved = true;
	}
	
	public void moveY(float dy) {
		this.y += dy;
		moved = true;
	}
	
	public void loadAnimationsFromFile(InputStream animxml) {
		ArrayList<SpriteAnimation> anims = SpriteDataParser.loadAnimationFromFile(animxml);
		
		addAnimationSet(anims);
	}
	
	public void addAnimationSet(ArrayList<SpriteAnimation> anims) {
		
		ListIterator<SpriteAnimation> iter = anims.listIterator();
		while(iter.hasNext()) {
			SpriteAnimation cur = iter.next();
			animations.put(cur.name, cur);
		}
	}

	
	public boolean hasMoved() { return this.moved; }

	@Override
	public int compareTo(AnimatedSprite another) {
		return this.image.compareTo(another.image);
	}
	
	
}
