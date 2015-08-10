
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
	private String fileName="";
	private String data="";
	private final int BUFFER_SIZE= 1024;
	

	public PitayaServer(int port) {
		this.port = port;
		this.running = false;
	}

	@Override
	public void run() {

		openServerSocket();
		byte[] buffer= new byte[BUFFER_SIZE];

		while (this.running) {
			try {
				Socket client = this.server.accept();
				if (client.isConnected()) {
					System.out.println("Client connected!\n IP:"
							+ client.getInetAddress() + "\n Port: "
							+ client.getPort());
					
				InputStream input = client.getInputStream();
				OutputStream output = client.getOutputStream();
				String header="";
				
				input.read(buffer);
				data = new String(buffer, "UTF-8");
				
					if (data.startsWith("GET") && getParameters(data)){
						
						File f = new File("apps/fileName");
						if (f.exists()){

							header ="HTTP/1.1 200 OK\r\n"
									+"Content-type: text/html; charset=UTF-8\r\n";
							long fileSize = f.length();
							
								header+="Content-size: "+fileSize+"\r\n";
							
							FileInputStream fis = new FileInputStream(f);
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
						
						
						
					}else if (data.startsWith("updateData")){
						
					}else{
						input.close();
						client.close();
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
	public boolean getParameters(String data) {
		try {
			System.out.println(data);
			String params[]= data.split("&");
			fileName= data.split(" ")[1].substring(0,data.indexOf("/"));
			
			if (params.length>1){
				this.pitayaNum=Integer.parseInt(params[0].split("=")[1]);
				this.token=params[1].split("=")[1];
			}else
				this.token=params[0].split("=")[1];
			
			System.out.println("fileName: "+fileName+" pitayaNum: "+pitayaNum+" token: "+pitayaNum);

			if (pitayaNum > Main.lookupTable.length){
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
