package phase_king;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class MulticastCommunication {
	
	int socket;
	String groupAddress;
	public JSONObject processesData = new JSONObject();
	public boolean tiebraker;
	
	public boolean flag = true;
	
	MulticastCommunication(String group, int socket){
		this.socket = socket;
		this.groupAddress = group;
	}
	
	public void talk(Integer pos, Boolean data, Integer valuePK, Boolean maj) throws IOException, SocketException { // null if not in use
		
		JSONArray info = new JSONArray();
		InetAddress group = InetAddress.getByName(this.groupAddress);
		MulticastSocket s = new MulticastSocket(this.socket);
		s.joinGroup(group);
		
		if(maj != null) {
			info.put(0, "tiebraker");
			info.put(1, maj);
		}
		
		else {
			
			info.put(0, "processInfo");
			info.put(1, pos);
			
			if(data != null) {
				info.put(2, data);
			}
			
			if(valuePK != null) {
				info.put(3, valuePK);
			}
		}
		
		String messageS = info.toString();
		
		byte [] m = messageS.getBytes();
	    
		DatagramPacket messageOut = new DatagramPacket(m, m.length, group, this.socket);
		s.send(messageOut);
		s.leaveGroup(group);
		s.close();
		
	}
	
	public void listen() throws IOException, SocketException {
		
		String groupAddress = this.groupAddress;
		int socket = this.socket;
		
		Thread thread = new Thread() {
			
			public void run() {
				
				MulticastSocket s = null;
				
				try {
					InetAddress group = InetAddress.getByName(groupAddress);
					s = new MulticastSocket(socket);
					s.joinGroup(group);
					
					while (flag) {
						byte[] buffer = new byte[1000];
						DatagramPacket in = new DatagramPacket (buffer, buffer.length);
						s.receive(in);
						JSONArray info = new JSONArray(new String(in.getData()));
						
						setData(info);
					}
					
				} catch (SocketException e){System.out.println("Socket: " + e.getMessage());
				} catch (IOException e){System.out.println("IO: " + e.getMessage());
				} finally {if (s != null) s.close(); }
			}
		};
		
		thread.start();
	}
	
	public void setData(JSONArray info) {
		
		if(info.getString(0).equals("processInfo")) {
			Integer id = info.getInt(1);
			info.remove(0);
			info.remove(0);
			this.processesData.put(id.toString(), info);
		}
		
		else {
			this.tiebraker = info.getBoolean(1);
		}
	}
	
	public String getData() throws InterruptedException { // maybe unnecessary
		
//		Thread.sleep(1000);
//		
//		JSONArray hue = this.processD.getJSONArray("2");
//		
//		System.out.println(hue.get(0).toString());
		
//		System.out.println(hue.toString());
		return this.processesData.toString();
	}
	
	public void stopListen() {
		this.flag = false;
	}
	
	public void resetData() {
		for (int i = 0; i < 5; i++) {
//			this.processesData[i] = null;
		}
	}
}
