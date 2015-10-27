package hu.garik.android.game.engine.collition;

import hu.garik.android.game.engine.IsoSprite;

public interface OnCollisionListener {

	public void handleCollision(IsoSprite s1, IsoSprite s2, int collisionGroupMask);
}
