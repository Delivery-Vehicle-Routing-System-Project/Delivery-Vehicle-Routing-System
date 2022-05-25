package DVRS;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.service.IService;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.micro.annotation.*;

/**
 * A delivery agent connects to the master routing agent and receives routes.
 */
@Agent
@RequiredServices(
	@RequiredService(name="routingservices", type= RoutingService.class, multiple=true, binding=@Binding(scope=Binding.SCOPE_GLOBAL)))
@Arguments(@Argument(name="combination", description = "Capacity of this delivery agent", clazz=String.class, defaultvalue = "5"))
public class DeliveryAgent {

	@AgentArgument
	protected String combination;
	protected String Distances;
	protected String AgentNo;
	protected int capacity;
	
	@AgentService
    public void addRoutingService(RoutingService routingservice) {
		
		// Make sure the capacity is at least one
		if (capacity < 1) {
			System.out.println("Warning: Delivery Agent capacity was set as " + capacity + ". Now set to 1.");
			capacity = 1;
		}
		
		// Obtain a subscription to the master routing service
        ISubscriptionIntermediateFuture<String> subscription = routingservice.registerVehicle(combination);
        // Wait for results from the subscription
        while(subscription.hasNextIntermediateResult()) {
        	
        	// Get the latest route provided
            String route = subscription.getNextIntermediateResult();
            String platform = ((IService)routingservice).getServiceIdentifier().getProviderId().getPlatformName();
            System.out.println("Delivery Agent [" + AgentNo + "] received new route from Master Routing Agent: "+route);
            String Split[] = route.split(":",3);
            if(Split[0] == "No route")
            {	
            }
            else
            {
            	String Distance[] = Split[1].split("Distance ");
            	Distances = Distance[1];
            	RouteArrived();
            }
           
        }
    }
	
	@AgentCreated
	public void created() {
		String values[] = combination.split(" ",2);
		capacity = Integer.parseInt(values[1]);
		AgentNo = values[0];
		System.out.println("New delivery agent [" + AgentNo + "] with a capacity of " + capacity + "Kg is created");
	}
    
    public void RouteArrived()
    {
    	SolverThread s = new SolverThread();
    	if(s.isPaused())
    	{
        	Long time = Long.parseLong(Distances);
        	Long Timertime = time * 1000;
        	// Set a timer to refresh the screen from time to time
        			Timer DeliverArrived = new Timer(true);
        			DeliverArrived.scheduleAtFixedRate(new TimerTask() {
        				@Override
        				public void run() {
        					System.out.println("Delivery Agent [" + AgentNo + "] has returned to Company Warehouse after " + time + " Seconds");
        				}
        			}, Timertime, Timertime);
    	}

    }

    public static void  main(String[] args) {
        PlatformConfiguration config = PlatformConfiguration.getDefaultNoGui();
        config.setNetworkName("MattAndAkshata");
        config.setNetworkPass("NetworkPass");
        config.addComponent(DeliveryAgent.class);
        config.setAwareness(true);
        Starter.createPlatform(config).get();
    }
}
