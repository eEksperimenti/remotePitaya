import java.net.Socket;


public class PitayaThread implements Runnable{
	
	private Inbox inbox;
	private String pitayaIp;

	public PitayaThread(String pitayaIp, Inbox inbox) {
		this.pitayaIp=pitayaIp;
		this.inbox = inbox;
	}
	@Override
	public void run() {
		
		
	}

}
