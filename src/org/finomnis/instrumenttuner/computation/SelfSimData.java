package org.finomnis.instrumenttuner.computation;

public class SelfSimData {

	private float[] data;
	public final float samplingRate;
	
	public SelfSimData(float samplingRate){
		this.samplingRate = samplingRate;
	}
	
	
	public float[] getData(){
		return data;
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
		return GeneralMath.interpolate(data, pos);
	}


	public void clearData() {
		for(int i = 0; i < data.length; i++){
			data[i] = 0;
		}
	}


	public void addSmooth(SelfSimData other, float smoothingFactor) {
		if(data.length != other.data.length){
			throw new RuntimeException("Internal Error!");
		}
		for(int i = 0; i < data.length; i++){
			data[i] = smoothingFactor*data[i] + (1-smoothingFactor) * other.data[i];
		}
	}
	
	
}
