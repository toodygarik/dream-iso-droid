package hu.garik.android.game.engine;

public class Vec3 {

	public float x = 0;
	public float y = 0;
	public float z = 0;
	
	public Vec3() {}
	
	public Vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public String toString() {
		return "Vec3: x: "+x+" y: "+y+" z: "+z;
	}
}
