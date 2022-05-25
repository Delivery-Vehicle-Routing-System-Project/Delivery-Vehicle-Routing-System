package DVRS;

public interface Solver {

	
	//Get the type of solver.
	
	public SolverType getType();
	
	
	//Calculate and return a route.
	public Route[] run();

	public Route[] run(int iterations);
}
