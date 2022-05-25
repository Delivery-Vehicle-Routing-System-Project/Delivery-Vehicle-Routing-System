package DVRS;

public class Coordinate {

	public final long x;
	public final long y;

	public Coordinate() {
		x = 0;
		y = 0;
	}
	
	public Coordinate(long X, long Y) {
		x = X;
		y = Y;
	}
	
	public Coordinate(Coordinate l) {
		x = l.x;
		y = l.y;
	}
	
	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
}
