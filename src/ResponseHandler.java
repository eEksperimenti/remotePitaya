import java.awt.geom.RoundRectangle2D;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BrokenBarrierException;

import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.spi.http.HttpHandler;

public class ResponseHandler implements Runnable {
	private Inbox inbox;
	private String Ip;
	private String experiment;
	private Socket client,pitaya;
	private InputStream pitayaIn;
	private OutputStream  clientOut;
	private BufferedWriter pitayaOut;
	private String footer="";

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
			
			System.out.println("OK: ip: " + this.Ip + " port: " + 80);
			openIOStrems();
			String request, response = "";
			String host = "Host: "+this.Ip;

			
			/*BufferedWriter welcomeOutput = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			// Send a welcome message to the client//
			response = "<b>Hello user!</b></br> You are now connected to our server for remote experimenting.</br><a href='212.235.190.181:5950/zarnica'>Clikc to start</a>;
			welcomeOutput.write(header + "\n");
			welcomeOutput.write("Content-Length: " + response.length() + "\r\n");
			welcomeOutput.write("Connection: keep-alive\r\n");
			welcomeOutput.write("\r\n");
			welcomeOutput.write(response + "\r\n");
			welcomeOutput.write("\r\n");
			welcomeOutput.flush();*/

			/*request = this.inbox.getRequest();
			System.out.println("REQUEST (responHand): "+request);
			String footer = request.substring(request.indexOf("Host: 212.235.190.181:5950")+"Host: 212.235.190.181:5950".length());*/
			
			request = inbox.getRequest();
			 footer  = request.substring(request.indexOf("User-Agent:") - "User-Agent:".length());

			System.out.println("First request sent");
			System.out.println("GET /zarnica/ HTTP/1.1\r\n" +
					"Host: "+this.Ip+"\r\n" +footer+"\r\n");
			this.pitayaOut.write("GET /zarnica/ HTTP/1.1\r\n" +
								"Host: "+this.Ip+"\r\n" +footer);
			this.pitayaOut.flush();
			//this.pitaya.close();
			

			while (true) {
				//openIOStrems();
				if (inbox.isNewRequest()){
					request = inbox.getRequest();
					String topheader = request.substring(0, request.indexOf("Host:"));
					String newRequest = topheader+"\r\nHost: "+this.Ip+"\r\n"+footer+"\rn\n";
					System.out.println(newRequest);
					pitayaOut.write(newRequest);
					pitayaOut.flush();
					
				}
				/*while (!(response = this.pitayaIn.readLine()).equals("")){
					clientOut.write(response);
					clientOut.flush();
					if (response.equals("Content-Length:"))
					this.clientOut.write(response);
					clientOut.flush();
					System.out.println(response);
				}*/
				System.out.println("--------- send to client -------");
				
				byte buf[] = new byte[1024];
				int bytesRead=0;
				while (( bytesRead = pitayaIn.read(buf)) != -1) {
			         this.clientOut.write(buf, 0, bytesRead);
			    }
				
			
				

			}
		} catch (SocketException ex) {
			ex.printStackTrace();

		} catch (IOException e) {
			/*try {
				this.clientOut.flush();
			
			this.pitayaIn.close();
			this.pitayaIn.close();
			this.pitaya.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}
	public void openIOStrems() throws UnknownHostException, IOException{
		this.pitaya = new Socket(Ip, 80);

		if (this.pitaya.isConnected())
			System.out.println("Connected");

		 this.pitayaOut = new BufferedWriter (new OutputStreamWriter(this.pitaya.getOutputStream(), "UTF-8"));
		 this.pitayaIn = this.pitaya.getInputStream();
		 this.clientOut = this.client.getOutputStream();
	}
}
