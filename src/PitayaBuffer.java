
public class PitayaBuffer {
	private String[] buffer = new String[2];
	private int writingTo   = 0;
	private int readingFrom = 1;
	
	public synchronized void writeData(String data){
		buffer[writingTo] = data;
		readingFrom = writingTo;
		writingTo= readingFrom;
	}
	public synchronized String readData(){
		return buffer[readingFrom];
	}
	
}
