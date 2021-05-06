import java.net.InetAddress;

public class Member {
	private InetAddress address;
	private int port;
	
	public Member(InetAddress adress, int port) {
		this.address = adress;
		this.port = port;
	}
	
	public InetAddress getAddress() {
		return this.address;
	}
	
	public int getPort() {
		return this.port;
	}
}
