import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class ResponseHandler implements Runnable{
	private Inbox inbox;
	private String pitayaIp;
	private Socket client;

	public ResponseHandler(Socket client,String pitayaIp, Inbox inbox) {
		this.pitayaIp = pitayaIp;
		this.inbox = inbox;
		this.client=client;
	}

	@Override
	public void run() {
		Socket pitaya;
		try {
			pitaya = new Socket(pitayaIp, Main.port);
			BufferedWriter pitayaOut = new BufferedWriter(new OutputStreamWriter(
					pitaya.getOutputStream(), "UTF-8"));
			BufferedWriter clientOut = new BufferedWriter(new OutputStreamWriter(
					client.getOutputStream(), "UTF-8"));
			BufferedReader in = new BufferedReader(new InputStreamReader(
					pitaya.getInputStream(), "UTF-8"));
			String request, response = "";
			
			
			String header = "HTTP/1.1 200 OK\nContent-Type: text/html";
			response = "<b>Hello user!</b></br> You are now connected to our server for remote experimenting.</br>";
			
			clientOut.write(header + "\n");
			clientOut.write("Content-Length: " + response.length() + "\r\n");
			clientOut.write("Connection: keep-alive\r\n");
			clientOut.write("\r\n");
			clientOut.write(response + "\r\n");
			clientOut.write("\r\n");
			clientOut.flush();
			
			
			while (true) {

				request = inbox.getRequest();
				pitayaOut.write(request);
				pitayaOut.flush();
				response = in.readLine();
				clientOut.write(response);
				clientOut.flush();


			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
