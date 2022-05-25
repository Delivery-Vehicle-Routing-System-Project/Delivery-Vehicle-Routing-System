package DVRS;

import java.util.Random;

public class Location {

	public final Coordinate coord;
	public final String name;

	public Location() {
		coord = new Coordinate();
		name = "";
	}

	public Location(long x, long y, String label) {
		assert label != null;
		coord = new Coordinate(x, y);
		name = label;
	}

	public Location(Location src) {
		coord = src.coord;
		name = src.name;
	}

	@Override
	public String toString() {
		return name + ":" + coord;
	}
	
	public static Location[] RandomList(int count, int range) {
		assert count > 0;
		Random rand = new Random();
		Location[] list = new Location[count];
		int limit = (range * 2) + 1;
		for (int i=0; i<count; i++) {
			long x = rand.nextInt(limit) - range;
			long y = rand.nextInt(limit) - range;
			list[i] = new Location(x, y, Integer.toString(i));
		}
		return list;
	}
}
