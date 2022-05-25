package DVRS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter; 
import java.io.IOException;

@SuppressWarnings("serial")
public class ButtonDeliveryDetails extends JButton implements ActionListener {
	public static Integer wTotal = null;
	public static Integer pTotal = null;
	private final JFrame parentFrame;
	private final SolverThread solver;

	public ButtonDeliveryDetails(JFrame parent, SolverThread s) {
		super("Delivery Details");
		parentFrame = parent;
		solver = s;
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (this == e.getSource()) {
			try {
				
				// Show input dialog
				String input1 = JOptionPane.showInputDialog(this, "Enter number of Parcels");
				String input2 = JOptionPane.showInputDialog(this, "Enter Number of Weight per Parcel(Kg)");
				if ((input1 != null) && !input1.isBlank()) {
					wTotal = Integer.valueOf(input2);
					final int weightTotal = Integer.valueOf(input1)*Integer.valueOf(input2);
					int newWeightTotal;
					int newinput1 = 0;
					if (weightTotal>1000) {
						System.out.println("The DAs unable to handle "+weightTotal+"kg of parcel.");
						for (int i = 1; i < Integer.valueOf(input1); i++) {
							newWeightTotal = i*Integer.valueOf(input2);
							if(newWeightTotal<=1000) {
								newinput1 = i;
							}else {
								System.out.println(Integer.valueOf(input1)-newinput1+" parcel will be pending for next delivery arrangement.");
								newinput1 = i -1;
								break;
							}
						}
						input1 = String.valueOf(newinput1);
						System.out.println("The MRA only arrange "+input1+" parcel which are expected to be delivered.");
					}
					pTotal = Integer.valueOf(input1);
					int count = Integer.parseInt(input1);
						
			        	//for Windows path
		        		//File file = new File("lib\\location.txt");
		        		//for macOs path
		        		File file = new File("lib/location.txt"); 
			           	BufferedReader bReader = new BufferedReader (new FileReader(file));
			            
					String LocationSelected;
					String [] Split;
					int LocCount = 1;
					Location[] list = new Location[count + 1];
			            
			        while(LocCount <= count + 1) {
						LocationSelected = bReader.readLine().replace("Location: (", "");
						Split = LocationSelected.split(",");
						Split[1] = Split[1].replace(")", "");
						int x = Integer.parseInt(Split[0]);
						int y = Integer.parseInt(Split[1]);

						if(LocCount == 1)
						{
							list[0] = new Location(x, y, "Company Warehouse");
							System.out.println("Company Warehouse Location: (" + x + "," + y + ")");
						}
								
						else
						{
							list[LocCount-1] = new Location(x, y, Integer.toString(LocCount-1));
							System.out.println(LocCount - 1 + "st Destination: (" + x + "," + y + ")");
						}
						LocCount += 1;
	
					}
					bReader.close();
					solver.setDistanceMatrix(new DistanceMatrix(list));

					//pass the list of items from DA to MRA (counts)
					parentFrame.repaint();

				}
			} catch (NumberFormatException err) {
				JOptionPane.showMessageDialog(this, "Invalid decimal number", "Error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception err) {
				JOptionPane.showMessageDialog(this, err.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
