package DVRS;

import java.util.ArrayList;
import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IExecutionFeature;
import jadex.bridge.service.annotation.*;
import jadex.commons.future.IFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.SubscriptionIntermediateFuture;
import jadex.commons.future.TerminationCommand;
import jadex.micro.annotation.*;

@Agent
@Service
@ProvidedServices(@ProvidedService(type= RoutingService.class))
public class MasterRoutingAgent implements RoutingService {
	
	public ISubscriptionIntermediateFuture<String> registerVehicle(String capacity) {

		String Combination[] = capacity.split(" ",2);
		int VeCap = Integer.valueOf(Combination[1]);
		int AgentNo = Integer.valueOf(Combination[0]);
		
        // Add the capacity of the new vehicle
    	int index = solver.addVehicle(VeCap);
    	
    	// Record the new vehicle data
    	System.out.println("Master Routing Agent received new delivery agent [" + AgentNo + "], capacity " + VeCap +"Kg");
    	Vehicle vehicle = new Vehicle(VeCap, index);
    	vehicles.add(vehicle);
    	
    	// Get result which will be returned from method
    	SubscriptionIntermediateFuture<String> result = vehicle.subscriber;
    	
        result.setTerminationCommand(new TerminationCommand() {
            public void terminated(Exception reason) {
                System.out.println("removed subscriber due to: "+reason);
                for (int i=0; i<vehicles.size();) {
                	if (vehicles.get(i).subscriber == result) {
                		vehicles.remove(i);
                	} else {
                		i++;
                	}
                }
            }
        });
        return result;
    }

	@AgentBody
	public void agentBody(IInternalAccess ia) {
		System.out.println("Master Routing Agent starting.");

		// Start solver thread
		solver.start();
		
		// Start and start GUI
		Gui gui = new Gui(solver);
		gui.setVisible(true);
		
		// Get the execution feature for this agent
		// This will allow the scheduling of some callback code
		IExecutionFeature exeFeat = ia.getComponentFeature(IExecutionFeature.class);
		
		// Schedule a recurring agent step to provide subscribers with routing information
		exeFeat.repeatStep(5000, 5000, ia1 -> {
			
			// Get the route list
			Route[] route = solver.getRoute();
			
			// Notify all subscribers
			for(Vehicle v : vehicles) {
				
				// Add the current route to the intermediate result
				if ((route != null) && (v.index < route.length) && (route[v.index].getCost() > 0)) {
					v.subscriber.addIntermediateResultIfUndone(route[v.index].toString());
				} else {
					v.subscriber.addIntermediateResultIfUndone("No route");
				}
			}
			return IFuture.DONE;
		});
	}

    public static void  main(String[] args) {
        PlatformConfiguration config = PlatformConfiguration.getDefaultNoGui();
        config.setNetworkName("MattAndAkshata");
        config.setNetworkPass("NetworkPass");
        config.addComponent(MasterRoutingAgent.class);
        config.setAwareness(true);
        Starter.createPlatform(config).get();
    }
	
	public MasterRoutingAgent() {
		vehicles = new ArrayList<Vehicle>();
		solver = new SolverThread();
	}

	public class Vehicle {
		SubscriptionIntermediateFuture<String> subscriber;
		int capacity;
		int index;
		public Vehicle(int capacity, int index) {
			this.subscriber = new SubscriptionIntermediateFuture<String>();
			this.capacity = capacity;
			this.index = index;
		}
	}
	
    protected ArrayList<Vehicle> vehicles;
    SolverThread solver;
}
