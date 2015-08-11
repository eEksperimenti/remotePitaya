
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class PitayaServer implements Runnable {
	private ServerSocket server;
	private int port;
	private boolean running;
	private Hashtable<String, Socket> clients;
	private String token = "";
	private int pitayaNum = -1;
	private int user = -1;
	private String fileName,method,contentType="";
	private String data="";
	private final int BUFFER_SIZE= 1024;
	

	public PitayaServer(int port) {
		this.port = port;
		this.running = false;
	}

	@Override
	public void run() {

		openServerSocket();      				//init the server socket
		byte[] buffer= new byte[BUFFER_SIZE];	//init the buffer to be used later for reading from input

		while (this.running) {
			try {
				Socket client = this.server.accept(); //accept incoming connections
				if (client.isConnected()) {
					System.out.println("Client connected!\n IP:"
							+ client.getInetAddress() + "\n Port: "
							+ client.getPort());
					
				InputStream input = client.getInputStream();     //open input/output streams
				OutputStream output = client.getOutputStream();
				String header="";
				
				input.read(buffer);					//read the http request
				data = new String(buffer, "UTF-8"); 
				
					if (data.startsWith("GET") && getParameters(data)){  //If we have a http GET request (user loading web page)
						
						File f = new File("apps/"+fileName);			
						if (f.exists()){       							//Check if we have the specified file

							header ="HTTP/1.1 200 OK\r\n" 				
									+"Content-type:contentType\r\n";
									
							long fileSize = f.length();
							
								header+="Content-size: "+fileSize+"\r\n";
								header+="Connection: Close\r\n\r\n";     //Http response status line and header
							
							FileInputStream fis = new FileInputStream(f); //reading from file (http body) and sending (with http header) 
							output.write(header.getBytes());
							int ch = fis.read(buffer, 0, BUFFER_SIZE);
							while (ch != -1) 
							{
								output.write(buffer, 0, ch);
								ch = fis.read(buffer, 0, BUFFER_SIZE);
							}
							output.flush();
						}else{
							header = "HTTP/1.1 404 Not found\r\n";
						}
									
						
						
					}else if (data.startsWith("updateData")){           //if we receive a string updateData, we have a socket conn. (web page already loaded)
						
					}else{
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
			String[] params=null; 
			String[] lines = data.split("\r\n");			//Split entire package into lines
			String[] status= data.split(" ");				//Split the status line in three parts (0-method, 1-URL, 2-protocol version)
			method = status[0];
			
			if (status[1].indexOf("&")>0){					//Check if we have any GET parameters
				String[] resource = status[1].split("\\?"); //Separate the URL from the parameters
				fileName = resource[0];							
				if (resource.length > 1){ 					
					 params= resource[1].split("&");		//Divide each parameter
				}
			}else{											//If no parameter is present, take the entire string
				fileName=status[1];
			}
			if (params != null){							//
				this.pitayaNum=Integer.parseInt(params[0].split("=")[1]);
				this.token=params[1].split("=")[1];
			}
			
			contentType=lines[3].split(":")[1];             //Take the content type from the http request
			if (contentType.startsWith("text/html")) contentType = "text/html"; //hack: for html pages use simpler content type string

		
			if (pitayaNum > Main.lookupTable.length){       //Check if pitayaNum is a valid number
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
