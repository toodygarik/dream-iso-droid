package hu.garik.android.game.engine.io;

import hu.garik.android.game.engine.IsoSprite;
import hu.garik.android.game.engine.SpriteAnimation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class SpriteDataParser {

	
	
	public static IsoSprite loadIsoSprite(InputStream xmlstream)  {
		
		IsoSprite sprite = null;
		ArrayList<SpriteAnimation> anims = new ArrayList<SpriteAnimation>();
		
		XmlPullParser parser;
		int eventType;
		int width = 0;
		int height = 0;
		
		try {
			
			parser = XmlPullParserFactory.newInstance().newPullParser();
			
			parser.setInput(new InputStreamReader(xmlstream));
			
			 eventType = parser.getEventType();
			 
			 while (eventType != XmlPullParser.END_DOCUMENT) {
	        	 
	        	 if(eventType == XmlPullParser.START_DOCUMENT) {
	        		 //System.out.println("Start document");
	        		 
	        	 } else if(eventType == XmlPullParser.START_TAG) {
	        		 //System.out.println("Start tag "+parser.getName());
	        		 
	        		 //start isosprite tag
	        		 if(parser.getName().equals("isosprite")) {
	        			 sprite = loadSpriteTag(parser);
	        		 }
	        		 
	        		 // animations tag
	        		 if(parser.getName().equals("animations")) {
	        			 for(int i=0; i<parser.getAttributeCount(); i++) {
	        				// System.out.println("Attr name: "+parser.getAttributeName(i));
	        				 
	        				 String attrName = parser.getAttributeName(i);
	        				 
	        				 if(attrName.equals("imgwidth")) 
	        					 width = Integer.valueOf(parser.getAttributeValue(i));
	        				 else if(attrName.equals("imgheight")) 
	        					 height = Integer.valueOf(parser.getAttributeValue(i));
	        			 }
	        		 }
	        		 
	        		 // animation tag
	        		 if(parser.getName().equals("animation")) {
	        			 anims.add( loadAnimation(parser, width, height) );
	        			 //System.out.println("ANIMATION LOADED");
	        		 }
	        		 
	        	 } else if(eventType == XmlPullParser.END_TAG) {
	        		 //System.out.println("End tag "+parser.getName());
	        		 
	        	 } else if(eventType == XmlPullParser.TEXT) {
	        		 //System.out.println("Text "+parser.getText());
	        		 
	        	 }
	          
	        	 eventType = parser.next();
	         }
			 
			 //System.out.println("End document");
			 
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// add loaded animations
		sprite.addAnimationSet(anims);
		
        return sprite;
	}
	
	private static IsoSprite loadSpriteTag(XmlPullParser parser) {
		IsoSprite sprite = null;
		
		String imgname = null, name = "";
		float swidth = 0, sheight = 0;
		int ax = 0, ay = 0, bx = 0, by = 0, bodyHeight = 0, startZ = 0, disoverRadius = 0;
		boolean isShadow = false;
		
		// load attributes
		for(int i=0; i<parser.getAttributeCount(); i++) {
			//System.out.println("Attr name: "+parser.getAttributeName(i));
			
			String attribName = parser.getAttributeName(i);
			
			if(attribName.equals("name")) 
				name = parser.getAttributeValue(i);
			
			else if(attribName.equals("imgname")) 
				imgname = parser.getAttributeValue(i);
			
			else if(attribName.equals("spritewidth")) 
				swidth = Float.valueOf(parser.getAttributeValue(i));
			
			else if(attribName.equals("spriteheight"))
				sheight = Float.valueOf(parser.getAttributeValue(i));
			
			else if(attribName.equals("ax"))
				ax = Integer.valueOf(parser.getAttributeValue(i));
			
			else if(attribName.equals("ay"))
				ay = Integer.valueOf(parser.getAttributeValue(i));
			
			else if(attribName.equals("bx"))
				bx = Integer.valueOf(parser.getAttributeValue(i));
			
			else if(attribName.equals("by"))
				by = Integer.valueOf(parser.getAttributeValue(i));
			
			else if(attribName.equals("bodyheight"))
				bodyHeight = Integer.valueOf(parser.getAttributeValue(i));
			
			else if(attribName.equals("startZ"))
				startZ = Integer.valueOf(parser.getAttributeValue(i));
			
			else if(attribName.equals("isShadow"))
				isShadow = Boolean.valueOf(parser.getAttributeValue(i));
			
			else if(attribName.equals("discoverRadius"))
				disoverRadius = Integer.valueOf(parser.getAttributeValue(i));
		}
		
		// collect
		if(imgname == null)
			return null;
		
		sprite = new IsoSprite(imgname);
		sprite.setName(name);
		sprite.setWidth(swidth);
		sprite.setHeight(sheight);
		sprite.setShadow(isShadow);
		sprite.setDiscoverRadius(disoverRadius);
		sprite.initIsoParameters(ax, ay, bx, by, startZ, bodyHeight);
		
		return sprite;
	}
	
	
	public static ArrayList<SpriteAnimation> loadAnimationFromFile(InputStream xmlstream)  {
		ArrayList<SpriteAnimation> anims = new ArrayList<SpriteAnimation>();
		
		XmlPullParser parser;
		int eventType;
		int width = 0;
		int height = 0;
		
		try {
			
			parser = XmlPullParserFactory.newInstance().newPullParser();
			
			parser.setInput(new InputStreamReader(xmlstream));
			
			 eventType = parser.getEventType();
			 
			 while (eventType != XmlPullParser.END_DOCUMENT) {
	        	 
	        	 if(eventType == XmlPullParser.START_DOCUMENT) {
	        		 //System.out.println("Start document");
	        		 
	        	 } else if(eventType == XmlPullParser.START_TAG) {
	        		 //System.out.println("Start tag "+parser.getName());
	        		 
	        		 // animations tag
	        		 if(parser.getName().equals("animations")) {
	        			 for(int i=0; i<parser.getAttributeCount(); i++) {
	        				// System.out.println("Attr name: "+parser.getAttributeName(i));
	        				 
	        				 String attrName = parser.getAttributeName(i);
	        				 
	        				 if(attrName.equals("imgwidth")) 
	        					 width = Integer.valueOf(parser.getAttributeValue(i));
	        				 else if(attrName.equals("imgheight")) 
	        					 height = Integer.valueOf(parser.getAttributeValue(i));
	        			 }
	        		 }
	        		 
	        		 // animation tag
	        		 if(parser.getName().equals("animation")) {
	        			 anims.add( loadAnimation(parser, width, height) );
	        			 //System.out.println("ANIMATION LOADED");
	        		 }
	        		 
	        	 } else if(eventType == XmlPullParser.END_TAG) {
	        		 //System.out.println("End tag "+parser.getName());
	        		 
	        	 } else if(eventType == XmlPullParser.TEXT) {
	        		 //System.out.println("Text "+parser.getText());
	        		 
	        	 }
	          
	        	 eventType = parser.next();
	         }
			 
			 //System.out.println("End document");
			 
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
         return anims;
	}
	
	private static SpriteAnimation loadAnimation(XmlPullParser parser, int width, int height) throws XmlPullParserException, IOException {
		SpriteAnimation anim = new SpriteAnimation();
		int eventType = -1;
		
		// load attributes
		for(int i=0; i<parser.getAttributeCount(); i++) {
			//System.out.println("Attr name: "+parser.getAttributeName(i));
			
			String attribName = parser.getAttributeName(i);
			
			if(attribName.equals("name")) 
				anim.name = parser.getAttributeValue(i);
			
			else if(attribName.equals("steptime")) 
				anim.stepTime = Long.valueOf(parser.getAttributeValue(i));
			
			else if(attribName.equals("startframe"))
				anim.startFrame = Integer.valueOf(parser.getAttributeValue(i));
		}
		
		//load frames
		eventType = parser.next();
		while(eventType != XmlPullParser.END_DOCUMENT) {
			
			 if(eventType == XmlPullParser.END_TAG && parser.getName().equals("animation"))
				 break;
			 
			 if(eventType == XmlPullParser.START_TAG && parser.getName().equals("frame")) {
					int left, right, top, bottom;
					left = right = top = bottom = 0;
					
					for(int i=0; i<parser.getAttributeCount(); i++) {
						//System.out.println("Attr name: "+parser.getAttributeName(i));
						
						String attribName = parser.getAttributeName(i);
						
						if(attribName.equals("left"))
							left = Integer.valueOf(parser.getAttributeValue(i));
						
						else if(attribName.equals("right"))
							right = Integer.valueOf(parser.getAttributeValue(i));
					
						else if(attribName.equals("top"))
							top = Integer.valueOf(parser.getAttributeValue(i));
						
						else if(attribName.equals("bottom"))
							bottom = Integer.valueOf(parser.getAttributeValue(i));
					}
					
					anim.uvCoords.add( generateUvCoords(left, right, top, bottom, width, height) );
					
				} 
			 
			 
			 eventType = parser.next();
		 }
		
		
		return anim;
	}
	
	private static float[] generateUvCoords(float left, float right, float top, float bottom, float imgwidth, float imgheight) {
		float[] uvcoords = {
			
			left / imgwidth, top / imgheight,
			right / imgwidth, top / imgheight,
			right / imgwidth, bottom / imgheight,
			
			left / imgwidth, top / imgheight,
			right / imgwidth, bottom / imgheight,
			left / imgwidth, bottom / imgheight,
		};
		
		
		
		
		return uvcoords;
	}
}
