import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Timer;

public class Client extends Thread implements ActionListener {
	long startTime;
	EndPoint clientEnd;
	ChatGUI chatGUI;
	InetAddress serverAddress = null;
	int serverPortNumber;
	String clientName;

	public Client(int clientPortNumber, String clientName) {
		// Create an endPoint on this computer to this
		// program identified by the provided port
		clientEnd = new EndPoint(clientPortNumber);
		this.clientName = clientName;
		// Start up GUI (runs in its own thread)
		chatGUI = new ChatGUI(this, clientName);
	}

	// Client parameters include server references for processing transmissions
	public void setServerParameters(String serverAddressString, int serverPortNumber) {
		try {
			serverAddress = InetAddress.getByName(serverAddressString);
		} catch (UnknownHostException e) {
			System.err.println("Error setting the server parameters, invalid server address!");
		}
		this.serverPortNumber = serverPortNumber;
	}

	// Connects user to server by sending a specific /handshake message with the
	// users name, address and port number
	public void connectToServer() {
		DatagramPacket messagePacket;

		String message = "/handshake " + clientName + " " + clientEnd.address + " " + clientEnd.portNumber + " ";

		messagePacket = clientEnd.makeNewPacket(message, serverAddress, serverPortNumber);

		clientEnd.sendPacket(messagePacket);
	}

	// Method used to calculate the Round trip time for packets
	public void calculateRTT() {
		// Start the clock for calculating Round trip time for packets
		startTime = System.currentTimeMillis();

		DatagramPacket messagePacket;

		// Creating a message so the server knows that this message is for testing purposes
		String message1 = clientName + "-" + "/calculateRTT 1";

		messagePacket = clientEnd.makeNewPacket(message1, serverAddress, serverPortNumber);

		clientEnd.sendPacket(messagePacket);

		// Creating a message so the server knows that this message is for testing purposes
		String message2 = clientName + "-" + "/calculateRTT 2";

		messagePacket = clientEnd.makeNewPacket(message2, serverAddress, serverPortNumber);

		clientEnd.sendPacket(messagePacket);
	}

	public void run() {
		// Do this method once when starting the client thread
		calculateRTT();

		do {
			// Receive a reply from server
			DatagramPacket replyPacket = clientEnd.receivePacket();
			// Get the message within packet
			String replyMessage = clientEnd.unmarshall(replyPacket);
			System.out.println("Client " + clientName + " received the message: " + replyMessage);
			
			// End the clock for calculating Round trip time for packets
			long elapsedTime = System.currentTimeMillis() - startTime;
			System.out.println(clientName + "'s RTT: " + elapsedTime);
			
			// Display message on GUI
			chatGUI.displayMessage(replyMessage);
		} while (true);
	}

	public void actionPerformed(ActionEvent e) {
		// There is only one event coming out from the GUI and that’s
		// the carriage return in the text input field, which indicates the
		// message/command in the chat input field to be sent to the server

		DatagramPacket messagePacket;

		// get the text typed in input field, using ChatGUI utility method
		String message = chatGUI.getInput();

		// add sender name to message
		message = clientName + "-" + message;
		// create packet to carry the message, assuming any message fits
		// a packet size
		messagePacket = clientEnd.makeNewPacket(message, serverAddress, serverPortNumber);
		// send the message
		clientEnd.sendPacket(messagePacket);

		// clear the GUI input field, using a utility function of ChatGUI
		chatGUI.clearInput();
	}
}