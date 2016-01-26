package org.finomnis.instrumenttuner.visualization;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GraphWindow {

	private class GraphComponent extends JComponent{

		private static final long serialVersionUID = 1L;
		private float[] x_vals = null;
		private float[] y_vals = null;
		private float x_min = 0;
		private float x_max = 0;
		private float y_min = 0;
		private float y_max = 0;
		private boolean needs_update = true;
		private Lock l = new ReentrantLock();
		
		private BufferedImage renderBuffer;
		private GraphRenderer graphRenderer;
		
		public void setXRange(float x_min, float x_max){
			l.lock();
			this.x_min = x_min;
			this.x_max = x_max;
			needs_update = true;
			l.unlock();				
		}
		
		public void setYRange(float y_min, float y_max){
			l.lock();
			this.y_min = y_min;
			this.y_max = y_max;
			needs_update = true;
			l.unlock();				
		}
		
		public void setData(float[] x_new, float[] y_new){
			if(x_new.length != y_new.length) throw new RuntimeException("Array dimensions do not agree!");

			float[] x_copy = new float[x_new.length];
			float[] y_copy = new float[x_new.length];
			System.arraycopy(x_new, 0, x_copy, 0, x_new.length);
			System.arraycopy(y_new, 0, y_copy, 0, y_new.length);
			l.lock();
			x_vals = x_copy;
			y_vals = y_copy;
			needs_update = true;
			l.unlock();
		}
		
		public void paintComponent(Graphics g){
			
			l.lock();
			float[] c_x_vals = x_vals;
			float[] c_y_vals = y_vals;
			float c_x_min = x_min;
			float c_x_max = x_max;
			float c_y_min = y_min;
			float c_y_max = y_max;
			boolean c_needs_update = needs_update;
			needs_update = false;
			l.unlock();
			
			int winWidth = this.getWidth();
			int winHeight = this.getHeight();
			
			if(renderBuffer == null){
				renderBuffer = new BufferedImage(winWidth, winHeight, BufferedImage.TYPE_INT_RGB);
				c_needs_update = true;
			} else {
				if(renderBuffer.getHeight() != winHeight || renderBuffer.getWidth() != winWidth){
					renderBuffer = new BufferedImage(winWidth, winHeight, BufferedImage.TYPE_INT_RGB);
					c_needs_update = true;
				}
			}
			
			if(c_needs_update){
				if(c_x_vals != null && c_y_vals != null){
					graphRenderer.plotGraph(renderBuffer, c_x_vals, c_y_vals, c_x_min, c_x_max, c_y_min, c_y_max);
				}
			}
			
			g.drawImage(renderBuffer, 0, 0, this);
			
		}
		
	}
	
	private GraphComponent graphComponent;
	private JFrame window;
	
	private static final int DEFAULT_WIDTH = 300;
	private static final int DEFAULT_HEIGHT = 200;
	
	
	public GraphWindow(String name, GraphRenderer renderer){
		
		
		try {
			SwingUtilities.invokeAndWait(new Runnable(){
				public void run(){
					// Create Window
					window = new JFrame();
					window.setTitle(name);
					window.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
					
					// Create Graph
					graphComponent = new GraphComponent();
					graphComponent.graphRenderer = renderer;
					window.add(graphComponent);

					// Display
					window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	                window.setVisible(true);

				};
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		};
				
	}
	
	public void setXRange(float x_min, float x_max){
		graphComponent.setXRange(x_min, x_max);
	}
	
	public void setYRange(float y_min, float y_max){
		graphComponent.setYRange(y_min, y_max);
	}
	
	public void updateData(float[] x, float[] y){
		graphComponent.setData(x, y);
		graphComponent.repaint();
	}
	
	public void close(){
		window.setVisible(false);
		window.dispose();
	}
	
}
