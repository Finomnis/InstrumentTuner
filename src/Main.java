
import org.finomnis.instrumenttuner.computation.FFT;
import org.finomnis.instrumenttuner.computation.SelfSim;
import org.finomnis.instrumenttuner.visualization.ColorTable;
import org.finomnis.instrumenttuner.visualization.ContinuousPlotter;
import org.finomnis.instrumenttuner.visualization.GraphPlotter;
import org.finomnis.instrumenttuner.visualization.GraphWindow;

public class Main {

	public static void main(String[] args) {

		
		GraphWindow graphWindow = new GraphWindow("SelfSim", new GraphPlotter());
		GraphWindow graphWindow2 = new GraphWindow("Continuous", new ContinuousPlotter(ColorTable.HeatMap));
		
		float y[] = {10, 15, 13, 11, 12};
		float x[] = {0.7f, 1.0f, 1.3f, 1.4f, 1.8f};
		
		graphWindow.setYRange(9,  16);
		graphWindow.setXRange(0.5f, 2.0f);
		graphWindow.updateData(x, y);
		
		int bufferSize = 2000;
		
		AudioInput input = AudioInput.create(bufferSize);
		float[] selfSim_avg = null;
		
		while(graphWindow != null){
			
			int[] datai = input.getNewestData(true);
			
			float[] pos = new float[datai.length];
			float[] data = new float[datai.length];
			float[] real = new float[datai.length];
			float[] imag = new float[datai.length];
			
			for(int j = 0; j < pos.length; j++){
				pos[j] = j;
				//float point = (float) (Math.random() / 1.0 + Math.sin(j/100.0 * 2.0 * Math.PI));
				//data[j] = point;
				//real[j] = point;
				data[j] = datai[j];
				real[j] = datai[j];
				imag[j] = 0;
			}
			
			FFT.transform(real, imag);
			float[] fft_abs = new float[datai.length/2];
			float[] freq = new float[datai.length/2];
			for(int j = 0; j < fft_abs.length; j++){
				fft_abs[j] = (float) Math.sqrt(real[j]*real[j]+imag[j]+imag[j]) / bufferSize;
				freq[j] = j;
			}
			
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
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		while(graphWindow != null){}
		
		System.exit(0);
	}

}
