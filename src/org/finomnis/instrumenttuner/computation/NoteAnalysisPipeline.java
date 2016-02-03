package org.finomnis.instrumenttuner.computation;

public class NoteAnalysisPipeline {
	
	public final float selfSimSmoothing;
	
	public final float samplingRate;
	private int dataWidth;
	
	private final FFTData fftData;
	private final SelfSimData selfSimData;
	private final SelfSimData selfSimDataSmoothed;
	private final MidiPitchData midiPitchData;
	
	private float roughFrequencyEstimate = -1.0f;
	private float exactFrequency = -1.0f;
		
	private boolean useFFT = false;
	

	public NoteAnalysisPipeline(float midi_min, float midi_max, float midi_resolution, float samplingRate){
		this(midi_min, midi_max, midi_resolution, samplingRate, 0.0f);
	}
	
	public NoteAnalysisPipeline(float midi_min, float midi_max, float midi_resolution, float samplingRate, float smoothing){
		this.samplingRate = samplingRate;
		this.selfSimSmoothing = smoothing;

		this.selfSimData = new SelfSimData(samplingRate);
		this.selfSimDataSmoothed = new SelfSimData(samplingRate);
		this.midiPitchData = new MidiPitchData(midi_min, midi_max, midi_resolution);
		this.fftData = new FFTData(samplingRate);
		
		this.setDataWidth(100);
	}
	
	public void enableFFT(boolean enable){
		this.useFFT = enable;
	}
	
	public void setDataWidth(int dataWidth){
		this.dataWidth = dataWidth;
		
		this.fftData.setDataWidth(dataWidth, 1);
		
		this.selfSimData.setData(new float[dataWidth - 1]);
		this.selfSimDataSmoothed.setData(new float[dataWidth - 1]);
		this.selfSimDataSmoothed.clearData();
	}
	
	public void analyze(float[] data){
		
		if(data.length != dataWidth){
			System.out.println("Data width changed. Setting new data width: " + data.length);
			setDataWidth(data.length);
		}
		
		// Compute number of overtones
		float periodMax = GeneralMath.midiToFreq(midiPitchData.midiMin);
		int numOvertones = (int) ((data.length-1) * periodMax / samplingRate);
		
		// Compute FFT
		if(useFFT)fftData.fromData(data, 1);
		
		// Compute Self-Similarity
		SelfSim.compute(data, selfSimData);
		
		// Smooth Self-Similarity
		selfSimDataSmoothed.addSmooth(selfSimData, selfSimSmoothing);
		
		// Compute Midi Pitch Probabilities
		midiPitchData.fromSelfSim(selfSimDataSmoothed, numOvertones);
	
		// Compute rough Frequency Estimate
		roughFrequencyEstimate = midiPitchData.getRoughMaximum();
		
		// Compute exact Frequency
		exactFrequency = midiPitchData.getExactMaximum(roughFrequencyEstimate);

	}
	
	public SelfSimData getSelfSimData(){
		return this.selfSimDataSmoothed;
	}

	public MidiPitchData getMidiPitchData() {
		return this.midiPitchData;
	}

	public FFTData getFFTData(){
		return this.fftData;
	}
	
	public MidiPitch getAutoNote() {
		return new MidiPitch(this.exactFrequency);
	}
	
	public MidiPitch getGuidedNote(float guidanceMidi){
		return new MidiPitch(this.midiPitchData.getExactMaximum(guidanceMidi));
	}

	
}
