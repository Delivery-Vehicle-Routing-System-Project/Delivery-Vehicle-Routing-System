
package DVRS;

public class SolverThread extends Thread {
	
	public SolverThread() {
		paused = true;
		distanceMatrix = new DistanceMatrix(Location.RandomList(1,  10));
		vehicleCapacity = null;
		solver = new SolverGA(distanceMatrix, vehicleCapacity);
		resetRoute();
		setDaemon(true); // This thread should not stop the program from terminating
	}
	
	public void run() {
		Solver localSolver;
		
		while (true) {
			
			// Check if stop
			synchronized(this) {
				if (paused) {
					try {
						wait();
					} catch (Exception e) {
					}
					continue;
				} else {
					
					localSolver = solver;
				}
			}
			
			// Run solver
			Route[] newRoute = localSolver.run();
			
			// Get the total cost of all the routes combined
			int newTotalCost = 0;
			for (Route r : newRoute) newTotalCost += r.getCost();
			
			// Check if new route is better than previous route
			synchronized(this) {
				if (localSolver == solver) {
					if ((totalCost >= newTotalCost) || (totalCost < 1)) {
						route = newRoute;
						totalCost = newTotalCost;
					}
				} else {
					resetRoute();
				}
			}
		}
	}
	
	public boolean isPaused() {
		synchronized(this) {
			return paused;
		}
	}
	
	public void pause() {
		synchronized(this) {
			paused = true;
		}
	}
	
	public void unpause() {
		synchronized(this) {
			paused = false;
			notify();
		}
	}
	
	public void setDistanceMatrix(DistanceMatrix dm) {
		synchronized(this) {
			distanceMatrix = dm;
			recreateSolver(solver.getType());
		}
	}

	public void setSolverType(SolverType t) {
		synchronized(this) {
			if (t != solver.getType()) {
				recreateSolver(t);
			}
		}
	}
	
	public SolverType getSolverType() {
		synchronized(this) {
			return solver.getType();
		}
	}
	
	public Route[] getRoute() {
		synchronized(this) {
			return Route.makeCopy(route);
		}
	}

	public DistanceMatrix getDistanceMatrix() {
		synchronized(this) {
			return distanceMatrix;
		}
	}

	public Solver getSolver() {
		synchronized(this) {
			switch (solver.getType()) {
			case ACO:
				return new SolverACO((SolverACO)solver);
			case GA:
				return new SolverGA((SolverGA)solver);
			default:
				return new SolverGA(distanceMatrix, vehicleCapacity);
			}
		}
	}

	public int addVehicle(int capacity) {
		capacity = Math.max(capacity, 1);
		synchronized(this) {
	    	if (vehicleCapacity == null) {
	    		vehicleCapacity = new int[1];
	    		vehicleCapacity[0] = capacity;
	    	} else {
	    		int[] temp = new int[vehicleCapacity.length + 1];
	    		for (int i=0; i<vehicleCapacity.length; i++) temp[i] = vehicleCapacity[i];
	    		temp[vehicleCapacity.length] = capacity;
	    		vehicleCapacity = temp;
	    	}
	    	recreateSolver(solver.getType());
	    	return vehicleCapacity.length - 1;
		}
	}
	
	private void recreateSolver(SolverType t) {
		switch (t) {
		case ACO:
			solver = new SolverACO(distanceMatrix, vehicleCapacity);
			break;
		case GA:
			solver = new SolverGA(distanceMatrix, vehicleCapacity);
			break;
		default:
			System.out.println("Found unknown solver type while recreating solver");
			solver = new SolverGA(distanceMatrix, vehicleCapacity);
		}
		resetRoute();
	}
	
	private void resetRoute() {
		route = new Route[] {new Route(distanceMatrix)};
		totalCost = 0;
	}
	
	private boolean paused;
	private DistanceMatrix distanceMatrix;
	private Solver solver;
	private Route[] route;
	private int totalCost;
	private int[] vehicleCapacity;
}
