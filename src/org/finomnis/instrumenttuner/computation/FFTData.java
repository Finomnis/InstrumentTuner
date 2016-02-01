package org.finomnis.instrumenttuner.computation;

public class FFTData {

	public final float samplingRate;
	
	private int dataWidth;
	private int upsamplingFactor;

	private float[] fft_real;
	private float[] fft_imag;
	
	private float[] fft_pow;
	
	private float[] window;
	
	public FFTData(float samplingRate){
		this.samplingRate = samplingRate;
		setDataWidth(100, 1);
	}
	
	private static float[] generateGaussianWindow(int size){
		float[] result = new float[size];
		float window_mid = (size-1)/2;
		for(int i = 0; i < result.length; i++){
			float x = (i - window_mid)/(0.5f * result.length);
			result[i] = (float) Math.exp(-x*x/2*4*4);
		}
		return result;
	}
	
	public void setDataWidth(int dataWidth, int upsamplingFactor){
		this.dataWidth = dataWidth;
		this.upsamplingFactor = upsamplingFactor;
		
		if(dataWidth*upsamplingFactor % 2 != 0){
			throw new RuntimeException("ERROR! Data width needs to be even.");
		}
		
		this.fft_real = new float[dataWidth * upsamplingFactor];
		this.fft_imag = new float[dataWidth * upsamplingFactor];
		
		this.fft_pow = new float[dataWidth * upsamplingFactor / 2 + 1];
	
		// Create gaussian window
		this.window = generateGaussianWindow(dataWidth);
		
	}
	
	public void fromData(float[] data, int upsamplingFactor){
		if(data.length != dataWidth || this.upsamplingFactor != upsamplingFactor){
			setDataWidth(data.length, upsamplingFactor);
		}
		
		// Compute FFT
		for(int i = 0; i < fft_real.length; i++){
			if(i < data.length){
				fft_real[i] = window[i]*data[i]/(65536.0f);
			} else {
				fft_real[i] = 0;
			}
			fft_imag[i] = 0;
		}
		FFT.transform(fft_real, fft_imag);
		
		// Compute FFT power
		for(int i = 0; i < fft_pow.length; i++){
			fft_pow[i] = (float) Math.sqrt(fft_real[i]*fft_real[i]+fft_imag[i]*fft_imag[i]);
			//fft_pow[i] = (float) Math.log(fft_pow[i] + 1);
		}
		
		
		// Sanity test
		/*if(Math.abs(fft_real[fft_pow.length-2] / fft_real[fft_pow.length] - 1) > 0.01){
			throw new RuntimeException("Internal ERROR: FFT Sanity Test failed. ("
					+ fft_real[fft_pow.length-2] + " != " + fft_real[fft_pow.length] + ")");
		};*/
	}
	
	public float getFrequencyIntensity(float freq){
		float freq_max = samplingRate/2.0f;
		
		float bin = (fft_pow.length - 1) * freq/freq_max;
		
		return GeneralMath.interpolate(fft_pow, bin);
	}
		
}
