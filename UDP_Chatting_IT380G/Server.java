import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;

public class Server extends Thread {
	EndPoint serverEnd;
	String replyMessage;
	TreeMap<String, Member> connectedMembers = new TreeMap<String, Member>();

	public Server(int serverPort) {
		// Create an endPoint for this program identified by serverport
		serverEnd = new EndPoint(serverPort);
	}

	public void setReplyMessage(String replyMessage) {
		this.replyMessage = replyMessage;
	}

	public void run() {

		do {
			// Receive a packet from client
			DatagramPacket receivedPacket = serverEnd.receivePacket();

			// Get the message within packet
			String receivedMessage = serverEnd.unmarshall(receivedPacket);

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
				DatagramPacket replyPacket = serverEnd.makeNewPacket(receivedMessage,
						connectedMembers.get(formattedMessage[0]).getAddress(), connectedMembers.get(formattedMessage[0]).getPort());
				serverEnd.sendPacket(replyPacket);
				continue;
			// If nothing else above, it is an ordinary message that the user wants to send to everybody
			} else {
				broadcast(receivedMessage, connectedMembers.lastKey());
				continue;
			}
		} while (true);
	}

	public void addNewMember(String message) {
		// Split up message to get client name, address and port number to add to the connectedMembers list
		String[] formattedMessage = message.split(" ");
		try {
			String name = formattedMessage[1];

			String[] addressSplit = formattedMessage[2].split("/");
			InetAddress senderAddress = InetAddress.getByName(addressSplit[1]);
			int senderPort = Integer.parseInt(formattedMessage[3]);

			processNewMembership(name, senderAddress, senderPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		// Let everyone know that a new user has connected to the chatroom
		replyMessage = "Server- " + formattedMessage[1] + " joined the chat";
		broadcast(replyMessage, connectedMembers.lastKey());
	}

	// Method to put the new user that connected to the chatroom in the connectedMembers list
	public void processNewMembership(String name, InetAddress senderAdress, int senderPort) {
		connectedMembers.put(name, new Member(senderAdress, senderPort));
	}

	// Method that broadcast a message to all the connected users
	public void broadcast(String message, String name) {
		// Loop through each member in the connectedMembers list
		for (Map.Entry<String, Member> entry : connectedMembers.entrySet()) {
			Member client = entry.getValue();
			
			// Send message back to all clients that are connected to the chat room
			DatagramPacket replyPacket = serverEnd.makeNewPacket(message, client.getAddress(), client.getPort());
			serverEnd.sendPacket(replyPacket);
		}
	}

	// Method that lets a user leave the chatroom and announces it to the rest of the users
	public void leaveChatRoom(String message) {
		String[] formattedMessage = message.split("-");

		replyMessage = "Server- " + formattedMessage[0] + " left the chat";
		connectedMembers.remove(formattedMessage[0]);

		broadcast(replyMessage, connectedMembers.lastKey());
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
		DatagramPacket replyPacket = serverEnd.makeNewPacket(formatMessage2,
				connectedMembers.get(formatMessage[1]).getAddress(), connectedMembers.get(formatMessage[1]).getPort());
		serverEnd.sendPacket(replyPacket);
	}

	// Method that list the members that are connected to the chatroom
	public void listMembers(String message) {
		String listOfMembers = "The members that are connected to this chatroom are:\n";

		// Go through every member that is connected to the chat room and append each
		// name to the listOfMembers string
		for (Map.Entry<String, Member> entry : connectedMembers.entrySet()) {
			listOfMembers += entry.getKey() + "\n";
		}

		// Remove the last line break when listing members
		listOfMembers = listOfMembers.substring(0, listOfMembers.length() - 1);

		// Send message with list of members back to the requesting client
		DatagramPacket replyPacket = serverEnd.makeNewPacket(listOfMembers, connectedMembers.get(message).getAddress(),
				connectedMembers.get(message).getPort());
		serverEnd.sendPacket(replyPacket);
	}
}