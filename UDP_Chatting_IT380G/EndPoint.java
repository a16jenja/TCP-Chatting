import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class EndPoint {
	InetAddress address; // this endpoint address
	int portNumber; // the program port number used through this endpoint
	DatagramSocket socket; // the communication socket of this endpoint

	public EndPoint(int m_portNumber) {
		portNumber = m_portNumber;
		try { // only this exception is handled for illustration!
			address = InetAddress.getLocalHost();
			socket = new DatagramSocket(portNumber);
		} catch (Exception e) {
			System.err.println("Error creating endPoint!");
		}
	}

	public DatagramPacket makeNewPacket(String message, InetAddress destinationAddress, int portNumber) {
		DatagramPacket packet = null; // to represent packets
		byte[] buffer; // to represents payload messages within packets
		// Marshall message
		buffer = message.getBytes();
		// Put message in a new packet
		packet = new DatagramPacket(buffer, buffer.length, destinationAddress, portNumber);
		return (packet);
	}

	public void sendPacket(DatagramPacket packet) {
		// Send message via socket
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DatagramPacket receivePacket() {
		// Create a new DatgramPacket object to buffer the received payload
		//65535
		byte[] buffer = new byte[255];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		// Wait to receive request packet
		try {
			socket.receive(packet);
		} catch (IOException e) {
			System.err.println("Error receiving packet from the server!");
		}
		return (packet);
	}

	public String unmarshall(DatagramPacket packet) {
		// Converting the packet to a string for easy readability
		String payloadString = new String(packet.getData(), 0, packet.getLength());
		return payloadString;
	}
}
