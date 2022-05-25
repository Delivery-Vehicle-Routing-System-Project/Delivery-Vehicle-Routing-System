package DVRS;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

import DVRS.ButtonDeliveryDetails;
import DVRS.ButtonToggleSolver;
import DVRS.ButtonToggleWorkings;
import DVRS.ButtonToggleRunning;

@SuppressWarnings("serial")
public class Gui extends JFrame {

	public Gui(SolverThread s) {

		// Setup location renderer panel
		LocationRenderer locationPanel = new LocationRenderer(s);

		// Setup control panel
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(5, 1)); //grid size
		controlPanel.add(new ButtonDeliveryDetails(this, s)); 
		controlPanel.add(new ButtonSaveRoute(s));
		controlPanel.add(new ButtonToggleSolver(this, s));
		controlPanel.add(new ButtonToggleRunning(s));
		controlPanel.add(new ButtonToggleWorkings(locationPanel));

		// Setup top split panel
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(controlPanel, BorderLayout.WEST); //Direction of the buttons
		topPanel.add(locationPanel, BorderLayout.CENTER);

		// Setup this JFrame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(800,500)); //dimension of the frame
		getContentPane().setLayout(new GridLayout());
		getContentPane().add(topPanel);
		setMinimumSize(new Dimension(200, 100)); 
		
		// Final command before returning
		pack();

		// Set a timer to refresh the screen from time to time
		refreshTimer = new Timer(true);
		refreshTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				repaint();
			}
		}, 250, 250);
	}
	
	private Timer refreshTimer;
}
