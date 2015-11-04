import java.lang.ProcessBuilder.Redirect;


public class PitayaBuffer {
	private String[] buffer = new String[2];
	private int writeTo  = 0;
	private int readFrom = 1;
	
	public synchronized void writeData(String data){
	//	if (writeTo != -1)
			buffer[writeTo] = data;
		int tmp = readFrom;
		readFrom = writeTo;
		writeTo= tmp;
	}
	public synchronized String readData(){
		//if (readFrom != -1)
			return buffer[readFrom];
		//return null;
	}
	public void clearBuffer(){
		buffer[writeTo] = null;
		buffer[readFrom] = null;
		writeTo=-1;
		readFrom=-1;
	}
}
