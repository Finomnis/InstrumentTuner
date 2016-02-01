package org.finomnis.instrumenttuner.computation;

public class GeneralMath {

	public static float midiToFreq(float midi){
		return ((float) Math.pow(2.0f, (midi-69.0f)/12.0f)) * 440.0f;
	}
	
	private final static float log2 = (float) Math.log(2);
	public static float freqToMidi(float freq){
		return 69.0f + 12 * ((float)Math.log(freq/440.0f)) / log2;
	}
	
	public static float interpolate(float[] vec, float pos){
		int pos0 = (int) pos;
		int pos1 = pos0 + 1;
		float pos1_intensity = pos - pos0;
		float pos0_intensity = 1 - pos1_intensity;
		return vec[pos0] * pos0_intensity + vec[pos1] * pos1_intensity;
	}
	
}
