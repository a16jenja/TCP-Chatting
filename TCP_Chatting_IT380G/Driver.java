
public class Driver {
	public static void main(String[] args) {
		// Client settings
		String[] clients = { "Jens", "John", "Alex", "Kalle", "Mohammed"};
		
		// Server settings
		String serverString = "localhost";
		int serverPort = 1234;

		// Create a server instance
		Server server = new Server(serverPort);
		// Start server thread
		server.start();

		// Create client instances
		for (int i = 0; i < clients.length; i++) {
			// Create a client instance
			Client client = new Client(clients[i]);

			client.setServerParameters(serverString, serverPort);

			client.connectToServer();

			client.start();
		}
	}
}
