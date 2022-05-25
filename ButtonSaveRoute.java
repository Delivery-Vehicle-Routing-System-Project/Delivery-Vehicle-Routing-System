package DVRS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ButtonSaveRoute extends JButton implements ActionListener {
	
	private final SolverThread solver;

	public ButtonSaveRoute(SolverThread s) {
		super("Save Route");
		solver = s;
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (this == e.getSource()) {
			
			FileWriter fileWriter = null;
			BufferedWriter bufferedWriter = null;
			
			try {
			
				// Get the previous working directory
				Config config = new Config();
				File directory = config.getWorkingDirectory();
				
				// Create a file filter
				FileNameExtensionFilter ffCSV = new FileNameExtensionFilter("DVRS Files", "csv");
				FileNameExtensionFilter ffTXT = new FileNameExtensionFilter("Text Files", "txt");
				
				// Create and open file chooser dialog window
				JFileChooser fc = new JFileChooser();
				fc.addChoosableFileFilter(ffCSV);
				fc.addChoosableFileFilter(ffTXT);
				fc.setFileFilter(ffTXT);
				int result = fc.showSaveDialog(this.getTopLevelAncestor());
				
				// Record current working directory
				config.setWorkingDirectory(fc.getCurrentDirectory());
				config.save();
				
				// Check if a file was selected
				if (result == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();

					// Make sure the file has an extension type
					if (!file.getName().contains(".")) {
						if (fc.getFileFilter() == ffCSV) {
							file = new File(file.toString() + ".csv");
						} else if (fc.getFileFilter() == ffTXT) {
							file = new File(file.toString() + ".txt");
						}
					}

					// Check the type of file being generated
					boolean makeCSVFile = (fc.getFileFilter() == ffCSV) || file.getName().toLowerCase().endsWith(".csv");

					// Make sure the file can be written too
					if (!file.exists() || (file.isFile() && file.canWrite())) {
						System.out.println("Save route file: "+file.getName());
						
						// Open file writer
						fileWriter = new FileWriter(file);
						bufferedWriter = new BufferedWriter(fileWriter);
						
						// Write data to file
						Route[] routes = solver.getRoute();
						if (makeCSVFile) {
							String t;

							int maxLocations = 0;
							for (Route r : routes) {
								if (maxLocations < r.size()) maxLocations = r.size();
							}
							
							// Write headers for route cost data
							t = "";
							for (int i=0; i<routes.length; i++) t += ",\"Route"+i+"\"";
							bufferedWriter.write(t);
							bufferedWriter.newLine();
							
							// Write cost data
							t = "\"Cost\"";
							for (int r=0; r<routes.length; r++) t += ","+routes[r].getCost();
							bufferedWriter.write(t);
							bufferedWriter.newLine();
							bufferedWriter.newLine(); //Extra line break
							
							// Write headers for route location data
							t = "";
							for (int r=0; r<routes.length; r++) t += ",\"Route"+r+"\"";
							bufferedWriter.write(t);
							bufferedWriter.newLine();

							// Write route location data
							for (int i=0; i<maxLocations; i++) {
								t = "";
								for (int r=0; r<routes.length; r++) {
									t += "," + ((i < routes[r].size()) ? Integer.toString(routes[r].getLocationIndex(i)) : "");
								}
								bufferedWriter.write(t);
								bufferedWriter.newLine();
							}
							
						} else {
							
							// Write each route to the file
							int j=0;
							for (Route r : routes) {
								String t = "Route " + (j++) + ", Cost " + Long.toString(r.getCost()) + ", Path: ";
								int length = r.size();
								if (length > 0) {
									t += Integer.toString(r.getLocationIndex(0));
									for (int i=1; i<length; i++) {
										t += " -> " + Integer.toString(r.getLocationIndex(i));
									}
								}
								bufferedWriter.write(t);
								bufferedWriter.newLine();
							}
						}
					}
				}
			} catch (Exception err) {
				JOptionPane.showMessageDialog(this, "Failed to save route data to file", "Error", JOptionPane.ERROR_MESSAGE);
			} finally {

				// Close file reader
				if (bufferedWriter != null) {
					try {bufferedWriter.close();} catch (IOException err) {}
				}
				if (fileWriter != null) {
					try {fileWriter.close();} catch (IOException err) {}
				}
			}
		}
	}

}
