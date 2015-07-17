
public class Inbox {
	private static Inbox instance = null;
	private String respons="";
	private String request="";
	
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
	}
	public String getResponse(){
		synchronized (respons) {
			return this.respons;
		}
		
	}
}
