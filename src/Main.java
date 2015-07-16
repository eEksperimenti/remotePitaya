import java.util.*;

public class Main {
	static boolean serviceRunning = false;
	static PitayaServer service;

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
				service = new PitayaServer(3000);
				new Thread(service).start();
				serviceRunning = true;
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
				
			default:
				System.out.println("Error: Unknown command!");
				break;
			}

		}

	}
}
