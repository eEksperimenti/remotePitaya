import java.lang.ProcessBuilder.Redirect;


public class PitayaBuffer {
	private String[] buffer = new String[2];
	private int writeTo  = 0;
	private int readFrom = 1;
	
	public synchronized void writeData(String data){
		buffer[writeTo] = data;
		int tmp = readFrom;
		readFrom = writeTo;
		writeTo= tmp;
	}
	public synchronized String readData(){
		return buffer[readFrom];
	}
	
}
