package DVRS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class ButtonToggleWorkings extends JButton implements ActionListener {
	private final LocationRenderer renderer;

	static private final String labelShow = "Show Workings";
	static private final String labelHide = "Hide Workings";
	
	public ButtonToggleWorkings(LocationRenderer r) {
		super(labelShow);
		renderer = r;
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (this == e.getSource()) {
			if (renderer.showWorking) {
				renderer.showWorking = false;
				setText(labelShow);
			} else {
				renderer.showWorking = true;
				setText(labelHide);
			}
		}
	}
}
