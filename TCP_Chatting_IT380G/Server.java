import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Server extends Thread {
	int portNumber;
	String replyMessage;
	public static Hashtable<String, ClientHandler> connectedMembers = new Hashtable <String, ClientHandler>();

	public Server(int serverPort) {
		// Identify server program by serverPort
		this.portNumber = serverPort;
	}

	public void setReplyMessage(String replyMessage) {
		this.replyMessage = replyMessage;
	}

	public void run() {
		ServerSocket serverSocket = null;
		Socket socket = null;
		
		// Create a server plug
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Create a communication channel
		EndPoint serverEnd = new EndPoint();
		
		do {
			// Build a socket and plug it to server, then listen to incoming streams
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ClientHandler handleClient = new ClientHandler(socket, serverEnd, replyMessage);
			
			handleClient.start();
		} while (true);
	}
}

class ClientHandler extends Thread {
	Socket socket;
	EndPoint serverEnd;
	String replyMessage;
	
	public ClientHandler(Socket socket, EndPoint serverEnd, String replyMessage) {
		this.socket = socket;
		this.serverEnd = serverEnd;
		this.replyMessage = replyMessage;
	}
	
	public void run() {
		do {
			// Receive a message from client
			String receivedMessage = serverEnd.readStream(socket);
			
			// Split the message up
			String[] formattedMessage = receivedMessage.split("-");

			// Check whether it is a “handshake” message
			if (receivedMessage.startsWith("/handshake")) {
				addNewMember(receivedMessage);
				continue;
				// Check if the user wants to leave the chatroom
			} else if (formattedMessage[1].startsWith("/leave")) {
				leaveChatRoom(receivedMessage);
				continue;
				// Check if the user wants to send a private message to another user
			} else if (formattedMessage[1].startsWith("/tell")) {
				sendPrivateMessage(receivedMessage);
				continue;
				// Check if the user wants to list all the members that are connected to the chatroom
			} else if (formattedMessage[1].startsWith("/list")) {
				listMembers(formattedMessage[0]);
				continue;
				// Check if the message is is going to be used for calculating round trip time
			} else if (formattedMessage[1].startsWith("/calculateRTT")) {
				serverEnd.writeStream(socket, receivedMessage);
				// If nothing else above, it is an ordinary message that the user wants to send to everybody
			} else {
				broadcast(receivedMessage);
				continue;
			}
		} while (true);
	}
	
	public void addNewMember(String message) {
		// Split up message to get client name to add to the connectMembers list
		String[] formattedMessage = message.split(" ");
		
		String name = formattedMessage[1];

		processNewMembership(name);

		// Let everyone know that a new user has connected to the chatroom
		replyMessage = "Server- " + formattedMessage[1] + " joined the chat";
		broadcast(replyMessage);
	}

	// Method to put the new user that connected to the chatroom in the connectedMembers list
	public void processNewMembership(String name) {
		Server.connectedMembers.put(name, this);
	}

	// Method that broadcast a message to all the connected users
	public void broadcast(String message) {
		// Loop through each member in the connectedMembers list
		for (Map.Entry<String, ClientHandler> entry : Server.connectedMembers.entrySet()) {
			// Send message back to all clients that are connected to the chat room
			serverEnd.writeStream(entry.getValue().socket, message);
		}
	}

	// Method that lets a user leave the chatroom and announces it to the rest of the users
	public void leaveChatRoom(String message) {
		String[] formattedMessage = message.split("-");

		replyMessage = "Server- " + formattedMessage[0] + " left the chat";
		Server.connectedMembers.remove(formattedMessage[0]);

		broadcast(replyMessage);
	}

	// Method that handles when a user wants to send a private messsage to another user
	public void sendPrivateMessage(String message) {
		// Splitting up the message to see which user the private message should go to
		String[] formattedMessage2 = message.split("-");

		String[] formatMessage = message.split(" ");

		String formatMessage2 = "";

		formatMessage2 += formattedMessage2[0] + "-" + " ";

		formatMessage[1] = formatMessage[1].substring(0, formatMessage[1].length() - 1);

		for (int i = 2; i < formatMessage.length; i++) {
			formatMessage2 += formatMessage[i] + " ";
		}
		System.out.println(formatMessage2);
		
		// Send message to the client that should receive the private message
		serverEnd.writeStream(socket, formatMessage2);
	}

	// Method that list the members that are connected to the chatroom
	public void listMembers(String message) {
		String listOfMembers = "The members that are connected to this chatroom are:\n";

		// Go through every member that is connected to the chat room and append each
		// name to the listOfMembers string
		for (Map.Entry<String, ClientHandler> entry : Server.connectedMembers.entrySet()) {
			listOfMembers += entry.getKey() + "\n";
		}

		// Remove the last line break when listing members
		listOfMembers = listOfMembers.substring(0, listOfMembers.length() - 1);

		// Send message with list of members back to the requesting client
		serverEnd.writeStream(socket, listOfMembers);
	}
}