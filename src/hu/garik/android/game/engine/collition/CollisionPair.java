package hu.garik.android.game.engine.collition;

import hu.garik.android.game.engine.IsoSprite;

public class CollisionPair {

	public IsoSprite sprite1;
	public IsoSprite sprite2;
	
	public String toString() {
		return sprite1.getName() +" - "+ sprite2.getName();
	}
}
