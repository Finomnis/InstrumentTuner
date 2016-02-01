package org.finomnis.instrumenttuner.computation;

public class MidiPitch {

	private final float midiPitch;
	
	public MidiPitch(float midiPitch){
		this.midiPitch = midiPitch;
	}
	
	@Override
	public String toString(){
		if(this.midiPitch < 0){
			return "---";
		}
		
		String result = "";
		
		int midiNote = Math.round(this.midiPitch);
		String[] midiNames = {"C ", "C#", "D ", "D#", "E ", "F ", "F#", "G ", "G#", "A ", "Bb", "B "};
		result += midiNames[midiNote%12];
		
		int octave = midiNote/12 - 1;
		if(octave >= 0){
			result += "  " + octave;
		} else {
			result += " -" + (-octave);
		}
		
		float fineTuning = this.midiPitch - midiNote;
		int cents = Math.round(fineTuning * 100);
		
		if(cents >= 0){
			result += " +" + cents;
		} else {
			result += " -" + (-cents);
		}
	
		return result;

	}

	public float getMidiPitch() {
		return midiPitch;
	}
	
	public float getFrequency() {
		return GeneralMath.midiToFreq(midiPitch);
	}
	
}
