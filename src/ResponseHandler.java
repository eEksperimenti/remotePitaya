import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

import javax.xml.ws.http.HTTPBinding;

public class ResponseHandler implements Runnable {
	private Inbox inbox;
	private String Ip;
	private String experiment;
	private Socket client;

	public ResponseHandler(Socket client, String pitayaIp, Inbox inbox) {
		this.Ip = pitayaIp.substring(0, pitayaIp.indexOf("/"));
		this.experiment = pitayaIp.substring(pitayaIp.indexOf("/") + 1);
		System.out.println(Ip + "  " + experiment);
		this.inbox = inbox;
		this.client = client;
	}

	@Override
	public void run() {
		Socket pitaya;
		try {
			System.out.println("OK: ip: " + this.Ip + " port: " + Main.port);

			pitaya = new Socket(Ip, Main.port);

			if (pitaya.isConnected())
				System.out.println("Connected");

			BufferedWriter pitayaOut = new BufferedWriter(
					new OutputStreamWriter(pitaya.getOutputStream(), "UTF-8"));
			BufferedWriter clientOut = new BufferedWriter(
					new OutputStreamWriter(client.getOutputStream(), "UTF-8"));
			BufferedReader pitayaIn = new BufferedReader(new InputStreamReader(
					pitaya.getInputStream(), "UTF-8"));
			String request, response = "";

			System.out.println("OK");
			String header = "HTTP/1.1 200 OK\nContent-Type: text/html";
			response = "<b>Hello user!</b></br> You are now connected to our server for remote experimenting.</br>";

			clientOut.write(header + "\n");
			clientOut.write("Content-Length: " + response.length() + "\r\n");
			clientOut.write("Connection: keep-alive\r\n");
			clientOut.write("\r\n");
			clientOut.write(response + "\r\n");
			clientOut.write("\r\n");
			clientOut.flush();

			request = inbox.getRequest();
			String footer = request
					.substring(request.indexOf("User-Agent:") - 11);
			System.out.println("footer: " + footer);

			pitayaOut
					.write("GET /zarnica/ HTTP/1.1\r\nHost: 192.168.94.132\r\n"
							+ footer);
			System.out
					.println("Request pitaya: "
							+ "GET /scope+gen_translation/ HTTP/1.1\r\nHost: 192.168.94.134\r\n"
							+ footer);
			pitayaOut.flush();
			
			

			while (true) {

				request = inbox.getRequest();

				pitayaOut.write(request);
				pitayaOut.flush();
				response = pitayaIn.readLine();
				clientOut.write(response);
				clientOut.flush();

			}
		} catch (SocketException ex) {
			ex.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}
}
