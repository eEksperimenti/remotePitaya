
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
	public synchronized void setRequest(String data){
			this.request=data;
			System.out.println("Inbox req. set: "+this.request);
			this.newRequest=true;
		
	}
	public synchronized String getRequest(){
		String newRequest= this.request;
		this.request="";
		this.newRequest=false;
		return newRequest;
	}
	public boolean isNewRequest(){
		return this.newRequest;
	}
	/*public String getResponse(){
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
	}*/
}
