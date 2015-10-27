package hu.garik.android.game.testgmame;

import hu.garik.android.game.R;
import hu.garik.android.game.engine.FogOfWar;
import hu.garik.android.game.engine.GameScene;
import hu.garik.android.game.engine.IsoSprite;
import hu.garik.android.game.engine.LineDrawer;
import hu.garik.android.game.engine.Map;
import hu.garik.android.game.engine.Point;
import hu.garik.android.game.engine.Transformer;
import hu.garik.android.game.engine.Vec3;
import hu.garik.android.game.engine.Viewport;
import hu.garik.android.game.engine.ai.IsoGridPathFinder;
import hu.garik.android.game.engine.ai.IPathFinder;
import hu.garik.android.game.engine.collition.CollisionDetector;
import hu.garik.android.game.engine.collition.OnCollisionListener;
import hu.garik.android.game.engine.io.SpriteDataParser;

import java.io.IOException;
import java.util.Random;

import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;

public class TestScene extends GameScene implements OnClickListener, OnCollisionListener, OnGestureListener {

	private int GROUND_LAYER;
	private int SHADOW_LAYER;
	private int MAIN_LAYER;
	
	private IsoSprite tsprite, s2, house, house_s, szonyeg;
	private IsoSprite[] forest, forest_shadow;
	private IsoSprite[][] grass;
	
	private Vec3 startPos;
	
	private GestureDetector motions;
	
	
	public TestScene(Context context, int surfaceWidth, int surfaceHeight) {
		super(context, surfaceWidth, surfaceHeight);
		motions = new GestureDetector(this);
	}
	
	@Override
	protected Viewport createViewport() {
		
		Viewport vp = new Viewport(0, 0, surfaceWidth, surfaceHeight, 2000, 2000, 0.5f);
		
		GROUND_LAYER = vp.addLayer(new Map(1));
		SHADOW_LAYER = vp.addLayer(new Map(50));
		MAIN_LAYER = vp.addLayer(new Map(50));
		
		
		return vp;
	}
	
	@Override
	protected CollisionDetector createCollisionDetector() {
		
		Vec3 mapCorner1 = new Vec3(0, 0, 0);
		Vec3 mapCorner2 = new Vec3(2000, 2000, 400);
		
		CollisionDetector collitionDetector = new CollisionDetector(mapCorner1, mapCorner2, new Vec3(40,40,40));
		collitionDetector.addOnCollisionListener(this);
		
		return collitionDetector;
	}
	
	@Override
	protected IPathFinder createPathFinder() {
		return new IsoGridPathFinder(2000, 2000, 50);
	}

	@Override
	protected FogOfWar createFogOfWar() {
		return new FogOfWar("fogtex", 128, 128, 2000, 2000);
	}

	@Override
	public void init(Context context) {

		//s2 = new IsoSprite(tsprite);
		try {
			s2 = SpriteDataParser.loadIsoSprite(context.getAssets().open("sprites/male_light_sprite.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		s2.moveInPlot(1200, 700, 0);
		s2.setAnimation("left_down_move");
		s2.setAnimationStartFrame(0);
		
		s2.setMoveAnimationNames(new String[] {"left_move", "left_up_move", "up_move", "right_up_move", 
				"right_move", "right_down_move", "down_move", "left_down_move"});
		
		
		//s2.setDebugMode(true);
	
		vp.addElement(s2, MAIN_LAYER);
		s2.moveInIso(1, 0, 0);
		s2.addCollisionType(1);
		collitionDetector.add(s2);
		
		try {
			tsprite = SpriteDataParser.loadIsoSprite(context.getAssets().open("sprites/money_sprite.xml"));
			tsprite.setName("MOVE");
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		tsprite.setAnimation("left_move");
		tsprite.startAnimation();
		tsprite.setLoopAnimation(true);
		 */
		//tsprite.setDebugMode(true);
		
		
		
		vp.addElement(tsprite, MAIN_LAYER);
		tsprite.addCollisionType(1);
		tsprite.addCollisionType(2);
		tsprite.addCollisionType(4);
		//collitionDetector.add(tsprite);
		
		
		
		//forest
		Random rnd = new Random();
		forest = new IsoSprite[200];
		forest_shadow = new IsoSprite[forest.length];
		for(int i=0; i<forest.length; i++) {
			int treeType = rnd.nextInt(2);
			
			try {
				forest[i] = SpriteDataParser.loadIsoSprite(context.getAssets().open("sprites/tree"+treeType+"_sprite.xml"));
				forest[i].setName(forest[i].getName()+"_"+i);
			} catch (IOException e) {
				e.printStackTrace(); 
				continue;
			}
			forest[i].moveInPlot(rnd.nextInt(100) * 20, rnd.nextInt(100) * 20, 0);
			
		
			//forest[i].setDebugMode(true);
			
			//Log.d("Sceend ADD", "index: "+i);
			//Log.d("Sceend ADD", "sprited ID: "+forest[i]);
			vp.addElement(forest[i], MAIN_LAYER);
			forest[i].addCollisionType(1);
			collitionDetector.add(forest[i]);
			
			pathFinder.addObstacle(forest[i]);
			
			
			try {
				forest_shadow[i] = SpriteDataParser.loadIsoSprite(context.getAssets().open("sprites/tree0_sprite_shadow.xml"));
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			forest_shadow[i].setX(forest[i].getX());
			forest_shadow[i].setY(forest[i].getY());
			
			vp.addElement(forest_shadow[i], SHADOW_LAYER);
			
		}
		
		
		// forester's longe
		try {
			house = SpriteDataParser.loadIsoSprite(context.getAssets().open("sprites/tree1_sprite.xml"));
			//house.setWidth(57);
			//house.setHeight(86);
			house.moveInPlot(150, 80, 0);
			//house.setDebugMode(true);
			vp.addElement(house, MAIN_LAYER);
			house.addCollisionType(1);
			house.addCollisionType(4);
			collitionDetector.add(house);
			pathFinder.addObstacle(house);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			house_s = SpriteDataParser.loadIsoSprite(context.getAssets().open("sprites/tree1_sprite_shadow.xml"));
			house_s.moveInPlot(150, 80, 0);
			//vp.addElement(house_s, GROUND_LAYER);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		// talaj
		grass = new IsoSprite[1][1];
		for(int x=0; x<1; x++) 
			for(int y=0; y<1; y++) {
				try {
					grass[x][y] = SpriteDataParser.loadIsoSprite(context.getAssets().open("sprites/grass.xml"));
					grass[x][y].moveInPlot(x*2000,y*2000, 0);
					grass[x][y].setAnimation("copy");
					
					vp.addElement(grass[x][y], GROUND_LAYER);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		
		//szonyeg
		try {
			szonyeg = SpriteDataParser.loadIsoSprite(context.getAssets().open("sprites/szonyeg_sprite.xml"));
			szonyeg.moveInPlot(10, 40, 0);
			vp.addElement(szonyeg, SHADOW_LAYER);
			//collitionDetector.add(szonyeg);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		// random rock
		try {
			
			for(int i=0; i<100; i++) {
				IsoSprite rock = SpriteDataParser.loadIsoSprite(context.getAssets().open("sprites/rocks_sprite.xml"));
				rock.moveInPlot(rnd.nextInt(2000), rnd.nextInt(2000), 0);
				rock.setAnimation("rock0");
				
				vp.addElement(rock, MAIN_LAYER);
				rock.addCollisionType(1);
				collitionDetector.add(rock);
				pathFinder.addObstacle(rock);
			}
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void update(long elapsedTime) {
		//Random rnd = new Random();
		
		//s2.moveInIso(-4 + rnd.nextInt(5)*2, -4 + rnd.nextInt(5)*2, 0);
	}

	@Override
	public boolean onTouch(View v, MotionEvent e) {
		motions.onTouchEvent(e);
		
		return true;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {

		float xd, yd;
		
		xd = 0;
		yd = 0;
		
		switch(v.getId()) {
		case R.main.t_button:
			yd -= 0.1f;
			break;
		case R.main.b_button:
			yd += 0.1f;
			break;
		case R.main.l_button:
			long start, end;
			
			start = System.currentTimeMillis();
			Vec3[] path = pathFinder.getPath(s2, 0, 0);
			end = System.currentTimeMillis();
			
			Log.i("TestScene", "Path time: "+(end- start)+"ms");
			break;
		case R.main.r_button:
			xd += 10;
			break;
		}
		
		setZoom(getZoom() + yd);
	}

	@Override
	public void handleCollision(IsoSprite s1, IsoSprite s2, int collisionGroupMask) {
		Log.w("COLLITION", "COLLIDE!!!!! "+s1+" : "+s2 +" maks: "+ collisionGroupMask);
	
	}

	
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		//s2.moveTo(e.getX() + vp.getX(), e.getY() + vp.getY(), s2.getZ(), 200);
		s2.movePathTo(pathFinder, e.getX() + vp.getX(), e.getY() + vp.getY(), 200);
		
		Log.e("TEST SCENE","TAP: x: "+(e.getX() + vp.getX())+" Y: "+(e.getY() + vp.getY()));
		startPos = s2.getCenter();
		
		return true;
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		
		if(Math.abs(distanceX) < 1.0f && Math.abs(distanceY) < 1.0f)
			return false;
		
		vp.move((int)(distanceX), (int)(distanceY));
		
		return true;
	}
	
	
	@Override
	public void render( GL11 gl) {
		super.render(gl);
		
		Point a = Transformer.isoToPlot(s2.getIminX(),	s2.getIminY());
		Point b = Transformer.isoToPlot(s2.getImaxX(),	s2.getImaxY());
		
		LineDrawer.drawLine(gl, a.x, a.y, b.x, b.y);
		
		a = Transformer.isoToPlot(s2.getImaxX(),	s2.getIminY());
		b = Transformer.isoToPlot(s2.getIminX(),	s2.getImaxY());
		
		LineDrawer.drawLine(gl, a.x, a.y, b.x, b.y);
		
		Vec3[] s2Path = s2.getPath();
		if(s2Path != null) {
			Point p0, p1;
			for(int i=0; i<s2Path.length-1; i++) {
				p0 = Transformer.isoToPlot(s2Path[i].x, s2Path[i].y);
				p1 = Transformer.isoToPlot(s2Path[i+1].x, s2Path[i+1].y);
				LineDrawer.drawLine(gl, p0.x, p0.y, p1.x, p1.y);
			}
		}
		
		if(startPos != null) {
			a = Transformer.isoToPlot(startPos.x - 10,	startPos.y - 10);
			b = Transformer.isoToPlot(startPos.x + 10,	startPos.y + 10);
			
			LineDrawer.drawLine(gl, a.x, a.y, b.x, b.y);
			
			a = Transformer.isoToPlot(startPos.x + 10,	startPos.y - 10);
			b = Transformer.isoToPlot(startPos.x - 10,	startPos.y + 10);
			
			LineDrawer.drawLine(gl, a.x, a.y, b.x, b.y);
		}
		
		
	}
	
	
	@Override
	public boolean onDown(MotionEvent e) {return false;}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {return false;}
	@Override
	public void onLongPress(MotionEvent e) {}
	@Override
	public void onShowPress(MotionEvent e) {}


}
