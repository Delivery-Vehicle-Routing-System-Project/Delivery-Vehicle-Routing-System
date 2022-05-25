package DVRS;

public class Route {
	public Route(DistanceMatrix d) {
		distanceMatrix = d;
		location = new IntegerList();
		cost = 0;
		location.reserve(d.size() + 1);
	}
	
	public Route(Route src) {
		distanceMatrix = src.distanceMatrix;
		location = new IntegerList(src.location);
		cost = src.cost;
	}

	static public Route makeCopy(Route src) {
		return new Route(src);
	}

	static public Route[] makeCopy(Route[] src) {
		Route[] routes = new Route[src.length];
		for (int i=0; i<src.length; i++) {
			routes[i] = new Route(src[i]);
		}
		return routes;
	}
	
	public DistanceMatrix distanceMatrix() {
		return distanceMatrix;
	}
	
	public long getCost() {
		return cost;
	}

	public int getCWeight() {
		return cweight;
	}
	
	static public long getCost(Route[] route) {
		long totalCost = 0;
		for (Route r : route) totalCost += r.cost;
		return totalCost;
	}
	
	public int size() {
		return location.size();
	}
	
	public void add(int locationIndex) {

		assert (0 <= locationIndex) && (locationIndex < distanceMatrix.size());
		cweight = 0;
		if (!location.isEmpty()) {
			int last = location.get(location.size() - 1); 
			cost += distanceMatrix.getDistance(last, locationIndex);
			cweight = location.size() * ButtonDeliveryDetails.wTotal;
		}
		if (cweight<=250) {
			//add location when DA can carry parcel
			location.add(locationIndex);
		}
		else if (cweight>250) {
			//add warehouse location when weight exceed to back to warehouse 
			location.add(0);
		}
	}

	public void setLocationIndex(int index, int locationIndex) {
		
		// Get old index
		int oldLocation = location.get(index);
		if (oldLocation != locationIndex) {
			
			// Get previous and next location, if any
			final int invalid = -1;
			int prevLocation = (index > 0) ? location.get(index - 1) : invalid;
			int nextLocation = (index < (location.size() - 1)) ? location.get(index + 1) : invalid;

			// Update location list
			location.set(index, locationIndex);
			
			// Update cost from previous location
			if (prevLocation != invalid) {
				cost +=
					distanceMatrix.getDistance(prevLocation, locationIndex) -
					distanceMatrix.getDistance(prevLocation, oldLocation);
			}
			if (nextLocation != invalid) {
				cost +=
						distanceMatrix.getDistance(locationIndex, nextLocation) -
						distanceMatrix.getDistance(oldLocation, nextLocation);
			}
		}
	}
	
	public int getLocationIndex(int index) {
		return location.get(index);
	}
	
	public Location getLocation(int index) {
		return distanceMatrix.getLocation(location.get(index));
	}
	
	public boolean isEmpty() {
		return location.isEmpty();
	}
	
	public void clear() {
		location.clear();
		cost = 0;
	}

	@Override
	public String toString() {
		String result = Integer.toString(location.get(0));
		for (int i=1; i<location.size(); i++) {
			result += " -> " + location.get(i);
		}
		cweight = (location.size()-2) * ButtonDeliveryDetails.wTotal;
		result += " "+ cweight+"kg";
		return result + " : Distance " + cost;
	}
	
	private DistanceMatrix distanceMatrix;
	private IntegerList location;
	private long cost;
	private int cweight;
}
