import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class PitayaDataFetcher implements Runnable{
	private String ip;
	private String experiment = "";
	static PitayaBuffer pitayaBuffer;
	private boolean wait=false;
	private HttpURLConnection conn;
 	public PitayaDataFetcher(String ip,String experiment) {
		this.ip=ip;
		this.experiment=experiment;
		this.pitayaBuffer=new PitayaBuffer();
		
	}
	public void run() {
		try {
			URL bazarURL = new URL("http://"+this.ip+":80/bazaar?start=scope+gen_translation");
			HttpURLConnection bazarConn = (HttpURLConnection) bazarURL.openConnection();
			bazarConn.setRequestMethod("GET");
			bazarConn.setRequestProperty("Accept", "application/json;charset=utf-8");
			bazarConn.setRequestProperty("X-Requested-With","XMLHttpRequest");
			bazarConn.setRequestProperty("Referer:","http://192.168.94.134/apps/");
			bazarConn.setRequestProperty("Connection", "keep-alive");
			bazarConn.connect();
			
			
			BufferedReader br=null;
			
			if (bazarConn.getResponseCode() == 200 || bazarConn.getResponseCode() == 201){
				br = new BufferedReader(new InputStreamReader(bazarConn.getInputStream()));
				String jsonData="",tmp="";
				while ((tmp = br.readLine()) != null){
					jsonData +=tmp;
				}
				System.out.println("bazar:\n"+jsonData);						
				jsonData="";
				
			}
			bazarConn.disconnect();		
		
			while (true){
				Thread.sleep(50);
				while(this.wait){}
	
				URL url = new URL("http://"+this.ip+":80/data");
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-type", "application/json;charset=utf-8");
				conn.setRequestProperty("X-Requested-With","XMLHttpRequest");
				conn.setRequestProperty("Connection", "keep-alive");
				conn.connect();
			//	System.out.println("Data fetching from Pitaya: "+ip);
						
				if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201){
					br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String jsonData="",tmp="";
					while ((tmp = br.readLine()) != null){
						jsonData +=tmp;
					}
				//	System.out.println("IP: "+ip+" DATA:\n"+jsonData);
					pitayaBuffer.writeData(jsonData);
					System.out.println("Data updated!");
					jsonData="";
					
				}
				br.close();
				conn.disconnect();
		}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
			
	}
	public  String sendParameters(String pitayaParams){
		/*try{
			
			wait = true;
			//conn.disconnect();
			byte[] data = pitayaParams.getBytes(StandardCharsets.UTF_8);
			int len = data.length;
			System.out.println("Fetcher!!");
			URL url = new URL("http://"+this.ip+":80/data");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);	
			conn.setRequestMethod("POST");
			conn.setInstanceFollowRedirects(true);
			conn.setRequestProperty("Content-length", Integer.toString(len));
			conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("X-Requested-With","XMLHttpRequest");
			conn.setRequestProperty("Connection", "keep-alive");
			System.out.println("Request: "+conn.toString()+"\n");
			
			conn.connect();

			//System.out.println("Fetcher2"+conn.getResponseCode());

			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			dos.write(data);
			dos.flush();
			System.out.println("Response: "+conn.getResponseMessage());
			dos.close();
			
			String jsonData="",tmp="";
			BufferedReader br=null;
			if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201){
				System.out.println("Response OK");
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while ((tmp = br.readLine()) != null){
					jsonData +=tmp;
				}
				System.out.println("POST json:\n"+jsonData);	
			}
			wait=false;
			br.close();
			conn.disconnect();
			return jsonData;*/
			
			try{
				Socket s = new Socket(this.ip,80);
				String request = "GET /data HTTP/1.1\r\n"
								+"Host: "+this.ip+"\r\n"
								+"Accept: */*\r\n"
								+"Accept-Language: en-US,en;q=0.5\r\n"
								+"Accept-Encoding: gzip, deflate\r\n"
								+"DNT: 1\r\n"
								+"Content-Type: application/x-www-form-urlencoded; charset=UTF-8\r\n"
								+"X-Requested-With: XMLHttpRequest\r\n"
								+"Content-Length: "+pitayaParams.length()+"\r\n"
								+"Connection: keep-alive\r\n"
								+"Pragma: no-cache\r\n"
								+"Cache-Control: no-cache\r\n\r\n"
								+pitayaParams;
			//	System.out.println("-----------------//---------------------------\n"+request);
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				String jsonData="",tmp="";
				dos.write(request.getBytes());
			//	dos.write(pitayaParams.getBytes());
				dos.flush();
				System.out.println();
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
					
					while ((tmp = br.readLine()) != null){
						jsonData +=tmp;
					}
				dos.close();
				br.close();
				s.close();
				return jsonData;
				
			}catch(IOException e){
				System.out.println(e.toString());
				return "";
			}
		
	
	}
	public PitayaBuffer getBuffer (){
		return this.pitayaBuffer;
	}
	public void setWait(boolean value ){
		this.wait=value;
	}
	
}
