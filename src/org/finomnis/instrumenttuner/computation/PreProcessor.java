package org.finomnis.instrumenttuner.computation;

public class PreProcessor {

	private int dataSize;
	public final float samplingFrequency;
	private float[] buf;
	
	public PreProcessor(float samplingFrequency){
		this.samplingFrequency = samplingFrequency;
		setDataSize(10);
	}
	
	private void setDataSize(int size){
		this.dataSize = size;
		this.buf = new float[size];
	}
	
	public float getWithBordersContinue(float[] data, int pos){
		
		if(pos < 0) return data[0];
		if(pos >= data.length) return data[data.length - 1];
		
		return data[pos];
		
	}
	
	public float[] preProcess(float[] data, int averagingNumNeighbors) {
		if(dataSize != data.length){
			setDataSize(data.length);
		}
		
		for(int i = 0; i < data.length; i++){
			buf[i] = data[i];
		}
		
		for(int i = 0; i < data.length; i++){
			data[i] = 0;
			for(int j = i - averagingNumNeighbors; j <= i+averagingNumNeighbors; j++){
				data[i] += getWithBordersContinue(buf, j);
			}
			data[i] /= 2*averagingNumNeighbors + 1;
		}
		
		return data;
	}
	
}
