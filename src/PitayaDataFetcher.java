import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.InterruptibleChannel;

public class PitayaDataFetcher implements Runnable{
	private String ip;
	private String experiment = "";
	private String bazarData="";
	static PitayaBuffer pitayaBuffer;
	private volatile boolean wait=false;
	private volatile boolean  running = false;
	public static boolean bazarResponse=false;
	private HttpURLConnection conn;
 	public PitayaDataFetcher(String ip,String experiment) {
 		
		this.ip=ip;
		this.experiment=experiment;
		this.pitayaBuffer=new PitayaBuffer();
		this.running = true;
	}
	public void run() {
		try {
			URL bazarURL = new URL("http://"+this.ip+":80/bazaar?start="+experiment);
			HttpURLConnection bazarConn = (HttpURLConnection) bazarURL.openConnection();
			bazarConn.setRequestMethod("GET");
			bazarConn.setRequestProperty("Accept", "application/json;charset=utf-8");
			bazarConn.setRequestProperty("X-Requested-With","XMLHttpRequest");
			bazarConn.setRequestProperty("Referer:","http://192.168.94.134/apps/");
			bazarConn.setRequestProperty("Connection", "keep-alive");
			bazarConn.connect();
			
			BufferedReader br=null;
			System.out.println("START: Poslano!");
			if (bazarConn.getResponseCode() == 200 || bazarConn.getResponseCode() == 201){
				br = new BufferedReader(new InputStreamReader(bazarConn.getInputStream()));
				String jsonData="",tmp="";
				while ((tmp = br.readLine()) != null){
					jsonData +=tmp;
				}
				bazarData = jsonData;
				bazarResponse=true;
				jsonData="";
				System.out.println("START: Prejeto!");
				
			}
			bazarConn.disconnect();		
		
			while (running){
				while(this.wait){continue;}

	
				URL url = new URL("http://"+this.ip+":80/data");
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-type", "application/json;charset=utf-8");
				conn.setRequestProperty("X-Requested-With","XMLHttpRequest");
				conn.setRequestProperty("Connection", "keep-alive");
				conn.connect();
		

				if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201){
					
					br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String jsonData="",tmp="";
					while ((tmp = br.readLine()) != null){
						jsonData +=tmp;
					}
				//	System.out.println("IP: "+ip+" DATA:\n"+jsonData);
					if (pitayaBuffer != null)
					pitayaBuffer.writeData(jsonData);
					
					jsonData="";
					
				}
				br.close();
				conn.disconnect();
				Thread.sleep(20);
			

		}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("PREKINJENO");
			e.printStackTrace();
			return;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
			
	}
	public  String sendParameters(String body,String pitayaParams){
		try{ 
			/*Get the socket and I/O strams*/
			Socket s = new Socket(this.ip,80);
			String request ="";
			if (pitayaParams.equals(""))
				 request = body;
			else
			request = body+"\r\n\r\n"+pitayaParams;
			
			/*send the request*/
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			String jsonData="",tmp="";
			dos.write(request.getBytes());
			dos.flush();
			
			
		
			/*Read the response*/
			InputStream input = s.getInputStream();
			BufferedReader bf = new BufferedReader(new InputStreamReader(input));
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

                sb.append(tmp + "\r\n"); // append the request
            } // end of while to read headers

            // if there is Message body, go in to this loop
            StringBuilder content=new StringBuilder();
            if (length > 0) {
                int read;
                while ((read = bf.read()) != -1) {
                   content.append((char) read);
                    if (content.length() == length){
                        break;
                    }
                }
            }

            sb.append("\r\n"+content.toString()); // adding the body to request
			String data =sb.toString();
            dos.close();
			bf.close();
			s.close();
            return  sb.toString();
			
			
		}catch(IOException e){
			System.out.println(e.toString());
			return "";
		}	
	}
	
	public String stopApp(){
		try {
			URL stopURL = new URL("http://"+this.ip+":80/bazaar?stop=");
			HttpURLConnection stopConn = (HttpURLConnection)stopURL.openConnection();

			stopConn.setRequestMethod("GET");
			stopConn.setRequestProperty("Host", "212.235.190.181:5950");
			stopConn.setRequestProperty("Accept:", "*/*");
			stopConn.setRequestProperty("Accept-Language:", "en-US,en;q=0.5");			
			stopConn.setRequestProperty("Accept-Encoding:", "gzip, deflate");
			stopConn.setRequestProperty("DNT:", "1");
			stopConn.setRequestProperty("X-Requested-With:","XMLHttpRequest");
			stopConn.setRequestProperty("Connection:","keep-alive");
			stopConn.connect();
			String jsonData="",tmp="";
			if (stopConn.getResponseCode() == 200 || stopConn.getResponseCode() == 210){
				
				BufferedReader br = new BufferedReader(new InputStreamReader(stopConn.getInputStream()));
				
				while ((tmp = br.readLine()) != null){
					jsonData +=tmp;
				}
				
				System.out.println("CLOSED!");
				this.running = false;
				this.pitayaBuffer.clearBuffer();
				this.pitayaBuffer=null;
				return jsonData;
				
			}
			return "";
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "";

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
}
/*	public void killThread(){
		System.out.println("KILLING");
		Thread.currentThread().interrupt();
	}*/

	public String getBazarData(){
		return this.bazarData;
	}
	public PitayaBuffer getBuffer (){
		return this.pitayaBuffer;
	}
	public void setWait(boolean value ){
			this.wait=value;
	}

	public String getEx(){
		return this.experiment;
	}
	
}
