package DVRS;

public class DistanceMatrix {

	public DistanceMatrix(Location[] l) {
		assert l != null;
		assert l.length > 0;
		
		// Record the location list and the number of locations there in
		size = l.length;
		location = l;
		
		// Create a new array for the distance matrix
		distance = new long[size][size];
		
		// Loop through every possible combination of two locations
		for (int x=0; x<size; x++) {
			for (int y=x+1; y<size; y++) {
				long d = calculate(x, y);
				distance[x][y] = d;
				distance[y][x] = d;
				if (maxDistance < d) maxDistance = d;
			}
		}
		
		// Set the distance values for cells where the location leads to itself
		for (int i=0; i<size; i++) {
			distance[i][i] = 0;
		}

		// Setup the location AABB
		locationAABB = new AABB();
		for (int i=0; i<size; i++) {
			locationAABB.add(location[i].coord);
		}
	}
	
	public int size() {
		return size;
	}
	
	public long getDistance(int locationA, int locationB) {
		return distance[locationA][locationB];
	}
	
	public Location getLocation(int index) {
		return location[index];
	}
	
	public long getMaxDistance() {
		return maxDistance;
	}
	
	public AABB getLocationAABB() {
		return new AABB(locationAABB);
	}

	
	//Calculates and returns the cost of travelling from location-A to location-B.
	private long calculate(int locationA, int locationB) {
		Location a = location[locationA];
		Location b = location[locationB];
		double dx = a.coord.x - b.coord.x;
		double dy = a.coord.y - b.coord.y;
		return (long)Math.sqrt((dx * dx) + (dy * dy));
	}
	
	private long[][] distance;
	private Location[] location;
	private int size;
	private long maxDistance;
	private AABB locationAABB;
}
