package DVRS;

import java.util.Random;

import DVRS.DistanceMatrix;
import DVRS.IntegerList;
import DVRS.Route;
import DVRS.Solver;
import DVRS.SolverGA;
import DVRS.SolverType;

public class SolverGA implements Solver {

	static final public int parentMax = 2;
	
	static final public int newCandidateCount = 20;

	public SolverType getType() {
		return SolverType.GA;
	}

	public Route[] run() {
		return run(5);
	}
	
	public Route[] run(int iterations) {
		final int vMax = vehicleCapacity.length;

		// Get total number of candidates
		final int candidates = newCandidateCount + parentMax;
		
		// Create list to hold candidate genomes and routes
		IntegerList[] candidateGenome = new IntegerList[candidates];
		Route[][] candidateRoute = new Route[candidates][vMax];

		// Check if the first two parents are identical
		// If there are more than two parents then ignore the rest
		if (parentGenome[0].isEqual(parentGenome[1])) {
			parentGenome[0] = generateRandom();
			parentRoute[0] = decode(parentGenome[0]);
		}
		
		// Loop for the requested number of iterations
		// A new route will be calculated on each iteration
		for (int it=0; it<iterations; it++) {

			// Generate and store new candidate genomes and routes
			for (int c=0; c<newCandidateCount; c++) {
				candidateGenome[c] = generateChild(parentGenome[c % parentMax], parentGenome[(c + 1) % parentMax]);
				candidateRoute[c] = decode(candidateGenome[c]);
			}
		
			// Add parent genomes to candidate list
			for (int p=0; p<parentMax; p++) {
				candidateGenome[newCandidateCount + p] = parentGenome[p];
				candidateRoute[newCandidateCount + p] = parentRoute[p];
			}
			
			// Loop through candidate list and find those with the best cost
			// Record the best candidates within parent list (they become the new parents)
			for (int p=0; p<parentMax; p++) {
				
				// Loop through all candidates
				// Record one with the better score
				// Skip any candidates which are null (have already been selected)
				int bestCandidate = p;
				long bestCost = Long.MAX_VALUE;
				for (int c=p+1; c<candidates; c++) {
					if (candidateRoute[c] != null) {
						long cost = Route.getCost(candidateRoute[c]);
						if (bestCost > cost) {
							bestCost = cost;
							bestCandidate = c;
						}
					}
				}
				
				// Record best candidate within parent list
				// Null the candidate so that it is not selected again
				parentGenome[p] = candidateGenome[bestCandidate];
				parentRoute[p] = candidateRoute[bestCandidate];
				candidateGenome[bestCandidate] = null;
				candidateRoute[bestCandidate] = null;
			}
		}
		
		// Search the parent list and return the best one
		// Return best parent route
		int bestParent = 0;
		long bestCost = Long.MAX_VALUE;
		for (int p=1; p<parentMax; p++) {
			long cost = Route.getCost(parentRoute[p]);
			if (bestCost > cost) {
				bestCost = cost;
				bestParent = p;
			}
		}
		return parentRoute[bestParent];
	}
	
	private IntegerList generateChild(IntegerList parentA, IntegerList parentB) {

		// Get number of locations
		final int n = distanceMatrix.size();
		
		// Stupidity check
		assert (parentA != null) && (parentA.size() == n);
		assert (parentB != null) && (parentB.size() == n);

		// Create child genome
		IntegerList child = new IntegerList();
		child.reserve(n);

		// Calculate crossover point for genome
		// The child will consist of data from parent-A before the crossover, and parent-B after the crossover
		final int c = 1 + rnd.nextInt(n - 2);

		// Create a list of all available locations
		IntegerList allLocations = new IntegerList();
		allLocations.reserve(n);
		for (int i=1; i<n; i++) allLocations.add(i);

		// Copy values from parent-A, up to crossover point
		// Also remove the visited locations from the location list
		for (int g=0; g<c; g++) {
			int l = parentA.get(g);

			// Add location to child
			child.add(l);
			
			// Remove location from list of all locations
			int toRemove = allLocations.find(l);
			if (toRemove != -1) allLocations.removeUnordered(toRemove);
		}
	
		IntegerList missedLocations = new IntegerList(allLocations);
		for (int i=c; i<parentB.size(); i++) {
			int toRemove = missedLocations.find(parentB.get(i));
			if (toRemove != -1) missedLocations.removeUnordered(toRemove);
		}

		for (int g=c; g<parentB.size(); g++) {
			int i = parentB.get(g);

			int f = allLocations.find(i);
			if (f >= 0) {

				// Add it to the child genome
				child.add(i);
				
				// Remove the location from list of all locations
				allLocations.removeUnordered(allLocations.find(i));
				
			} else {
				
				// Add a missed location to the genome instead
				if (!missedLocations.isEmpty()) child.add(missedLocations.pop()); else {assert false; assert true;}
			}
		}
		
		// Add any locations which are missing
		while (!missedLocations.isEmpty()) {
			child.add(missedLocations.pop());
		}

		// Mutate the list, if required
		float mutateThreshold = (parentA.isEqual(parentB) ? 1.0f : 0.01f);
		while (rnd.nextFloat() < mutateThreshold) {
			mutateThreshold *= 0.8f;
			
			// Swap two random locations within the list
			int indexA = rnd.nextInt(n - 1);
			int indexB = rnd.nextInt(n - 1);
			int locationA = child.get(indexA);
			int locationB = child.get(indexB);
			child.set(indexA, locationB);
			child.set(indexB, locationA);
		}

		// Return result
		return child;
	}

	private IntegerList generateRandom() {
		
		// Get number of locations
		final int n = distanceMatrix.size();
		
		// Fill the integer list with all the location indices
		IntegerList unused = new IntegerList();
		unused.reserve(n - 1);
		for (int i=1; i<n; i++) {
			unused.push(i);
		}
		
		// Create a result list to use for the genome
		// Add all the location indices randomly to the genome
		IntegerList genome = new IntegerList();
		genome.reserve(n - 1);
		while (!unused.isEmpty()) {
			int i = rnd.nextInt(unused.size());
			genome.add(unused.get(i));
			unused.removeUnordered(i);
		}
		return genome;
	}
	
	public String getParentString(int index) {
		if (parentGenome[index].isEmpty()) {
			return "";
		} else {
			IntegerList list = parentGenome[index];
			String result = Integer.toString(list.get(0));
			for (int i=1; i<list.size(); i++) result += "," + list.get(i);
			return result;
		}
	}
	
	private Route[] decode(IntegerList genome) {
		final int vMax = vehicleCapacity.length;
		
		// Create route list
		Route[] route = new Route[vMax];
		for (int v=0; v<vMax; v++) route[v] = new Route(distanceMatrix);

		// Create a list used to store the number of locations each
		int[] locationsVisited = new int[vMax];
		for (int v=0; v<vMax; v++) locationsVisited[v] = 0;
		
		// Add the starting location to each route
		for (Route r : route) r.add(0);
		
		IntegerList pending = new IntegerList(genome);
		
		int v = -1;
		while (!pending.isEmpty()) {

			// Increment the vehicle index
			v = (v + 1) % vMax;
	
			// Check if the vehicle has visited the maximum number of locations
			if (locationsVisited[v]++ < vehicleCapacity[v]) {

				// Max locations not yet reached
				// Add location to route
				route[v].add(pending.pop());
					
			} else {
				
				// Max locations reached
				// Add a trip back to the depot
				locationsVisited[v] = 0;
				route[v].add(0);
			}
		}
		
		// Add the return depot trip to the vehicles, if required
		for (Route r : route) {
			if (r.getLocationIndex(r.size() - 1) != 0) r.add(0);
		}
		
		if (genome.size() != (distanceMatrix.size() - 1)) {
			System.out.println("---------- Genome size "+genome.size()+","+distanceMatrix.size());
		}
		
		return route;
	}
	
	public SolverGA(DistanceMatrix d, int[] vehicleCapacity) {
		assert d != null;
		assert d.size() > 0;
		
		// Record general values
		distanceMatrix = d;
		this.vehicleCapacity = (vehicleCapacity != null) ? vehicleCapacity : new int[]{Integer.MAX_VALUE};

		// Create a new random number generator
		rnd = new Random();
		rnd.nextFloat();

		// Create parent lists
		parentGenome = new IntegerList[parentMax];
		parentRoute = new Route[parentMax][this.vehicleCapacity.length];
		for (int i=0; i<parentMax; i++) {
			parentGenome[i] = generateRandom();
			parentRoute[i] = decode(parentGenome[i]);
		}
	}
	
	public SolverGA(SolverGA src) {
		assert src != null;

		// Record general values
		distanceMatrix = src.distanceMatrix;
		vehicleCapacity = src.vehicleCapacity;

		// Create a new random number generator
		rnd = new Random();
		rnd.nextFloat();
		
		// Create parent lists
		parentGenome = new IntegerList[parentMax];
		parentRoute = new Route[parentMax][vehicleCapacity.length];
		for (int i=0; i<parentMax; i++) {
			parentGenome[i] = new IntegerList(src.parentGenome[i]);
			parentRoute[i] = Route.makeCopy(src.parentRoute[i]);
		}
	}
	
	final private DistanceMatrix distanceMatrix;
	final private int[] vehicleCapacity;
	private IntegerList[] parentGenome;
	private Route[][] parentRoute;
	private Random rnd;
}
