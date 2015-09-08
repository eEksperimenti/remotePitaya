import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.Connection;

public class PitayaServer implements Runnable {
	private ServerSocket server;
	private Socket client;
	private int port;
	private boolean running, isAdmin=false, tokenValid=false;
	private int pitayaNum = -1;
	private OutputStream output;
	private InputStream input;
	private String token, experiment, fileName, method, contentType, data = "";
	private final int BUFFER_SIZE = 1;
	private boolean[] runningPitaya = new boolean[Main.lookupTable.length];
	private PitayaDataFetcher[] fetchers = new PitayaDataFetcher[Main.lookupTable.length];

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
		while (this.running) {
			try {
				// accept incoming connections
				client = this.server.accept(); 
				if (client.isConnected()) {
					System.out.println("Client connected!\n IP:"
							+ client.getInetAddress() + "\n Port: "
							+ client.getPort());
					//open input/output streams
					 input = client.getInputStream(); 
					 output = client.getOutputStream();
					String header = "";
					
					BufferedReader bf = new BufferedReader(new InputStreamReader(input));
					String tmp="";
					StringBuilder sb =new StringBuilder();
					int length=-1;
		            while ((tmp = bf.readLine()) != null) {
		                if (tmp.equals("")) {
		                	break;
		                }
		                if (tmp.startsWith("Content-Length: ")) { 
		                    int index = tmp.indexOf(':') + 1;
		                    String len = tmp.substring(index).trim();
		                    length = Integer.parseInt(len);
		                }
		                System.out.println("TMP: "+tmp);
		                sb.append(tmp + "\r\n");
		            } 
		            StringBuilder content=new StringBuilder();
		            System.out.println("Len: "+length);
		            if (length > 0) {
		                int read;
		                while ((read = bf.read()) != -1) {
		                	System.out.print("|"+(char) read);
		                   content.append((char) read);
		                    if (content.length() == length){
		                    	System.out.println("Content len: "+content.length()+" = "+length);
		                        break;
		                    }
		                }
		            }
		            System.out.println("Content len: "+content.length());
		            sb.append("\r\n"+content.toString()); // adding the body to request
					data=sb.toString();
					System.out.println(data);
				
					// If we have a http GET request 
					if (data.startsWith("GET") && getParameters(data)) {	
						
						//Check if we are already fetching from the desired pitaya
					/*	if (!runningPitaya[pitayaNum-1] ){
							PitayaDataFetcher fetcher = new PitayaDataFetcher(Main.lookupTable[pitayaNum-1],this.experiment);
							fetchers[pitayaNum-1] = fetcher;
							Thread t = new Thread(fetcher);
							t.start();
							runningPitaya[pitayaNum-1] = true;
						}*/
						//If requesting data, read from pitayaBuffer and send to client
						if (fileName.startsWith("/data") ){
							String responseData="";
							if (PitayaDataFetcher.pitayaBuffer == null)
								responseData="{\"app\":{},\"datasets\":{},\"status\":\"ERROR\",\"reason\":\"Application not loaded\"}";
							else
								responseData = PitayaDataFetcher.pitayaBuffer.readData();

							header = "HTTP/1.1 200 OK\r\n"
									+ "Content-type: application/json\r\n"
									+ "Content-size: " + responseData.length() + "\r\n"
									+ "Connection: Close\r\n\r\n";
									
							System.out.println("SENDING DATA.....");
							output.write(header.getBytes());
							output.write(responseData.getBytes());
							output.flush();
						}
						else if (fileName.contains("adminControl")){
							System.out.println("###### ADMIN REQUEST");
							String adminResponse = "";
							if (isTokenValid())
								adminResponse = "{\"tokenValid\":1}";
							else
								adminResponse = "{\"tokenValid\":0}";
							
							header = "HTTP/1.1 200 OK\r\n"
									+ "Content-type: application/json\r\n"
									+ "Content-size: " + adminResponse.length() + "\r\n"
									+ "Connection: Close\r\n\r\n";
							output.write(header.getBytes());
							output.write(adminResponse.getBytes());
							output.flush();
						}
						else if (fileName.startsWith("/bazaar?stop") && isTokenValid() ){
							PitayaDataFetcher fetcher = fetchers[pitayaNum-1];
							fetcher.stopApp();
							
						}else if (fileName.startsWith("/bazaar?start")){
							this.experiment = fileName.substring(fileName.indexOf("=")+1);
							System.out.println("EX: "+this.experiment);
							PitayaDataFetcher fetcher = new PitayaDataFetcher(Main.lookupTable[pitayaNum-1],this.experiment);
							fetchers[pitayaNum-1] = fetcher;
							Thread t = new Thread(fetcher);
							t.start();
													
							Thread.sleep(1000);
							String bazarData = fetcher.getBazarData();
							System.out.println("BazarData: "+bazarData);
							String bazarHeader  = "HTTP/1.1 200 OK\r\n"
									  +"Server: nginx/1.5.3\r\n"
									  +"Date: Thu, 01 Jan 1970 02:57:27 GMT\r\n"
									  +"Content-Type: application/json\r\n"
									  +"Content-Length: "+bazarData.length()+"\r\n"
									  +"Connection: close\r\n"
									  +"Access-Control-Allow-Origin: *\r\n"
									  +"Access-Control-Allow-Credentials: true\r\n"
									  +"Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n"
									  +"Access-Control-Allow-Headers: DNT,X-Mx-ReqToken," +
									  "Keep-Alive,User-Agent,X-Requested-With," +
									  "If-Modified-Since,Cache-Control,Content-Type\r\n\r\n";
							output.write(bazarHeader.getBytes());
							output.write(bazarData.getBytes());
							output.flush();
						}
						else{
						// Send the file
							sendFile();
					}
				}else if (data.startsWith("POST") && getParameters(data)){			
					try{
							System.out.println("######### POST request ########");
							/*data is our POST request */
							System.out.println("Len: "+data.length());
							
							/*Separate the http body from the content (JSON data)*/
							String appParams     = data.split("\r\n\r\n")[1];
							String appBody       = data.split("\r\n\r\n")[0];
							
							/*Clean the params - not working */
							/*Return to the app the same params and add status:ok at the end*/
							int len = appParams.length();
							String responseParams = appParams.substring(0,len-1)+ ",\"status\":\"OK\"}";
							
							System.out.println("DIRTY: \n"+ appParams+"\n-------------");
							System.out.println("CLEAN PARAMS: \n"+appParams+"|\n-------------");
	
							/*Get the pitaya number*/
							String firstLine 	= data.split("\r\n")[0];
							String resource 	= firstLine.split(" ")[1];
							String firstParam	= resource.split("\\?")[1];
							//String firstParam 	= getParams.split("&")[0];
							System.out.println("FIRST PARAM: "+firstParam+" split: "+firstParam.split("=")[1]);
							int num=-1;
							if (firstParam.startsWith("p")) 
								 num  = Integer.parseInt(firstParam.split("=")[1]);
							
							/*Get the fetcher object and set the wait flag to true -
							 * no data fetching during parameter posting
							*/
							PitayaDataFetcher fetcher = fetchers[num-1];
							fetcher.setWait(true);
							/* Send the parasm to pitaya and get the response (response relevant only for the date field ) */
							String pitayaData = fetcher.sendParameters(appBody,appParams);
							String pitayaBody = pitayaData.split("\r\n\r\n")[0];
							String date       = pitayaBody.substring(pitayaBody.indexOf("Date"),pitayaBody.indexOf("Date")+31);
					
							System.out.println("## Response\n"+pitayaData);
							
							/*Compose the entire http response package and send it to the app. */
							String response ="HTTP/1.1 200 OK\r\n"
										   +"Server: nginx/1.5.3\r\n"
										   +date+"\r\n"
										   +"Content-Type: application/json\r\n"
										   +"Content-Length: "+responseParams.length()+"\r\n"
										   +"Connection: close\r\n"
										   +"Access-Control-Allow-Origin: *\r\n"
										   +"Access-Control-Allow-Credentials: true\r\n"
										   +"Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n"
										   +"Access-Control-Allow-Headers: DNT,X-Mx-ReqToken,Keep-Alive," +
										   "User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type\r\n\r\n" 
										   +responseParams;	
						
							output.write(response.getBytes());
							output.flush();
							output.close();
							fetcher.setWait(false);

							}catch(Exception e){
								e.printStackTrace();
							}	
						
				}else {
					input.close();
					client.close();
				}
				output.close();
				client.close();
				}

			} catch (IOException | InterruptedException e) {
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
			//pitayaNum=-1;experiment="";token="";
			// Split entire package into lines
			String[] params =null;
			String[] lines = data.split("\r\n"); 
			if (lines.length < 1)
				return false;
			// Split the status line in three parts (0-method, 1-URL, 2-protocol version)
			String[] status = data.split(" "); 
			method = status[0]; // method GET or POST
			// Check if we have any GET parameters
			System.out.println("Status len: "+status.length+"\n"+lines[0]);
			if (status[1].indexOf("&") > 0) { 
				System.out.println("status inside");
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
				this.pitayaNum = Integer.parseInt(params[0].split("=")[1]);
				this.experiment = (params.length > 1) ? params[1].split("=")[1] : "";
				this.token = (params.length > 2) ? params[2].split("=")[1] : "";
			}

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
			System.out.println("## Method: "+method+" Filename: "+fileName+" pitayaNum: "+pitayaNum+" ex: "+experiment+" token: "+token);

		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public void sendFile(){
		try{
			byte[] buffer = new byte[BUFFER_SIZE]; 
		
			File f = new File("apps/" + fileName);
			if (f.exists()) { 
				long fileSize = f.length();
				
				// reading from file (http body) and sending(with http header)
				FileInputStream fis = new FileInputStream(f); 
				
				// Http response status line and header
				String header = "HTTP/1.1 200 OK\r\n"
						+ "Content-type: "+contentType+"\r\n"
						+ "Content-size: " + fileSize + "\r\n"
						+ "Connection: Close\r\n\r\n"; 
				output.write(header.getBytes());
		
		
				int ch = fis.read(buffer, 0, BUFFER_SIZE);
				while (ch != -1) {
					output.write(buffer, 0, ch);
					ch = fis.read(buffer, 0, BUFFER_SIZE);
				}
				output.flush();
		}else{
			
		}
		}catch(IOException e){
			e.printStackTrace();
		} 
	}
	

	public boolean isTokenValid() {
		if (this.token == null)
			return false;
		try {
			String query =  "SELECT  count(i.reference_number) as num "
							+"FROM reservation_instances as i, reservation_series as s "
							+"WHERE i.series_id = s.series_id " 
							+"AND s.status_id = 1 "
							+"AND i.reference_number = '"+this.token+"' "
							+"AND date_format(curdate(),'%d/%m/%Y') between date_format(i.start_date,'%d/%m/%Y') and date_format(i.end_date,'%d/%m/%Y')";
			System.out.println("QUERY: "+query);
			
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = (Connection) DriverManager.getConnection("jdbc:mysql://194.249.0.123:3306/bookedscheduler","remotePitaya","MtRZnsFm8KZ");
			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(query);
			if(rs.next()){
				int num = Integer.parseInt(rs.getString("num"));
				System.out.println("######## MYSQL RS: "+num);
				if (num == 1)
					return true;
			}
		
				return false;
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

}
