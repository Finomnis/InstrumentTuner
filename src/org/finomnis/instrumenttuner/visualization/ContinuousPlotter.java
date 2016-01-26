package org.finomnis.instrumenttuner.visualization;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ContinuousPlotter implements GraphRenderer{
	
	private BufferedImage moveBuffer = null;
	double[] bins = null;
	int[] bincount = null;
	private ColorTable colorTable;
	
	public ContinuousPlotter(ColorTable colorTable){
		this.colorTable = colorTable;
	}
	
	public void plotGraph(BufferedImage buf, float[] data_x, float[] data_y, float x_min, float x_max, float y_min, float y_max){
		if(data_x.length != data_y.length) throw new RuntimeException("Internal Error!");
		
		int width = buf.getWidth();
		int height = buf.getHeight();
		
		if(moveBuffer == null){
			moveBuffer = new BufferedImage(width-1, height, buf.getType());
			bins = new double[height];
			bincount = new int[height];
		} else if (moveBuffer.getWidth() != width - 1 || moveBuffer.getHeight() != height){
			moveBuffer = new BufferedImage(width-1, height, buf.getType());
			bins = new double[height];
			bincount = new int[height];
		}
		
		Graphics2D g2d_buf = buf.createGraphics();
		Graphics2D g2d_move = moveBuffer.createGraphics();
		
		g2d_move.drawImage(buf, 0, 0, width-1, height, 1, 0, width, height, null);
		g2d_buf.drawImage(moveBuffer, 0, 0, width-1, height, null);
		
		/*for(int y = 0; y < height; y++){
			buf.setRGB(width-1, y, (int) (Math.random() * 256*256*256));
		}*/

		for(int i = 0; i < bins.length; i++){
			bins[i] = 0.0;
			bincount[i] = 0;
		}

		float scaling_x = height/(x_max - x_min);
		float scaling_y = 1/(y_max - y_min);
		for(int i = 0; i < data_x.length-1; i++){
			float x0 = (data_x[i] - x_min) * scaling_x;
			float y0 = (data_y[i] - y_min) * scaling_y;
			int bin_id = Math.round(x0);
			if(bin_id < 0) continue;
			if(bin_id >= bins.length) continue;
			
			double items_in_bin = bincount[bin_id];
			double prefactor = items_in_bin / (items_in_bin+1);
			bins[bin_id] = prefactor * bins[bin_id] + (1.0 - prefactor) * y0;
			
			bincount[bin_id] ++;
		}
		
		// interpolate
		
		for(int i = 0; i < height; i++){
			double intensity = 0;
			if(bincount[i] == 0){
				//System.out.println("Empty bin " + i);
				int next_bin_left = i - 1;
				while(next_bin_left >= 0){
					if(bincount[next_bin_left] > 0) break;
					next_bin_left--;
				}
				int next_bin_right = i + 1;
				while(next_bin_right < bincount.length){
					if(bincount[next_bin_right] > 0) break;
					next_bin_right++;
				}
				
				if(next_bin_left < 0 && next_bin_right >= bincount.length){
					intensity = 0;
				} else if (next_bin_left < 0){
					intensity = bins[next_bin_right];
				} else if (next_bin_right >= bincount.length) {
					intensity = bins[next_bin_left];
				} else {
					int dist_right = next_bin_right - i;
					int dist_left = i - next_bin_left;
					intensity = (dist_left * bins[next_bin_right] + dist_right * bins[next_bin_left]) / (dist_left + dist_right);
				}
				
			} else {
				intensity = bins[i];
			}
			
			if(intensity > 1) intensity = 1;
			if(intensity < 0) intensity = 0;
			
			float col = (float) intensity;
			buf.setRGB(width-1, height - 1 - i, colorTable.getColor(col));
		}
		
		
	}

}
