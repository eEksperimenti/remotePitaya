import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PitayaDataFetcher implements Runnable{
	private String ip;
	private String experiment = "";
	static PitayaBuffer pitayaBuffer;
 	public PitayaDataFetcher(String ip,String experiment) {
		this.ip=ip;
		this.experiment=experiment;
		this.pitayaBuffer=new PitayaBuffer();
	}
	public void run() {
		try {
			URL bazarURL = new URL("http://"+this.ip+":80/bazaar?start=soncna_celica");
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
	
				URL url = new URL("http://"+this.ip+":80/data");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
	
}
