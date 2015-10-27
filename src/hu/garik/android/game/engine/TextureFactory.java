package hu.garik.android.game.engine;

import hu.garik.android.game.R.drawable;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLUtils;
import android.util.Log;

public class TextureFactory {

	private Context context;
	private GL11 gl;
	
	private static TextureFactory instance = null;
	
	//HashMap< String, Integer > textures;
	ArrayList<TexData> textures;
	
	protected TextureFactory(){
		
		//textures = new HashMap< String, Integer >();
		textures = new ArrayList<TexData>();
		
	}
	
	public static TextureFactory getInstance(){
		
		if( instance == null )
			instance = new TextureFactory();
		
		return instance;
			
		
	}
	
	public void setGl( GL11 gl ){
		
		this.gl = gl;
		
	}
	
	public void setContext( Context c ){
		
		this.context = c;
		
	}
	
	//
	public Context getContext(){
		
		return context;
		
	}
	
	public int getId( String name ){
		
		return isLoadedTexture( name );
		
	}
	
	
	public void loadTexture( String image ){
		
		Log.d("TextureFactory", "loading: "+image);
		
		int[] texture = new int[ 1 ];

		gl.glGenTextures( 1, texture, 0 );
		gl.glBindTexture( GL11.GL_TEXTURE_2D, texture[ 0 ] );
	
		int ResourceId = getResId(image, context, drawable.class);
		//Log.e("RESID", "ID: "+ResourceId);
		Bitmap bmp = ( ( BitmapDrawable ) context.getResources().getDrawable( ResourceId ) ).getBitmap();
		
		gl.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR );
		gl.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR );
		gl.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		gl.glTexParameterf( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		
	    int[] crop = new int[4];

	    crop[0] = 0;
	    crop[1] = bmp.getHeight();
	    crop[2] = bmp.getWidth();
	    crop[3] = -bmp.getHeight();
		( ( GL11 ) gl ).glTexParameteriv( GL11.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, crop, 0 );
		
		GLUtils.texImage2D( GL11.GL_TEXTURE_2D, 0, bmp, 0 );
		
		bmp.recycle();
	
		//textures.put( image, texture[ 0 ] );
		textures.add(new TexData(image, texture[ 0 ]));
		
	}
	
	private int isLoadedTexture(String image) {
		for(int i=0; i <textures.size(); i++) 
			if(textures.get(i).key.equals(image))
				return i;
		
		return -1;
	}
	
	public void bind( String image ){
		
		/*
		//TODO a containsKey kurva lassú!!!!
		if( !textures.containsKey( image ) )
			loadTexture( image );
		*/
		
		int texIdx = isLoadedTexture(image);
		if(texIdx == -1) {
			loadTexture(image);
			texIdx = textures.size()-1;
		}

		gl.glBindTexture( GL11.GL_TEXTURE_2D, textures.get( texIdx ).texId );
		
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
    
    public void clear() {
    	textures.clear();
    }
	
}


class TexData { 
	
	
	public String key;
	public Integer texId;
	
	public TexData(String key, Integer id) {
		this.key = key;
		this.texId = id;
	}

}