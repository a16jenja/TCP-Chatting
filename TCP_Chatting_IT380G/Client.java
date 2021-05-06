import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Thread implements ActionListener {
	long startTime;
	Socket socket;
	ChatGUI chatGUI;
	String clientName;
	EndPoint clientEnd;
	
	public Client(String clientName) {
		// Create a new end point instance
		clientEnd = new EndPoint();
		this.clientName = clientName;
		// Start up GUI in a new thread
		chatGUI = new ChatGUI(this, clientName);
	}

	public void setServerParameters(String serverAddressString, int serverPortNumber) {
		
		InetAddress address = null;
		
		// Build a socket to a server destination address and program
		try {
			address = InetAddress.getByName(serverAddressString);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			socket = new Socket(address, serverPortNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	// Connects user to server by sending a specific /handshake message with the
	// users name
	public void connectToServer() {
		// add sender name to message
		String message = "/handshake " + clientName + " ";;
			
		// Write the message to the stream
		clientEnd.writeStream(socket, message);
	}
	
	// Method used to calculate the Round trip time for packets
	public void calculateRTT() {
		// Start the clock for calculating Round trip time for packets
		startTime = System.currentTimeMillis();
		
		// Creating a message so the server knows that this message is for testing purposes
		String message1 = clientName + "-" + "/calculateRTT 1";
			
		clientEnd.writeStream(socket, message1);
		
		// Creating a message so the server knows that this message is for testing purposes
		String message2 = clientName + "-" + "/calculateRTT 2";
		
		// Write message to stream
		clientEnd.writeStream(socket, message2);
	}

	public void run() {
		// Do this method once when starting the client thread
		calculateRTT();
		
		do {
			// Receive a reply message from server
			String replyMessage = clientEnd.readStream(socket);
			
			System.out.println("Client- received: " + replyMessage);
			
			// End the clock for calculating Round trip time for packets
			long elapsedTime = System.currentTimeMillis() - startTime;
			System.out.println(clientName + "'s RTT: " + elapsedTime);
			
			// Display message on GUI
			chatGUI.displayMessage(replyMessage);
		} while(true);
	}

	public void actionPerformed(ActionEvent e) {
		// There is only one event coming out from the GUI and that’s
		// the carriage return in the text input field, which indicates the
		// message/command in the chat input field to be sent to the server

		// get the text typed in input field, using ChatGUI utility method
		String message = chatGUI.getInput();

		// add sender name to message
		message = clientName + "-" + message;
		
		// Send a message to server
		clientEnd.writeStream(socket, message);
		
		// clear the GUI input field, using a utility function of ChatGUI
		chatGUI.clearInput();
	}
}