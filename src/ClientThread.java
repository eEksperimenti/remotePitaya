import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;


public class ClientThread implements Runnable{
	private Socket client;
	private Inbox inbox;
	public ClientThread(Socket client,Inbox inbox){
		this.client=client;
		this.inbox=inbox;
	}

	@Override
	public void run() {
	
		try {
			
			String header ="HTTP/1.1 200 OK\nContent-Type: text/html";
		 	String data ="<b>Hello user!</b></br> You are now connected to our server for remote experimenting.</br>";
		 	
	    	BufferedWriter  out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(),"UTF-8"));
	    	BufferedReader 	in = new BufferedReader(new InputStreamReader(client.getInputStream(),"UTF-8"));

		    out.write(header+"\n");
		    out.write("Content-Length: " + data.length()+"\r\n");
		    out.write("\r\n");
		    out.write(data+"\r\n");
		    out.write("\r\n");
		    out.flush();
		    String request,response ="";
		    
		    while ((request = in.readLine()) != null){
		    		inbox.setRequest(request);
		    		String response = inbox.getRespons();
		    }
		    
	    
		    //out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
