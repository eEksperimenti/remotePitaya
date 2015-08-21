import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

public class PitayaServer implements Runnable {
	private ServerSocket server;
	private Socket client;
	private int port;
	private boolean running;
	private int pitayaNum = -1;
	private String token, experiment, fileName, method, contentType, data = "";
	private final int BUFFER_SIZE = 1024;
	private boolean[] runningPitaya = new boolean[Main.lookupTable.length];

	public PitayaServer(int port) {
		this.port = port;
		this.running = false;
		for (int i = 0; i < runningPitaya.length; i++) {
			runningPitaya[i]= false;
		}
	}

	@Override
	public void run() {
		// init the server socket
		// init the buffer to be used later for reading from input
		openServerSocket(); 
		byte[] buffer = new byte[BUFFER_SIZE]; 
		while (this.running) {
			try {
				// accept incoming connections
				client = this.server.accept(); 
				if (client.isConnected()) {
					System.out.println("Client connected!\n IP:"
							+ client.getInetAddress() + "\n Port: "
							+ client.getPort());
					//open input/output streams
					InputStream input = client.getInputStream(); 
					OutputStream output = client.getOutputStream();
					String header = "";
					
					// read the http request
					input.read(buffer); 
					data = new String(buffer, "UTF-8");
					
					// If we have a http GET request 
					if (data.startsWith("GET") && getParameters(data)) { 
						
						//Check if we are already fetching from the desired pitaya
						if (!runningPitaya[pitayaNum-1]){
							Thread t = new Thread(new PitayaDataFetcher(Main.lookupTable[pitayaNum-1],this.experiment));
							t.start();
							runningPitaya[pitayaNum-1] = true;
						}

						//If requesting data, read from pitayaBuffer and send to client
						if (fileName.startsWith("/data")){
							String responseData = PitayaDataFetcher.pitayaBuffer.readData();

							header = "HTTP/1.1 200 OK\r\n"
									+ "Content-type: application/json\r\n"
									+ "Content-size: " + responseData.length() + "\r\n"
									+ "Connection: Close\r\n\r\n"
									+"Access-Control-Allow-Origin: *"
									+"Access-Control-Allow-Credentials: true"
									+"Access-Control-Allow-Methods: GET, POST, OPTIONS"
									+"Access-Control-Allow-Headers: DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type";
							System.out.println("SENDING DATA.....");
							output.write(header.getBytes());
							output.write(responseData.getBytes());
							output.flush();
						}else{
						// Check if we have the specified file
						File f = new File("apps/" + fileName);
						if (f.exists()) { 
							long fileSize = f.length();
							
							// reading from file (http body) and sending(with http header)
							FileInputStream fis = new FileInputStream(f); 
							
							// Http response status line and header
							header = "HTTP/1.1 200 OK\r\n"
									+ "Content-type: "+contentType+"\r\n"
									+ "Content-size: " + fileSize + "\r\n"
									+ "Connection: Close\r\n\r\n"; 
							output.write(header.getBytes());
							System.out.println("HEADER: "+header);


							int ch = fis.read(buffer, 0, BUFFER_SIZE);
							while (ch != -1) {
								output.write(buffer, 0, ch);
								ch = fis.read(buffer, 0, BUFFER_SIZE);
							}
							System.out.println(output);
							output.flush();
							
						} else {
							header = "HTTP/1.1 404 Not found\r\n";
						}
					}
				}else {
					input.close();
					client.close();
				}
				output.close();
				client.close();
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

	public boolean getParameters(String data) {
		try {
			// Split entire package into lines
			String[] params =null;
			String[] lines = data.split("\r\n"); 
			
			// Split the status line in three parts (0-method, 1-URL, 2-protocol version)
			String[] status = data.split(" "); 
			method = status[0]; // method GET or POST
			System.out.println("STATUS: "+status[1]);
			// Check if we have any GET parameters
			if (status[1].indexOf("&") > 0) { 
				// if we have any parameters (resources[1]), take them apart
				String[] resource = status[1].split("\\?"); 
				fileName = resource[0]; 
				if (resource.length > 1) {
					params = resource[1].split("&"); // Divide each parameter
				}
			} else { // If no parameter present, take the entire string
				fileName = status[1];
			}
			if (params != null) { //
				System.out.println("### LEN: "+params.length);
				this.pitayaNum = Integer.parseInt(params[0].split("=")[1]);
				this.experiment = (params.length > 1) ? params[1].split("=")[1] : "";
				this.token = (params.length > 2) ? params[2].split("=")[1] : "";
			}
			System.out.println("P: "+this.pitayaNum+" ex: "+this.experiment+" t: "+this.token);

			contentType = (lines[3].split(":")[1]).trim(); // Take the content type from
													// the http request
			// hack: for html pages use simpler
			// content type string
			if (contentType.startsWith("text/html")){
				contentType = "text/html"; 
				
			}else if (contentType.startsWith("*/*"))
				contentType = "application/x-javascript";
		

			if (pitayaNum > Main.lookupTable.length) { // Check if pitayaNum is
														// a valid number
				return false;
			}

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
