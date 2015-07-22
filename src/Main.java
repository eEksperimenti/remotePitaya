import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
	static boolean serviceRunning = false;
	private static PitayaServer service;
	static int port=-1;
	static String[] lookupTable;
	static String dbIP,dbName,dbUser,dbPass="";

	public static void main(String[] args) {
		System.out.println("*************** WELCOME ***************");
		System.out.println("Commands:\nstart - Start the remote pitaya service\n"
						+ "stop - Stop the Pitaya service\n"
						+ "shutdown - Shutdown the server");
		Scanner sc = new Scanner(System.in);

		while (true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.print(">");
			String command = sc.nextLine();
			
			switch (command) {

			case "start":
				if(readConfigFile()){
					service = new PitayaServer(port);
					new Thread(service).start();
					serviceRunning = true;
				}
				break;

			case "stop":
				if (serviceRunning) {
					service.stopService();
					serviceRunning = false;
					service = null;
					System.out.println("Service stoped");
				} else
					System.out.println("Can't stop a non-running service. Start the service first!");
				break;
				
			case "shutdown":
				if (serviceRunning) {
					service.stopService();
					serviceRunning = false;
					service = null;
				}
				System.out.println("Server shuting down.... ");
				System.exit(0);
				break;
			case "import_settings":
				if (serviceRunning) {
					service.stopService();
					serviceRunning = false;
					service = null;
				}
				if (!readConfigFile())
					System.out.println("Error: Can't import settings!");
				break;

				
			default:
				System.out.println("Error: Unknown command!");
				break;
			}

		}

	}
	public static boolean readConfigFile(){
		try {
			
			Document doc = Jsoup.parse(new File("config/config.xml"),"UTF-8","");
			Element server = doc.getElementsByTag("server").first();
				port = Integer.parseInt(server.attr("port"));
				
			Element database = doc.getElementsByTag("database").first();
				dbIP = database.attr("ip");
				dbUser = database.attr("username");
				dbPass = database.attr("password");
				dbName = database.attr("dbname");
				
			Elements pitayas= doc.getElementsByTag("pitaya");
				lookupTable = new String[pitayas.size()];
				for (Element pitaya : pitayas){
					lookupTable[Integer.parseInt(pitaya.attr("num"))-1] = pitaya.attr("ip")+"/"+pitaya.attr("experiment");
					System.out.println("Pitaya "+"num "+Integer.parseInt(pitaya.attr("num"))+": "+pitaya.attr("ip")+"/"+pitaya.attr("experiment"));
				}
				
			return true;
			
		} catch (FileNotFoundException e) {
			System.out.println("Error: No config file found!");
			return false;
		}catch (IOException e){
			System.out.println("Error: Can't read config file!");
			return false;
		}
		
	}
}
