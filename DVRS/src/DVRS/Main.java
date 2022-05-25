package DVRS;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.SUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jade.util.leap.ArrayList;
//Delivery Vehicle Routing System
public class Main {
	
	private static String rdAgent;
	private static String rdWeightCap;

	public static void main(String[] args) {

	// Create master routing agent
        PlatformConfiguration platformConfig = PlatformConfiguration.getDefaultNoGui();
        platformConfig.addComponent(MasterRoutingAgent.class);
        IExternalAccess routingAgent = Starter.createPlatform(platformConfig).get();

        // Get the component management service
        IComponentManagementService cms = SServiceProvider.getService(routingAgent, IComponentManagementService.class).get();
        
        //read delivery vehicle text file
        try {
        	//for Windows path
    		File file = new File("lib\\deliveryvehicle.txt");
    		//for macOs path
    		//File file = new File("lib/deliveryvehicle.txt"); 
            BufferedReader bReader = new BufferedReader (new FileReader(file));
            String fileline;
            
            while((fileline = bReader.readLine()) != null) {
                String rdWeightCap = bReader.readLine().replace("Weightcapacity: ", "");
                String AgentNo = fileline.replace("Agent: ", "");
            	String Combination = AgentNo + " " + rdWeightCap;
                CreationInfo ci = new CreationInfo(SUtil.createHashMap(new String[]{"combination"}, new Object[]{Combination}));
                
                cms.createComponent("dA", "DVRS.DeliveryAgent.class", ci);
            }
            bReader.close();
        }catch (IOException e) {
        	e.printStackTrace();
        }
	}
}
