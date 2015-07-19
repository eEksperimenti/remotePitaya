import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class PitayaThread implements Runnable {

	private Inbox inbox;
	private String pitayaIp;

	public PitayaThread(String pitayaIp, Inbox inbox) {
		this.pitayaIp = pitayaIp;
		this.inbox = inbox;
	}

	@Override
	public void run() {
		Socket pitaya;
		try {
			pitaya = new Socket(pitayaIp, Main.port);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					pitaya.getOutputStream(), "UTF-8"));
			BufferedReader in = new BufferedReader(new InputStreamReader(
					pitaya.getInputStream(), "UTF-8"));
			String request, response = "";
			while (true) {

				request = inbox.getRequest();
				out.write(request);
				out.flush();
				response = in.readLine();
				inbox.setResponse(response);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
