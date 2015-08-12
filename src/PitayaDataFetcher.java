import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class PitayaDataFetcher implements Runnable{
	private String ip;
	public PitayaDataFetcher(String ip) {
		this.ip=ip;
	}
	public void run() {
		try {
			URL appUrl = new URL("http://"+this.ip+":80/soncna_celica");
			HttpURLConnection tmpConn = (HttpURLConnection) appUrl.openConnection();
			tmpConn.setRequestMethod("GET");
			tmpConn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			tmpConn.setRequestProperty("Referer:","http://192.168.94.134/apps/");
			tmpConn.setRequestProperty("Connection", "keep-alive");
			tmpConn.connect();
		
			while (true){
				Thread.sleep(50);
	
				URL url = new URL("http://"+this.ip+":80/data?_=1437573753404");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-type", "application/json;charset=utf-8");
				conn.setRequestProperty("X-Requested-With","XMLHttpRequest");
				conn.setRequestProperty("Connection", "keep-alive");
				conn.connect();
				System.out.println("Data fetching from Pitaya: "+ip);
		
				BufferedReader br=null;
				
				if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201){
					br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String jsonData="",tmp="";
					while ((tmp = br.readLine()) != null){
						jsonData +=tmp;
					}
					System.out.println("IP: "+ip+" DATA:\n"+jsonData);
					//add to circural buffer
							
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
