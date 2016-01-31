package org.finomnis.instrumenttuner.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

public class GraphPlotter implements GraphRenderer{
	
	public void plotGraph(BufferedImage buf, float[] data_x, float[] data_y, float x_min, float x_max, float y_min, float y_max, float[] marks){
		if(data_x.length != data_y.length) throw new RuntimeException("Internal Error!");
		
		int width = buf.getWidth();
		int height = buf.getHeight();
		//System.out.println("plotGraph");

		Graphics2D g2d = buf.createGraphics();
		
		// Enable Antialiasing
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							 RenderingHints.VALUE_ANTIALIAS_ON);

		// Clear
		g2d.setBackground(Color.WHITE);
		g2d.clearRect(0, 0, width, height);
		
		// Compute some constants for graph rendering
		float graph_width = width;
		float graph_height = height;
		float graph_pos_x = 0;
		float graph_pos_y = 0;
		
		float scaling_x = graph_width/(x_max - x_min);
		float scaling_y = graph_height/(y_max - y_min);
		
		g2d.setColor(Color.BLUE);
		g2d.draw(new Line2D.Double(0, height/2.0, width, height/2.0));

		// Plot the actual graph
		g2d.setColor(Color.BLACK);
		for(int i = 0; i < data_x.length-1; i++){
			float x0 = (data_x[i] - x_min) * scaling_x + graph_pos_x;
			float y0 = -(data_y[i] - y_min) * scaling_y + graph_pos_y + graph_height;
			float x1 = (data_x[i+1] - x_min) * scaling_x + graph_pos_x;
			float y1 = -(data_y[i+1] - y_min) * scaling_y + graph_pos_y + graph_height;
			g2d.draw(new Line2D.Double(x0, y0, x1, y1));
		}
		
		
		g2d.setColor(Color.RED);
		for(int i = 0; i < marks.length; i++){
			float x0 = (marks[i] - x_min) * scaling_x + graph_pos_x;
			float y0 = graph_pos_y;
			float y1 = graph_pos_y + graph_height;
			g2d.draw(new Line2D.Double(x0, y0, x0, y1));
		}
	}

}
