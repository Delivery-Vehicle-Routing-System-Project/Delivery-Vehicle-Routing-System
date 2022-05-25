package DVRS;

import jadex.commons.future.ISubscriptionIntermediateFuture;

public interface RoutingService {
	
	
	 //Subscribe to the master routing agent to receive routing information.
	 //This is used by delivery agents to inform the master that they exist.
	 
	public ISubscriptionIntermediateFuture<String> registerVehicle(String combination);

}
