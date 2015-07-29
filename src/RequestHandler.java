import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class RequestHandler implements Runnable {
	private Socket client;
	private Inbox inbox;

	public RequestHandler(Socket client, Inbox inbox) {
		this.client = client;
		this.inbox = inbox;
		
	}

	public void run() {
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
			InputStream stream = client.getInputStream();

			
			String request = "";
			String tmp = "";
			/*while (true) {
				System.out.println("Stream ready: "+in.ready());
				while ((tmp = in.readLine()) != null){
					System.out.println("tmp (requestHandler):\n "+tmp);
					request+=tmp;
				}*/
			byte buf[] = new byte[1024];
			int bytesRead=0;
			while (( bytesRead = stream.read(buf)) != -1) {
		        request+=bytesRead;
		    }
				System.out.println("## NEW REQUEST ##:\n  "+request);
				System.out.println("Starts with GET: "+request.startsWith("GET"));
				
				if (request.length() > 0)
					inbox.setRequest(request);
				request ="";
			
		} catch (IOException e) {
			e.printStackTrace();
			try {
				client.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}

}
