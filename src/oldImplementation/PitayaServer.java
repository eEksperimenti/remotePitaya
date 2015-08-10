package oldImplementation;
import java.awt.image.LookupTable;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.EOFException;

public class PitayaServer implements Runnable {
	private ServerSocket server;
	private int port;
	private boolean running;

	private String token = "";
	private int pitayaNum = -1;
	private int user = -1;
	private String data="";

	public PitayaServer(int port) {
		this.port = port;
		this.running = false;
	}

	@Override
	public void run() {

		openServerSocket();

		while (this.running) {
			try {
				Socket client = this.server.accept();
				if (client.isConnected()) {
					System.out.println("Client connected!\n IP:"
							+ client.getInetAddress() + "\n Port: "
							+ client.getPort());

				
					if (isTokenValid() && getParameters(client)) {

						Inbox inbox = Inbox.getInstance();
						System.out.println("DATA: "+data);
						inbox.setRequest(data);
						
						RequestHandler request = new RequestHandler(client, inbox);
						new Thread(request).start();
						ResponseHandler response = new ResponseHandler(client,Main.lookupTable[pitayaNum-1],inbox);
						new Thread(response).start();

					}

				}

			} catch (IOException e) {
				if (!running && server.isClosed())
					try {
						server.close();
					} catch (Exception e2) {
						System.out.println(e2.toString());
					}

			}
		}
		System.out.println("Service stoped");

	}

	public void openServerSocket() {
		try {
			this.server = new ServerSocket(port);

		} catch (IOException e) {
			System.out.println("Can't open service socket\n" + e);
			this.running = false;
			return;
		}
		this.running = true;
		System.out.println("Service  started.\nListening on port: "
				+ server.getLocalPort());
	}

	public void stopService() {
		if (running) {
			try {
				server.close();
				running = false;
			} catch (IOException e) {
				System.out.println("Can't close the service\n" + e);
			}
		}
	}

	public boolean getParameters(Socket client) {
		try {
			InputStream input = client.getInputStream();
			byte[] buffer = new byte[4096];
			input.read(buffer);
			data = new String(buffer, "UTF-8");

			System.out.println(data);
			int indexStart = data.indexOf("/?") + 2;
			int indexEnd = data.indexOf("HTTP") - 1;
			System.out.println("start "+indexStart+ " end: "+indexEnd);
			if (indexStart > 10){
			String params = data.substring(indexStart, indexEnd);

			token = params.substring(2, params.indexOf("&"));
			params = params.substring(params.indexOf("&") + 1);

			user = Integer.parseInt(params.substring(2, params.indexOf("&")));
			params = params.substring(params.indexOf("&") + 1);

			pitayaNum = Integer.parseInt(params.substring(2));
			}
			
			if (pitayaNum > Main.lookupTable.length){
				return false;
			}

		} catch (IOException e) {
			System.out.println("Can't open input stream from connection");
		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean isTokenValid() {

		// To do: Look in booked database for token
		return true;
	}

}
