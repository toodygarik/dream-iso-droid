package hu.garik.android.game.testgmame;

import hu.garik.android.game.R;
import hu.garik.android.game.engine.GLCanvas;
import hu.garik.android.game.engine.GameScene;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	private GLCanvas canvas;
	private Button u, d, l, r;
	private ImageView img;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.main);
        
        canvas = (GLCanvas) findViewById(R.main.glcanvas);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        
        GameScene scene = new TestScene(this, metrics.widthPixels, metrics.heightPixels);
        
        canvas.setGameScene(scene);
        canvas.setFps(100);

        u = (Button) findViewById(R.main.t_button);
        d = (Button) findViewById(R.main.b_button);
        l = (Button) findViewById(R.main.l_button);
        r = (Button) findViewById(R.main.r_button);
        
        u.setOnClickListener((TestScene)canvas.getGameScene());
        d.setOnClickListener((TestScene)canvas.getGameScene());
        l.setOnClickListener((TestScene)canvas.getGameScene());
        r.setOnClickListener((TestScene)canvas.getGameScene());
        
        
        img = (ImageView)findViewById(R.main.img);
        /*
        try {
			InputStream is = getAssets().open("sprites/tree.png");
			if(is.available() > 0) {
				//BitmapDrawable drawable = (BitmapDrawable) BitmapDrawable.createFromStream(is, null);
				
				img.setImageBitmap(BitmapFactory.decodeStream(is));
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		*/
        
        
        
      //  img.setBackgroundResource(id);
    
    }
    
    
    public static int getResId(String variableName, Context context, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } 
    }
}