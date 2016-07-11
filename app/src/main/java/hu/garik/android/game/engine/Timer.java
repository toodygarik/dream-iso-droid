package hu.garik.android.game.engine;

public class Timer {

	private long ticks;
	
	public void start(){
		
		ticks = System.currentTimeMillis();
		
	}
	
	public long getTicks(){
		
		return System.currentTimeMillis() - ticks;
		
	}
	
}
