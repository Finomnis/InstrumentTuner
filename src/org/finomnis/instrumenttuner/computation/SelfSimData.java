package org.finomnis.instrumenttuner.computation;

public class SelfSimData {

	private float[] data;
	private final float samplingRate;
	
	public SelfSimData(float samplingRate){
		this.samplingRate = samplingRate;
	}
	
	public void setData(float[] data){
		this.data = data;
	}
	
	public float[] getFrequencyValues(float freq){
		float period = samplingRate/freq;
		
		float pos_max = data.length*0.7f;
		int num_values = (int)(pos_max / period);
		
		float[] result = new float[num_values];
	
		for(int i = 0; i < result.length; i++){
			result[i] = getInterpolatedValue(period * (i+1));
		}
		
		return result;
	}
	
	public float getFrequencyCorelation(float freq){
		float period = samplingRate/freq;
		
		float pos_max = data.length*0.7f;
		int num_values = (int)(pos_max / period);
		
		float result = 0.0f;
		
		for(int i = 0; i < num_values; i++){
			float currentVal = getInterpolatedValue(period * (i+1));
			if(currentVal > result) result = currentVal;
		}
		
		return Math.max(0.0f, 1.0f - result);		
	}
	
	
	public float getInterpolatedValue(float pos){
		int p0 = (int) pos;
		int p1 = p0 + 1;
		
		if(p0 < 0 || p1 >= data.length){
			throw new RuntimeException("Internal Error!");
		}
		
		float p1_intensity = pos - p0;
		float p0_intensity = p1 - pos;
		
		return data[p0] * p0_intensity + data[p1] * p1_intensity;
	}
	
	
}
