package DVRS;

public class AABB {

	long xMin;
	long xMax;
	long yMin;
	long yMax;
	
	public AABB() {
		xMin = Long.MAX_VALUE;
		yMin = Long.MAX_VALUE;
		xMax = Long.MIN_VALUE;
		yMax = Long.MIN_VALUE;
	}

	public AABB(AABB src) {
		xMin = src.xMin;
		xMax = src.xMax;
		yMin = src.yMin;
		yMax = src.yMax;
	}
	
	public boolean isValid() {
		return (xMin <= xMax) && (yMin <= yMax);
	}
	
	public void add(Coordinate l) {
		if (xMin > l.x) xMin = l.x;
		if (xMax < l.x) xMax = l.x;
		if (yMin > l.y) yMin = l.y;
		if (yMax < l.y) yMax = l.y;
	}
}
