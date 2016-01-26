package org.finomnis.instrumenttuner.visualization;

import java.awt.Color;

public class ColorTable {

	public static final ColorTable GrayScale = new ColorTable(
			new float[]{},
			new float[]{0.0f, 1.0f},
			new float[]{0.0f, 1.0f},
			new float[]{0.0f, 1.0f}
	);
	public static final ColorTable HeatMap = new ColorTable(
			new float[]{      1/7.0f, 2/7.0f, 3/7.0f, 4/7.0f, 5/7.0f, 6/7.0f      },
			new float[]{0.0f, 1.0f,   0.0f,   0.0f,   0.0f,   1.0f,   1.0f,   1.0f},
			new float[]{0.0f, 0.0f,   0.0f,   1.0f,   1.0f,   1.0f,   0.0f,   1.0f},
			new float[]{0.0f, 1.0f,   1.0f,   1.0f,   0.0f,   0.0f,   0.0f,   1.0f}
	);
	
	private final float[] pos;
	private final float[] r;
	private final float[] g;
	private final float[] b;
	
	public ColorTable(float[] pos, float[] r, float[] g, float[] b){
		this.pos = pos;
		this.r = r;
		this.g = g;
		this.b = b;
		if(pos.length + 2 != r.length || r.length != g.length || g.length != b.length){
			throw new RuntimeException("ColorTable constructor variables invalid!");
		}
	}
	
	public int getColor(float value) {
		if(value > 1) value = 1;
		if(value < 0) value = 0;
		
		int col2 = pos.length + 1;
		
		for(int i = 0; i < pos.length; i++){
			if(pos[i] > value){
				col2 = i+1;
				break;
			}
		}
		
		int col1 = col2 - 1;
		float col1_pos = 0;
		if(col1 > 0){
			col1_pos = pos[col1 - 1];
		}
		float col2_pos = 1;
		if(col2 < pos.length + 1){
			col2_pos = pos[col2 - 1];
		}
		
		float col1_dist = value - col1_pos;
		float col2_dist = col2_pos - value;
		
		
		float col1_intensity = col2_dist / (col1_dist + col2_dist);
		float col2_intensity = col1_dist / (col1_dist + col2_dist);
		
		
		float r_val = r[col1] * col1_intensity + r[col2] * col2_intensity;
		float g_val = g[col1] * col1_intensity + g[col2] * col2_intensity;
		float b_val = b[col1] * col1_intensity + b[col2] * col2_intensity;
		return new Color(r_val, g_val, b_val).getRGB();
	}

}
