package org.finomnis.instrumenttuner.computation;

public class MidiPitchData {

	public final float midiMin;
	public final float midiMax;
	public final float stepWidth;
	
	private final float[] midis;
	private final float[] midiCorelations;
	private final float[] midiCorelationsWithSubtoneRemoval;
	
	private int max_rough;
	
	public MidiPitchData(float midiMin, float midiMax, float stepWidth){
		this.midiMin = midiMin;
		this.midiMax = midiMax;
		this.stepWidth = stepWidth;
		
		int numBuckets = 0;
		for(float midi = midiMin; midi <= midiMax; midi+=stepWidth){
			numBuckets++;
		}
		midis = new float[numBuckets];
		midiCorelations = new float[numBuckets];
		midiCorelationsWithSubtoneRemoval = new float[numBuckets];
		
		int i = 0;
		for(float midi = midiMin; midi <= midiMax; midi+=stepWidth){
			midis[i] = midi;
			midiCorelations[i] = 0.0f;
			i++;
		}
	}
	
	public void fromSelfSim(SelfSimData data, int numOvertones){
		for(int i = 0; i < midis.length; i++){
			float currentMidi = midis[i];
			float freq = GeneralMath.midiToFreq(currentMidi);
			midiCorelations[i] = data.getFrequencyCorelation(freq, numOvertones);
			//if(midiCorelations[i] > 0.01f) System.out.println(i + " - " + midiCorelations[i]);
		}
		doMaximumExtraction();
	}
	
	public void fromFFT(FFTData data){
		for(int i = 0; i < midis.length; i++){
			float currentMidi = midis[i];
			float freq = GeneralMath.midiToFreq(currentMidi);
			midiCorelations[i] = data.getFrequencyIntensity(freq);
		}
		doMaximumExtraction();
	}
	
	public float[] getMidis(){
		return midis;
	}
	
	public float[] getMidiCorelations(){
		return midiCorelations;
	}
	
	private void doMaximumExtraction(){
		max_rough = roughMaximum(midiCorelations, midis);
		doSubtoneRemoval();
	}
	
	public final static float overtoneDetectionStrength = 0.005f;
	
	private static int roughMaximum(float[] vals, float[] midis){
		
		// Compute absolute Maximum
		float absoluteMaximum = 0.0f;
		int absoluteMaximumPosition = -1;
		for(int i = 0; i < vals.length; i++){
			//if(vals[i] == 0.0f) continue;
			if(absoluteMaximum < vals[i]+overtoneDetectionStrength*midis[i]){
				absoluteMaximum = vals[i]+overtoneDetectionStrength*midis[i];
				absoluteMaximumPosition = i;
			}
		}

		if(absoluteMaximumPosition == vals.length-1){
			return -1;
		}
		
		return absoluteMaximumPosition;
	}

	private void doSubtoneRemoval(){
		for(int i = 0; i < midiCorelationsWithSubtoneRemoval.length; i++){
			midiCorelationsWithSubtoneRemoval[i] = 0.0f;
		}
		if(max_rough < 0) return;
		for(int i=max_rough; i>=0; i--){
			midiCorelationsWithSubtoneRemoval[i] = midiCorelations[i];
			if(midiCorelations[i] == 0.0) break;
		}
		for(int i=max_rough; i<midiCorelations.length; i++){
			midiCorelationsWithSubtoneRemoval[i] = midiCorelations[i];
			if(midiCorelations[i] == 0.0) break;
		}
	}

	public boolean hasMaximum() {
		return max_rough >= 0;
	}
	
	public float getExactMaximum(float roughMaximum){
		int peakstart;
		for(peakstart = 1; peakstart < midis.length; peakstart++){
			if(midis[peakstart]>=roughMaximum) break;
		}
		if(Math.abs(midis[peakstart - 1]-roughMaximum) < Math.abs(midis[peakstart] - roughMaximum)){
			peakstart --;
		}
		
		
		if(peakstart >= midis.length) peakstart = midis.length - 1;
		
		if(midiCorelations[peakstart] <= 0.0f){
			return -1f;
		}
		
		int peak_left;
		for(peak_left=peakstart; peak_left>=0; peak_left--){
			if(midiCorelations[peak_left] == 0.0) break;
		}
		int peak_right;
		for(peak_right=peakstart; peak_right<midiCorelations.length; peak_right++){
			if(midiCorelations[peak_right] == 0.0) break;
		}
		
		if(peak_left < 0) peak_left = 0;
		if(peak_right > midiCorelations.length-1) peak_right = midiCorelations.length-1;
		
		double sum = 0.0;
		for(int i = peak_left; i<=peak_right; i++){
			double freq = GeneralMath.midiToFreq(midis[i]);
			double period = 1.0/freq;
			sum += midiCorelations[i]*period;
		}
		
		double midp = 0.0;
		for(int i = peak_left; i<=peak_right; i++){
			double freq = GeneralMath.midiToFreq(midis[i]);
			double period = 1.0/freq;
			midp += i*(midiCorelations[i]*period/sum);
		}
		
		float maximum = (float)midp;
		
		float max_midi = GeneralMath.interpolate(midis, maximum);
		
		//System.out.println(max_midi);
		return max_midi;
	}
	
	public float[] getMidiCorelationsWithSubtoneRemoval(){
		return midiCorelationsWithSubtoneRemoval;
	}

	public float getRoughMaximum() {
		if(max_rough < 0) return -1;
		return midis[max_rough];
	}
	
}
