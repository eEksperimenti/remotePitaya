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
			Thread.sleep(50);
		                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
		URL url = new URL(this.ip+"/data");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-type", "application/json;charset=utf-8");
		conn.connect();
		BufferedReader br=null;
		
		if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201){
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String jsonData="",tmp="";
			while ((tmp = br.readLine()) != null){
				jsonData +=tmp;
			}
			//add to circural buffer
					
			jsonData="";
			
		}
		br.close();
		conn.disconnect();
		
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
