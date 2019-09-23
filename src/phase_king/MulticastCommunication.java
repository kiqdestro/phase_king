package phase_king;

import java.io.*;
import java.net.*;

public class MulticastCommunication {
	
	int socket;
	String groupAddress;
//	public boolean processesData[];
	
	MulticastCommunication(String group, int socket){
		this.socket = socket;
		this.groupAddress = group;
	}
	
	public void talk(String message) throws IOException, SocketException {
		
		InetAddress group = InetAddress.getByName(this.groupAddress);
		MulticastSocket s = new MulticastSocket(this.socket);
		s.joinGroup(group);
		byte [] m = message.getBytes();
		DatagramPacket messageOut = new DatagramPacket(m, m.length, group, this.socket);
		s.send(messageOut);
		s.leaveGroup(group);
		s.close();
		
	}
	
	public void listen() throws IOException, SocketException {
		
		Thread thread = new Thread() {
			private boolean flag = true;
			String groupAddress = this.groupAddress;
			int socket = this.socket;
			
			public void run() {
				
				MulticastSocket s = null;
				boolean flag = true;
				
				try {
					InetAddress group = InetAddress.getByName(groupAddress);
					s = new MulticastSocket(socket);
					s.joinGroup(group);
					
					while (flag) {
						byte[] buffer = new byte[500];
						DatagramPacket in = new DatagramPacket (buffer, buffer.length);
						s.receive(in);
						System.out.println("Received: " + new String(in.getData()));
					}
					
				} catch (SocketException e){System.out.println("Socket: " + e.getMessage());
				} catch (IOException e){System.out.println("IO: " + e.getMessage());
				} finally {if (s != null) s.close(); }
			}
			
			public void stopRunning() {
				flag = false;
			}
		};
	}
}
