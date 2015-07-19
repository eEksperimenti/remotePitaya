import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.xml.crypto.Data;

public class ClientThread implements Runnable {
	private Socket client;
	private Inbox inbox;

	public ClientThread(Socket client, Inbox inbox) {
		this.client = client;
		this.inbox = inbox;
	}

	@Override
	public void run() {

		try {
			String header = "HTTP/1.1 200 OK\nContent-Type: text/html";
			String response = "<b>Hello user!</b></br> You are now connected to our server for remote experimenting.</br>";
			String request = "";

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"));
			//ObjectOutputStream ois = new ObjectOutputStream(client.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));

			out.write(header + "\n");
			out.write("Content-Length: " + response.length() + "\r\n");
			out.write("Connection: keep-alive\r\n");
			out.write("\r\n");
			out.write(response + "\r\n");
			out.write("\r\n");
			out.flush();

			while (true) {
				request = in.readLine();
				long start = System.nanoTime();
				inbox.setRequest(request);
				response = inbox.getResponse();
				 
				 out.write(response);	
				 out.flush();
				
				long stop = System.nanoTime();
				long duration =(stop-start)/1000000;
				System.out.println("start: "+start+" end: "+stop+" -> Time: "+duration);
			}

		} catch (IOException e) {
			// out.close();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
