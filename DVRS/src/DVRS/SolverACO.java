package DVRS;

import java.util.Random;

import DVRS.DistanceMatrix;
import DVRS.IntegerList;
import DVRS.Route;
import DVRS.Solver;
import DVRS.SolverACO;
import DVRS.SolverType;

/**
 * A solver which uses Ant Colony Optimisation (ACO) to find routes.
 */
public class SolverACO implements Solver {

	public SolverType getType() {
		return SolverType.ACO;
	}

	public Route[] run() {
		return run(40);
	}
	
	public Route[] run(int iterations) {
		final int lCount = distanceMatrix.size();
		final int vCount = vehicleCapacity.length;

		// Create a route list
		Route[] routes = new Route[vCount];
		for (int i=0; i<vCount; i++) {
			routes[i] = new Route(distanceMatrix);
		}
		
		// Create a list used to store the number of locations each
		int[] locationsVisited = new int[vCount];
		
		// When a vehicle returns to the depot it will remain parked until told otherwise
		boolean[] parked = new boolean[vCount];
		for (int v=0; v<vCount; v++) parked[v] = false;
		
		// Define values to hold the best route found thus far
		Route[] bestRoute = null;
		long bestCost = Long.MAX_VALUE;
		
		// Create list to contain the probability of visiting a specific location
		float[] toVisitProbability = new float[lCount];

		IntegerList toVisit = new IntegerList();
		toVisit.reserve(lCount);
		
		for (int it=0; it<iterations; it++) {

			// Fill the location list with all the location indices
			toVisit.clear();
			toVisit.reserve(lCount);
			for (int i=1; i<lCount; i++) {
				toVisit.push(i);
			}
			
			// Reset the route data
			// Add the starting location (location index 0)
			for (int v=0; v<vCount; v++) {
				routes[v].clear();
				routes[v].add(0);
				locationsVisited[v] = 0;
			}
			
			// Loop until location list is empty
			int v = -1;
			while (!toVisit.isEmpty()) {
				
				// Increment the vehicle index
				// Check if the vehicle is parked
				v = (v + 1) % vCount;
				if (parked[v]) {

					// Try to find an non-parked vehicle instead
					int u = v;
					do {
						u = (u + 1) % vCount;
						if (!parked[u]) {
							v = u++; // Save v and set u to something else
							break;
						}
					} while (u != v);
				
					// Check if all vehicles are parked
					if (u == v) {

						// Find vehicle with shortest travel distance and resume that one
						long t = Long.MAX_VALUE;
						for (u=0; u<vCount; u++) {
							if (t > routes[u].getCost()) {
								t = routes[u].getCost();
								v = u;
							}
						}
						parked[v] = false;
					}
				}

				// Get a shorthand reference to the route which will receive the next location
				Route r = routes[v];
				
				// Get the last location visited by this route
				int lastVisited = r.getLocationIndex(r.size() - 1);
				
				// Find the longest available distance
				float maxDistance = 0;
				for (int i=0; i<toVisit.size(); i++) {
					maxDistance = Math.max(maxDistance, distanceMatrix.getDistance(lastVisited, toVisit.get(i)));
				}

				// Calculate the probability of visiting each remaining location
				// Fill the probability list accordingly
				float pTotal = 0;
				for (int i=0; i<toVisit.size(); i++) {
					float pDistance = 1.001f - (distanceMatrix.getDistance(lastVisited, toVisit.get(i)) / maxDistance);
					float pUsage = Math.max(usage[v][lastVisited][toVisit.get(i)], 0.001f);
					toVisitProbability[i] = pDistance + pUsage;
					pTotal += toVisitProbability[i];
				}
				
				// Pick a location
				// Generate a random number and see where in the probability list it points too
				pTotal *= rnd.nextFloat();
				for (int i=toVisit.size()-1; i>=0; i--) {
					if ((pTotal > toVisitProbability[i]) && (i > 0)) {
						pTotal -= toVisitProbability[i];
					} else {
						
						// Add location to route
						r.add(toVisit.get(i));
						toVisit.removeUnordered(i);
						
						if (++locationsVisited[v] >= vehicleCapacity[v]) {
							locationsVisited[v] = 0;
							r.add(0);
							parked[v] = true;
						}
						
						break;
					}
				}
			}
			
			// Add a return trip to the depot for each vehicle not already there
			for (v=0; v<vCount; v++) {
				Route r = routes[v];
				if (r.getLocationIndex(r.size() - 1) != 0) r.add(0);
			}
			
			// Calculate the total travel distance of all vehicles
			long totalCost = Route.getCost(routes);
			
			// Update average route distance
			if (costAverage == 0) {
				costAverage = totalCost;
			} else {
				costAverage = ((costAverage * 99) + totalCost) / 100;
			}
			
			
			// Calculate score for this route
			float score = (float)costAverage / (float)totalCost;

			// Update usage matrix
			reduce(0.999f);
			if (score >= 1.5f) increase(routes, 50.0f); else
			if (score >= 1.2f) increase(routes, 5.0f); else
			if (score >= 1.1f) increase(routes, 1.0f); else
			if (score >= 1.0f) increase(routes, 0.01f); else
			if (score >= 0.9f) increase(routes, 0.001f);

			// Check if route is acceptable
			if (totalCost < bestCost) {
				bestCost = totalCost;
				if (bestRoute == null) {
					bestRoute = Route.makeCopy(routes);
				} else {
					Route[] temp = bestRoute;
					bestRoute = routes;
					routes = temp;
				}
			}
		}
		
		// Record route
		return bestRoute;
	}

	public long getAverageDistance() {
		return costAverage;
	}

	public void increase(Route[] route, float amount) {
		assert route.length == vehicleCapacity.length;
		for (int v=0; v<route.length; v++) {
			Route r = route[v];
			for (int i=1; i<r.size(); i++) {
				int a = r.getLocationIndex(i - 1);
				int b = r.getLocationIndex(i);
				usage[v][a][b] += amount;
				if (usageMax < usage[v][a][b]) usageMax = usage[v][a][b];
			}
		}
	}
	
	private void reduce(float multiplier) {
		usageMax = usageMaxSmallest;
		for (int v=0; v<vehicleCapacity.length; v++) {
			for (int a=0; a<size; a++) {
				for (int b=0; b<size; b++) {
					float u = usage[v][a][b] * multiplier;
					usage[v][a][b] = u;
					if (usageMax < u) usageMax = u;
				}
			}
		}
	}

	public int size() {
		return size;
	}
	
	public float getMaxUsage(int locationA, int locationB) {
		float result = 0;
		for (int v=0; v<vehicleCapacity.length; v++) {
			if (result < usage[v][locationA][locationB]) result = usage[v][locationA][locationB];
			if (result < usage[v][locationB][locationA]) result = usage[v][locationB][locationA];
		}
		return result;
	}
	
	public float getMaxUsage() {
		return usageMax;
	}

	public SolverACO(DistanceMatrix d, int[] vehicleCapacity) {
		assert d != null;
		assert d.size() > 0;
		
		// Record the size of the matrix
		distanceMatrix = d;
		
		// Setup general values
		size = d.size();
		usageMax = usageMaxSmallest;
		costAverage = 0;
		this.vehicleCapacity = (vehicleCapacity != null) ? vehicleCapacity : new int[]{Integer.MAX_VALUE};

		// Create a new random number generator
		rnd = new Random();
		rnd.nextFloat(); // Run once
		
		// Create a new array for the path-usage matrix
		// Only the top half of the matrix is used
		usage = new float[this.vehicleCapacity.length][size][size];
		for (int v=0; v<this.vehicleCapacity.length; v++) {
			for (int x=0; x<size; x++) {
				for (int y=0; y<size; y++) {
					usage[v][x][y] = 0;
				}
			}
		}
	}

	public SolverACO(SolverACO src) {
		assert src != null;
		
		// Record the size of the matrix
		distanceMatrix = src.distanceMatrix;
		
		// Setup general values
		size = src.size;
		usageMax = src.usageMax;
		costAverage = src.costAverage;
		vehicleCapacity = src.vehicleCapacity;

		// Create a new random number generator
		rnd = new Random();
		rnd.nextFloat();
		
		// Create a new array for the path-usage matrix
		// Only the top half of the matrix is used
		usage = new float[vehicleCapacity.length][size][size];
		for (int v=0; v<this.vehicleCapacity.length; v++) {
			for (int x=0; x<size; x++) {
				for (int y=0; y<size; y++) {
					usage[v][x][y] = src.usage[v][x][y];
				}
			}
		}
	}
	
	static final private float usageMaxSmallest = 0.001f;
	
	final private DistanceMatrix distanceMatrix;
	final private int size;
	final private int[] vehicleCapacity;
	private float[][][] usage;
	private float usageMax;
	private Random rnd;
	private long costAverage;
}

