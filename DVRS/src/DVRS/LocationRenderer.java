package DVRS;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class LocationRenderer extends JPanel {

	private SolverThread solverThread;
	private DistanceMatrix distanceMatrix;

	public boolean showWorking;

	public LocationRenderer(SolverThread s) {
		showWorking = false;
		solverThread = s;
		distanceMatrix = s.getDistanceMatrix();
		setMinimumSize(new Dimension(50, 50));
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		
		// Get fresh data from solver thread
		distanceMatrix = solverThread.getDistanceMatrix();
		Solver solver = showWorking ? solverThread.getSolver() : null;
		Route[] routes = solverThread.getRoute();
		
		// Get 2D version of graphics handle
		Graphics2D g2D = (Graphics2D)g;
		
		// Calculate scale used to render information to the panel
		ScaleOffset scale = new ScaleOffset(getSize(), distanceMatrix);
		
		// Draw a general grid
		drawGrid(g2D, scale);
		
		// Draw the route used
		float c = 0.1f;
		for (Route r : routes) {
			drawRoute(g2D, scale, r, Color.getHSBColor(c, 0.5f, 0.9f));
			c += 0.15f;
		}
		
		// Draw solver data
		if (solver != null) {
			if (solver instanceof SolverACO) {
				drawUsage(g2D, scale, (SolverACO)solver);
			} else if (solver instanceof SolverGA) {
				drawGenome(g2D, scale, (SolverGA)solver);
			}
		}
		
		// Draw the locations within the distance matrix
		drawLocations(g2D, scale);
		
		// Draw the length of the route
		g.setColor(Color.BLACK);
		g.drawString("RouteTotalLength="+Route.getCost(routes), 5, 30);
	}

	private class ScaleOffset {
		public final Dimension size;
		public final float scale;
		public final float xOffset;
		public final float yOffset;
		
		public ScaleOffset(Dimension panelSize, DistanceMatrix dm)  {
			final int padding = 15;
			final int minSize = 10;
			
			// Make sure panel is not too small
			if ((panelSize.width < minSize) || (panelSize.height < minSize)) {
				panelSize = new Dimension(
					Math.max(panelSize.width, minSize),
					Math.max(panelSize.height, minSize));
			}
			
			// Calculate distance matrix area along access
			AABB aabb = dm.getLocationAABB();
			assert aabb.isValid() != false;
			float xScale = (float)(panelSize.width - (padding * 2)) / (float)(aabb.xMax - aabb.xMin);
			float yScale = (float)(panelSize.height - (padding * 2)) / (float)(aabb.yMax - aabb.yMin);

			// Record values
			scale = Math.min(xScale, yScale);
			xOffset = (panelSize.width / 2) - (scale * (aabb.xMax + aabb.xMin) / 2);
			yOffset = (panelSize.height / 2) - (scale * (aabb.yMax + aabb.yMin) / 2);
			size = panelSize;
		}
		
		public Coordinate Update(Coordinate c) {
			return new Coordinate(
				(long)(xOffset + (scale * c.x)),
				(long)(yOffset + (scale * c.y)));
		}
	}
	
	private void drawGrid(Graphics2D g, ScaleOffset scale) {
		final int targetGridSize = 50;

		// Get number of lines to draw
		int xMax = (int)((float)scale.size.width / targetGridSize);
		int yMax = (int)((float)scale.size.height / targetGridSize);
		
		// Get spacing between lines
		float xStep = (float)scale.size.width / xMax;
		float yStep = (float)scale.size.height / yMax;
		
		// Draw lines
		g.setColor(Color.getHSBColor(0, 0, 0.85f));
		g.setStroke(new BasicStroke(1));
		for (int x=1; x<xMax; x++) {
			g.drawLine((int)(x * xStep), 0, (int)(x * xStep), scale.size.height);
		}
		for (int y=1; y<yMax; y++) {
			g.drawLine(0, (int)(y * yStep), scale.size.width, (int)(y * yStep));
		}
	}

	private void drawLocations(Graphics2D g, ScaleOffset scale) {
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(2));
		final int z = 2;
		for (int i=0; i<distanceMatrix.size(); i++) {
			Location l = distanceMatrix.getLocation(i);
			Coordinate c = scale.Update(l.coord);
			g.drawLine(
				(int)c.x - z,
				(int)c.y - z,
				(int)c.x + z,
				(int)c.y + z);
			g.drawLine(
				(int)c.x - z,
				(int)c.y + z,
				(int)c.x + z,
				(int)c.y - z);
			g.drawString(
				l.name,
				c.x + z + 2,
				c.y);
		}
	}

	private void drawUsage(Graphics2D g, ScaleOffset scale, SolverACO solverACO) {
		g.setStroke(new BasicStroke(1));
		final int size = solverACO.size();
		final float maxUsage = Math.max(solverACO.getMaxUsage(), 0.0001f);
		for (int x=0; x<size; x++) {
			for (int y=x+1; y<size; y++) {
				float usage = solverACO.getMaxUsage(x, y);
				float usageFrac = usage / maxUsage;
				if (usageFrac > 0.001) {
					g.setColor(Color.getHSBColor(0.8f, 0.1f + (0.5f * usageFrac), 1f - (0.2f * usageFrac)));
					Coordinate a = scale.Update(distanceMatrix.getLocation(x).coord);
					Coordinate b = scale.Update(distanceMatrix.getLocation(y).coord);
					g.drawLine(
						(int)a.x,
						(int)a.y,
						(int)b.x,
						(int)b.y);
					if (usageFrac > 0.3f) {
						g.drawString(String.format("%.2f", usage), (a.x + b.x) / 2, (a.y + b.y) / 2);
					}
				}
			}
		}
		g.setColor(Color.BLACK);
		g.drawString("MaxUsage="+solverACO.getMaxUsage(), 5, scale.size.height - 5);
		g.drawString("AverageDistance="+solverACO.getAverageDistance(), 5, scale.size.height - 20);
	}

	private void drawGenome(Graphics2D g, ScaleOffset scale, SolverGA solverGA) {
		g.setColor(Color.BLACK);
		for (int i=0; i<SolverGA.parentMax; i++) {
			g.drawString("P"+i+": "+solverGA.getParentString(i), 5, scale.size.height - 5 - (20 * i));
		}
	}
	
	private void drawRoute(Graphics2D g, ScaleOffset scale, Route route, Color c) {
		final double arrowLength = 12;
		final double arrowAngle = 0.35;
		g.setColor(c);
		g.setStroke(new BasicStroke(1.8f));
		for (int i=1; i<route.size(); i++) {
			Coordinate a = scale.Update(route.getLocation(i - 1).coord);
			Coordinate b = scale.Update(route.getLocation(i).coord);
			g.drawLine(
				(int)a.x,
				(int)a.y,
				(int)b.x,
				(int)b.y);
			double angle = Math.atan2(b.y - a.y, b.x - a.x);
			g.drawLine(
				(int)b.x,
				(int)b.y,
				(int)b.x - (int)(arrowLength * Math.cos(angle + arrowAngle)),
				(int)b.y - (int)(arrowLength * Math.sin(angle + arrowAngle)));
			g.drawLine(
				(int)b.x,
				(int)b.y,
				(int)b.x - (int)(arrowLength * Math.cos(angle - arrowAngle)),
				(int)b.y - (int)(arrowLength * Math.sin(angle - arrowAngle)));
		}
	}
}
