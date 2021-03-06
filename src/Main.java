import org.finomnis.instrumenttuner.computation.MidiPitchData;
import org.finomnis.instrumenttuner.computation.NoteAnalysisPipeline;
import org.finomnis.instrumenttuner.computation.PreProcessor;
import org.finomnis.instrumenttuner.computation.SelfSimData;
import org.finomnis.instrumenttuner.visualization.ColorTable;
import org.finomnis.instrumenttuner.visualization.ContinuousPlotter;
import org.finomnis.instrumenttuner.visualization.GraphPlotter;
import org.finomnis.instrumenttuner.visualization.GraphRenderer;
import org.finomnis.instrumenttuner.visualization.GraphWindow;

public class Main {

	public static void main(String[] args) {

		GraphWindow graphWindow0 = new GraphWindow("Original Signal", new GraphPlotter());
		GraphWindow graphWindow00 = new GraphWindow("Preprocessed Signal", new GraphPlotter());
		
		
		GraphWindow graphWindow = new GraphWindow("SelfSim", new GraphPlotter());
		GraphWindow graphWindow2 = new GraphWindow("Continuous", new ContinuousPlotter(ColorTable.HeatMap));
		GraphWindow graphWindow3 = new GraphWindow("Bb-Recognition", new GraphPlotter());
		//GraphWindow graphWindow4 = new GraphWindow("Note-Recognition", new ContinuousPlotter(ColorTable.HeatMap));
		GraphWindow graphWindow4 = new GraphWindow("Note-Recognition", new GraphPlotter());
		GraphWindow graphWindow5 = new GraphWindow("Note-Recognition (without Overtones)", new ContinuousPlotter(ColorTable.GrayScale));
		//GraphWindow graphWindow5 = new GraphWindow("Note-Recognition (without Overtones)", new GraphPlotter());
		//GraphWindow graphWindow6 = new GraphWindow("FFT", new ContinuousPlotter(ColorTable.GrayScale));
		GraphWindow graphWindow6 = new GraphWindow("FFT", new GraphPlotter());
		
		int bufferSize = 4096;
		
		AudioInput input = AudioInput.create(bufferSize);
		double t_framesync = System.currentTimeMillis();
		
		MidiPitchData midiPitchFFTData = new MidiPitchData(65.0f, 95.0f, 0.01f);
		
		NoteAnalysisPipeline analysisPipeline = new NoteAnalysisPipeline(37.0f, 95.0f, 0.001f, input.sampleRate);
		analysisPipeline.enableFFT(true);
		
		float[] data = null;
		
		PreProcessor preProcessor = new PreProcessor(input.sampleRate);
		
		while(graphWindow != null){
			
			data = input.getNewestFloatData(data, false);
			
			
			graphWindow0.setXRange(0, data.length);
			graphWindow0.setYRange(1, -1);
			graphWindow0.updateData(GraphRenderer.getIndices(data.length), data);
			
			data = preProcessor.preProcess(data, 5);
			
			graphWindow00.setXRange(0, data.length);
			graphWindow00.setYRange(1f, -1f);
			graphWindow00.updateData(GraphRenderer.getIndices(data.length), data);
			
			analysisPipeline.analyze(data);
			
			SelfSimData selfSimData = analysisPipeline.getSelfSimData();
			MidiPitchData midiPitchData = analysisPipeline.getMidiPitchData();

			float[] bb3_recognition = selfSimData.getFrequencyValues(233.08f, 5);
			
			
			midiPitchFFTData.fromFFT(analysisPipeline.getFFTData());
			
			
			graphWindow.setXRange(0, selfSimData.getData().length - 1);
			graphWindow.setYRange(2.0f, 0.0f);
			graphWindow.updateData(GraphRenderer.getIndices(selfSimData.getData().length), selfSimData.getData());
			
			graphWindow2.setXRange(0, selfSimData.getData().length - 1);
			graphWindow2.setYRange(1.0f, 0.0f);
			graphWindow2.updateData(GraphRenderer.getIndices(selfSimData.getData().length), selfSimData.getData());
			graphWindow3.setXRange(0, bb3_recognition.length - 1);
			graphWindow3.setYRange(1.0f, 0.0f);
			graphWindow3.updateData(GraphRenderer.getIndices(bb3_recognition.length), bb3_recognition);
			

			float[] max = new float[101];
			max[0] = analysisPipeline.getAutoNote().getMidiPitch();
//			max[0] = midiPitchData.getExactMaximum(45);
			for(int j = 1; j<max.length; j++){
				max[j] = j;
			}
			
			
			
			graphWindow4.setXRange(midiPitchData.midiMin, midiPitchData.midiMax);
			graphWindow4.setXRange(44.0f, 46.0f);
			graphWindow4.setYRange(0f, 1f);
			graphWindow4.updateData(midiPitchData.getMidis(), midiPitchData.getMidiCorelations());
			graphWindow4.setMarks(max);
			
			graphWindow5.setXRange(midiPitchData.midiMin, midiPitchData.midiMax);
			graphWindow5.setYRange(0f, 1f);
			graphWindow5.updateData(midiPitchData.getMidis(), midiPitchData.getMidiCorelationsWithSubtoneRemoval());
			graphWindow5.setMarks(max);
			
			graphWindow6.setXRange(midiPitchFFTData.midiMin, midiPitchFFTData.midiMax);
			graphWindow6.setYRange(0, 10f);
			graphWindow6.updateData(midiPitchFFTData.getMidis(), midiPitchFFTData.getMidiCorelations());
			
			
			System.out.println(analysisPipeline.getAutoNote());
			
			try {
				double targetfps = 100;
				t_framesync = t_framesync + 1000/targetfps;
				double leftover_frame_time = t_framesync - System.currentTimeMillis();
				//System.out.println(leftover_frame_time);
				leftover_frame_time = t_framesync - System.currentTimeMillis();
				if(leftover_frame_time > 0)
					Thread.sleep(Math.round(leftover_frame_time));
				else {
					//System.out.println("Can't keep up! (" + (-leftover_frame_time) +")");
					//t_framesync -= leftover_frame_time;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		while(graphWindow != null){}
		
		System.exit(0);
	}

}
