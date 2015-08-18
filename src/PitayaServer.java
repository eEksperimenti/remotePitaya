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

	static Hashtable<String, ArrayList<Socket>> subscribers = new Hashtable<String,ArrayList<Socket>>();

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
				client = this.server.accept(); // accept incoming
														// connections
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
					
					// If we have a http GET request (user loading web page)
					if (data.startsWith("GET") && getParameters(data)) { 
						
						//Check if we are already fetching from the desired pitaya
						if (!runningPitaya[pitayaNum-1]){
							Thread t = new Thread(new PitayaDataFetcher(Main.lookupTable[pitayaNum-1],this.experiment));
							t.start();
							runningPitaya[pitayaNum-1] = true;
						}
						System.out.println("FILE: "+fileName);

						if (fileName.equals("data")){
							System.out.println("!DATA : "+fileName);
							PitayaDataFetcher.pitayaBuffer.readData();
							output.write((PitayaDataFetcher.pitayaBuffer.readData()).getBytes());
							output.flush();
						}else{
						// Check if we have the specified file
						File f = new File("apps/" + fileName);
						if (f.exists()) { 

							header = "HTTP/1.1 200 OK\r\n"
									+ "Content-type:contentType\r\n";

							long fileSize = f.length();
							
							// Http response status line and header
							header += "Content-size: " + fileSize + "\r\n";
							header += "Connection: Close\r\n\r\n"; 
							
							// reading from file (http body) and sending(with http header)
							FileInputStream fis = new FileInputStream(f); 
							output.write(header.getBytes());
							
							int ch = fis.read(buffer, 0, BUFFER_SIZE);
							while (ch != -1) {
								output.write(buffer, 0, ch);
								ch = fis.read(buffer, 0, BUFFER_SIZE);
							}
							output.flush();
						} else {
							header = "HTTP/1.1 404 Not found\r\n";
						}
					}
						// if we receive a string updateData, we have a socket conn.
						// (web page already loaded)	
					} else if (data.startsWith("downloadData")) { 
							String exp = data.split("|")[1];
							System.out.println("requesting data for exp.: "+exp);
							if (!subscribers.containsKey(exp))
									subscribers.put(exp,new ArrayList());
						
							ArrayList<Socket> tmp = subscribers.get(exp);
							tmp.add(client);
							subscribers.put(exp,tmp);	
					} else {
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

			contentType = lines[3].split(":")[1]; // Take the content type from
													// the http request
			if (contentType.startsWith("text/html"))
				contentType = "text/html"; // hack: for html pages use simpler
											// content type string

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
