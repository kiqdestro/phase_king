package phase_king;

import java.net.InetAddress;

public class MulticastCommunication {
	
	int socket;
	String groupAddress;
	
	MulticastCommunication(String group, int socket){
		this.socket = socket;
		this.groupAddress = group;
	}
	
	public void talk() {
		
		InetAddress group = InetAddress.getByName(groupAddress);
	}
}
