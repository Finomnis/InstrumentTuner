package org.finomnis.instrumenttuner.visualization;

import java.awt.image.BufferedImage;

public interface GraphRenderer {

	public void plotGraph(BufferedImage renderBuffer, float[] c_x_vals, float[] c_y_vals, float c_x_min, float c_x_max,
			float c_y_min, float c_y_max);

}
