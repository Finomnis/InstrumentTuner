package org.finomnis.instrumenttuner.computation;

public class SelfSim {

	public static float[] compute(float[] input){
		float[] result = new float[input.length - 1];
		return compute(input, result);
	}
	
	public static float[] compute(float[] input, float[] result){
		for(int lag = 0; lag < result.length; lag++){
			double sum = 0.0f;
			for(int i = lag; i < input.length; i++){
				double diff = input[i] - input[i-lag];
				sum += diff*diff; 
			}
			result[lag] = (float) (sum/(result.length - lag));
		}
		
		double sig_avg = 0.0;
		for(int j = 0; j < input.length; j++){
			sig_avg += Math.abs(input[j]);
		}
		sig_avg /= input.length;
		double val_h = sig_avg * 2.0;
		double correction_factor = 1.0f / (val_h * val_h * 2.0f / 3.0f);
		
		for(int j = 0; j < result.length; j++){
			result[j] *= correction_factor;
		}
		
		return result;
	}
	
	public static void compute(float[] input, SelfSimData selfSimData) {
		selfSimData.setData(compute(input, selfSimData.getData()));
	}
	
}
