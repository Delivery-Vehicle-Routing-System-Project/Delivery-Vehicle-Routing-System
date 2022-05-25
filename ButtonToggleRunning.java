package DVRS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class ButtonToggleRunning extends JButton implements ActionListener {
	private final SolverThread solver;

	static private final String labelPause = "Stop Solver";
	static private final String labelUnpause = "Start Solver";
	
	public ButtonToggleRunning(SolverThread s) {
		super(labelUnpause);
		solver = s;
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (this == e.getSource()) {
			if (solver.isPaused()) {
				solver.unpause();
				setText(labelPause);
			} else {
				solver.pause();
				setText(labelUnpause);
			}
		}
	}
}

