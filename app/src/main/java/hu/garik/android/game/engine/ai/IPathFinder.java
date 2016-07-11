package hu.garik.android.game.engine.ai;

import hu.garik.android.game.engine.IsoSprite;
import hu.garik.android.game.engine.Vec3;

public interface IPathFinder {

	public float getWorldIsoWidth();
	public float getWorldIsoHeight();
	
	public void addObstacle(IsoSprite obstacle);
	public void removeObstacle(IsoSprite obstacle);
	
	public Vec3[] getPath(IsoSprite mover, int toX, int toY);
}
