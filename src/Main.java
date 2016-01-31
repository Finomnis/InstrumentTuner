
import java.util.ArrayList;
import java.util.List;

import org.finomnis.instrumenttuner.computation.FFT;
import org.finomnis.instrumenttuner.computation.MidiPitchData;
import org.finomnis.instrumenttuner.computation.SelfSim;
import org.finomnis.instrumenttuner.computation.SelfSimData;
import org.finomnis.instrumenttuner.visualization.ColorTable;
import org.finomnis.instrumenttuner.visualization.ContinuousPlotter;
import org.finomnis.instrumenttuner.visualization.GraphPlotter;
import org.finomnis.instrumenttuner.visualization.GraphRenderer;
import org.finomnis.instrumenttuner.visualization.GraphWindow;

public class Main {

	public static void main(String[] args) {

		
		GraphWindow graphWindow = new GraphWindow("SelfSim", new GraphPlotter());
		GraphWindow graphWindow2 = new GraphWindow("Continuous", new ContinuousPlotter(ColorTable.HeatMap));
		GraphWindow graphWindow3 = new GraphWindow("Bb-Recognition", new GraphPlotter());
		GraphWindow graphWindow4 = new GraphWindow("Note-Recognition", new ContinuousPlotter(ColorTable.HeatMap));
		//GraphWindow graphWindow4 = new GraphWindow("Note-Recognition", new GraphPlotter());
		//GraphWindow graphWindow5 = new GraphWindow("Note-Recognition (without Overtones)", new ContinuousPlotter(ColorTable.HeatMap));
		GraphWindow graphWindow5 = new GraphWindow("Note-Recognition (without Overtones)", new GraphPlotter());
		float y[] = {10, 15, 13, 11, 12};
		float x[] = {0.7f, 1.0f, 1.3f, 1.4f, 1.8f};
		
		graphWindow.setYRange(9,  16);
		graphWindow.setXRange(0.5f, 2.0f);
		graphWindow.updateData(x, y);
		
		int bufferSize = 2000;
		
		AudioInput input = AudioInput.create(bufferSize);
		float[] selfSim_avg = null;
		double t_framesync = System.currentTimeMillis();
		
		SelfSimData selfSimData = new SelfSimData(input.sampleRate);
		MidiPitchData midiPitchData = new MidiPitchData(37.0f, 95.0f);
		
		while(graphWindow != null){
			
			int[] datai = input.getNewestData(false);
			
			float[] pos = new float[datai.length];
			float[] data = new float[datai.length];
			//float[] real = new float[datai.length];
			//float[] imag = new float[datai.length];
			
			for(int j = 0; j < pos.length; j++){
				pos[j] = j;
				//float point = (float) (Math.random() / 1.0 + Math.sin(j/100.0 * 2.0 * Math.PI));
				//data[j] = point;
				//real[j] = point;
				data[j] = datai[j];
				//real[j] = datai[j];
				//imag[j] = 0;
			}
			/*
			FFT.transform(real, imag);
			float[] fft_abs = new float[datai.length/2];
			float[] freq = new float[datai.length/2];
			for(int j = 0; j < fft_abs.length; j++){
				fft_abs[j] = (float) Math.sqrt(real[j]*real[j]+imag[j]+imag[j]) / bufferSize;
				freq[j] = j;
			}*/
			
			float[] selfSim = SelfSim.compute(data);
			float[] lag = new float[selfSim.length];
			for(int j = 0; j < lag.length; j++){
				lag[j] = j;
			}
			
			boolean selfSim_avg_needs_reset = false;
			if(selfSim_avg == null){
				selfSim_avg_needs_reset = true;
			} else if(selfSim_avg.length != selfSim.length){
				selfSim_avg_needs_reset = true;
			}
			
			if(selfSim_avg_needs_reset){
				selfSim_avg = new float[selfSim.length];
				for(int j = 0; j < selfSim_avg.length; j++){
					selfSim_avg[j] = 0;
				}
			}

			
			double sig_avg = 0.0;
			for(int j = 0; j < data.length; j++){
				sig_avg += Math.abs(data[j]);
			}
			sig_avg /= data.length;
			double val_h = sig_avg * 2.0;
			
			
			for(int j = 0; j < selfSim.length; j++){
				float new_value = selfSim[j];
				new_value /= (val_h*val_h*2.0f/3.0f);
				//new_value = 1/new_value;
				selfSim_avg[j] = 0.7f*selfSim_avg[j] + 0.3f*new_value;
			}

			float[] selfSim_cropped = new float[selfSim.length];
			for(int j = 0; j < selfSim_cropped.length; j++){
				float new_value = selfSim_avg[j];
				//new_value /= (val_h * val_h * 2.0f/3.0f);
				new_value = 1-new_value;
				if(new_value < 0) new_value = 0;
				selfSim_cropped[j] = new_value;
			}
			
			selfSimData.setData(selfSim_avg);
			float[] a4_recognition = selfSimData.getFrequencyValues(233.08f);
			
			midiPitchData.fromSelfSim(selfSimData);

			float[] midiTones = midiPitchData.getMidis();
			float[] midiSimilarity = midiPitchData.getMidiCorelations();
			
			/*graphWindow.setXRange(0, data.length-1);
			graphWindow.setYRange(-32768, 32768);
			graphWindow.updateData(pos, data);
			*/
			/*graphWindow.setXRange(0, fft_abs.length/8);
			graphWindow.setYRange(-10, 1000);
			graphWindow.updateData(freq, fft_abs);
			*/
			graphWindow.setXRange(0, selfSim.length - 1);
			graphWindow.setYRange(2.0f, 0.0f);
			graphWindow.updateData(lag, selfSim_avg);
			graphWindow2.setXRange(0, selfSim.length - 1);
			graphWindow2.setYRange(1.0f, 0.0f);
			graphWindow2.updateData(lag, selfSim_avg);
			graphWindow3.setXRange(0, a4_recognition.length - 1);
			graphWindow3.setYRange(1.0f, 0.0f);
			graphWindow3.updateData(GraphRenderer.getIndices(a4_recognition.length), a4_recognition);
			

			float[] max = new float[101];
			max[0] = midiPitchData.getExactMaximum(midiPitchData.getRoughMaximum());
//			max[0] = midiPitchData.getExactMaximum(45);
			for(int j = 1; j<max.length; j++){
				max[j] = j;
			}
			
			
			
			graphWindow4.setXRange(midiTones[0], midiTones[midiTones.length - 1]);
			graphWindow4.setYRange(0f, 1f);
			graphWindow4.updateData(midiTones, midiPitchData.getMidiCorelationsWithSubtoneRemoval());
			graphWindow4.setMarks(max);
			
			graphWindow5.setXRange(midiTones[0], midiTones[midiTones.length - 1]);
			//graphWindow5.setXRange(44.0f, 46.0f);
			graphWindow5.setYRange(0f, 1f);
			graphWindow5.updateData(midiTones, midiSimilarity);
			graphWindow5.setMarks(max);
			/*
			 * computed normalization factor:
			 * random values from -1 to 1
			 * X = [-1,1]
			 * Y = X-X
			 * Y = [-2, 2] with linear edges
			 * integrate(sin^2(x)) from 0 to 2*pi = pi;
			 * => average
			 * 2 = pi*x
			 * x=2/pi
			 */
			
			try {
				double targetfps = 100;
				t_framesync = t_framesync + 1000/targetfps;
				double leftover_frame_time = t_framesync - System.currentTimeMillis();
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
