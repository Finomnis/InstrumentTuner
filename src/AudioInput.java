import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class AudioInput {

	public final int sampleRate;
	public final int sampleSize;
	public final int sampleMaxValue;
	
	private TargetDataLine line;
	
	private byte[] readBuffer;
	
	private int[] buffer;
	
	private long bufferPosition;
	
	private AudioInput(int sampleRate, int sampleSize, int bufferSize) throws LineUnavailableException{
		this.sampleRate = sampleRate;
		this.sampleSize = sampleSize;
		
		
		this.sampleMaxValue = 1 << (8*sampleSize - 1);
		
		System.out.println("SampleMaxValue: " + this.sampleMaxValue);
		
		//AudioFormat format = new AudioFormat(sampleRate, sampleSize, 1, true, false);
		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, sampleSize*8, 1, sampleSize, sampleRate,  true);
		
		this.line = AudioSystem.getTargetDataLine(format);
		
		if(bufferSize*sampleSize > line.getBufferSize()){
			throw new RuntimeException("Requested buffer size is too large. (Maximum for this device: " + line.getBufferSize() + ")");
		}
		
		line.open();
		line.start();
		
		long t0 = System.currentTimeMillis();
		byte[] b = new byte[sampleSize];
		while(!line.isRunning()){
			
			long t = System.currentTimeMillis() - t0;
			if(t > 1000){
				throw new RuntimeException("Unable to open audio input line!");
			}
			
			if(line.available() > 0){
				line.read(b, 0, sampleSize);
			}
		}
		
		buffer = new int[bufferSize];
		readBuffer = new byte[line.getBufferSize()];
		
		for(int i = 0; i < buffer.length; i++){
			buffer[i] = 0;
		}
	}
	
	public int[] getNewestData(){
		
		return getNewestData(false);
		
	}

	public float[] getNewestFloatData(float [] suggestionBuffer, boolean zeroDelay){
		
		// retrieve new data
		int[] newData = getNewestData(zeroDelay);
		
		// re-initialize buffer if necessary
		if(suggestionBuffer == null){
			suggestionBuffer = new float[newData.length];
		} else if (suggestionBuffer.length != newData.length){
			suggestionBuffer = new float[newData.length];
		}
		
		// convert
		float sampleMaxValueF = sampleMaxValue;
		for(int i = 0; i < newData.length; i++){
			suggestionBuffer[i] = newData[i] / sampleMaxValueF;
		}
		
		return suggestionBuffer;
		
	}
	
	public int[] getNewestData(boolean zeroDelay){
		
		// check for available data
		long newPosition = line.getLongFramePosition();
		int positionChange = (int)(newPosition - bufferPosition) * sampleSize;
		int available = line.available();
		
		//System.out.println("lag: " + (available - positionChange));
		
		int readNum = positionChange;
		if(available < readNum || zeroDelay){
			//System.out.println("LAG! " + (available - readNum));
			readNum = available;
		}
		bufferPosition = newPosition;
		
		
		// read data to readBuffer
		if(readNum > readBuffer.length) throw new RuntimeException("readNum too large");
		int num_read = line.read(readBuffer, 0, readNum);
		if(readNum != num_read) throw new RuntimeException("Internal Error!");
		
		// move old data in buffer
		int moveAmount = readNum/sampleSize;
		if(moveAmount > buffer.length) moveAmount = buffer.length;
		for(int i = buffer.length-1; i >= moveAmount; i--){
			buffer[i] = buffer[i - moveAmount];
		}
		
		if(sampleSize == 2){
			for(int i = 0; i < moveAmount; i++){
				int data = ((readBuffer[2*i+1]&0xff)<<8) + ( readBuffer[2*i+1]&0xff);
				data = ((readBuffer[2*i]) << 8) + (readBuffer[2*i+1]&0xff);
				buffer[moveAmount - 1 - i] = data;
			}
		} else if (sampleSize == 1){
			for(int i = 0; i < moveAmount; i++){
				int data = readBuffer[i];
				buffer[moveAmount - 1 - i] = data;
			}
		} else {
			
			throw new RuntimeException("Not implemented yet.");
			
		}
		
		return buffer;
	}
	
	
	public static AudioInput create(int bufferSize){
		
		AudioInput result;
		try {
			result = new AudioInput(44100, 2, bufferSize);
			return result;
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}
	
}
