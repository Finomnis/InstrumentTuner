package org.finomnis.instrumenttuner.visualization;

import java.awt.image.BufferedImage;

public interface GraphRenderer {

	public void plotGraph(BufferedImage renderBuffer, float[] c_x_vals, float[] c_y_vals, float c_x_min, float c_x_max,
			float c_y_min, float c_y_max, float[] marks);
	
	public static float[] getIndices(int size){
		float[] result = new float[size];
		for(int i = 0; i < result.length; i++){
			result[i] = i;
		}
		return result;
	}

}
