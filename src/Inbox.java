
public class Inbox {
	private static Inbox instance = null;
	private String response="";
	private String request="";
	
	
	private boolean newRequest=false;
	
	protected Inbox(){}
	public static Inbox getInstance() {
	      if(instance == null) {
	         instance = new Inbox();
	      }
	      return instance;
	   }
	public void setRequest(String data){
		synchronized (request) {
			this.request=request;
		}
		this.newRequest=true;
	}
	public String getRequest(){
		return this.request;
	}
	public String getResponse(){
		synchronized (response) {
			return this.response;
		}
	}
	public void setResponse(String response){
		synchronized (response) {
			this.response=response;
		}
	}
	
	public boolean isNewRequest(){
		return this.newRequest;
	}
}
