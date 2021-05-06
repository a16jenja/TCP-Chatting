import java.util.ArrayList;

public class Driver {
	public static void main(String[] args) {
		// Client settings
		String[] clients = {"Jens", "John", "Alex", "Kalle", "Mohammed"};
		int[] clientPorts = {5678, 8765, 8756, 4312, 1359};
		ArrayList<Client> clients1 = new ArrayList<Client>();
		
		// Server settings
		String serverAddressString = "localhost";
		int serverPort = 1234;
		
		// Create a server instance
		Server server = new Server(serverPort);
		// Start server thread
		server.start();

		// Create client instances
		for(int i = 0; i < clients.length; i++) {
			// Create a client instance
			clients1.add(new Client(clientPorts[i], clients[i]));
			
			// Set server parameters for client
			clients1.get(i).setServerParameters(serverAddressString, serverPort);
			
			// Connect client to the server
			clients1.get(i).connectToServer();
			
			// Start client instance
			clients1.get(i).start();
		}
	}
}
