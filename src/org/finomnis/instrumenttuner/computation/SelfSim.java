package org.finomnis.instrumenttuner.computation;

public class SelfSim {

	public static float[] compute(float[] input){
		float[] result = new float[input.length - 1];
		for(int lag = 0; lag < result.length; lag++){
			double sum = 0.0f;
			for(int i = lag; i < input.length; i++){
				double diff = input[i] - input[i-lag];
				sum += diff*diff; 
			}
			result[lag] = (float) (sum/(result.length - lag));
		}
		return result;
	}
	
}
