import java.io.*;
import java.net.*;

public class EndPoint {
	public void writeStream(Socket socket, String message) {
		OutputStream os;
		try {
			os = socket.getOutputStream();
			DataOutputStream dout = new DataOutputStream(os);
			dout.writeUTF(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String readStream(Socket socket) {
		String message = null;
		InputStream is;
		try {
			is = socket.getInputStream();
			DataInputStream din = new DataInputStream(is);
			message = din.readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (message);
	}
}
