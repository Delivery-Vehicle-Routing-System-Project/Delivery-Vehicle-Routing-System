package DVRS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class ButtonToggleSolver extends JButton implements ActionListener {
	private final JFrame parentFrame;
	private final SolverThread solver;

	public ButtonToggleSolver(JFrame parent, SolverThread s) {
		parentFrame = parent;
		solver = s;
		addActionListener(this);
		setText(getSolverType());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (this == e.getSource()) {
			switch (solver.getSolverType()) {
			case ACO:
				solver.setSolverType(SolverType.GA);
				break;
			case GA:
				solver.setSolverType(SolverType.ACO);
				break;
			}
			setText(getSolverType());
			parentFrame.repaint();
		}
	}
	
	private String getSolverType() {
		return "Solver: " + solver.getSolverType().toString();
	}
}

