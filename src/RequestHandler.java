import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class RequestHandler implements Runnable {
	private Socket client;
	private Inbox inbox;

	public RequestHandler(Socket client, Inbox inbox) {
		this.client = client;
		this.inbox = inbox;
		
	}

	public void run() {
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
			String request = "";
			while (true) {
				request = in.readLine();
				inbox.setRequest(request);
			}
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

}
