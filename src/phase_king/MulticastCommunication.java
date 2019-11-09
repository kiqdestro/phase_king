package phase_king;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.json.*;

public class MulticastCommunication {
	
	int socket; // socket defined in PhaseKing.java
	String groupAddress; // group address also defined in PhaseKing.java
	public JSONObject processesData = new JSONObject(); // information of all the processes will be written here
	public boolean tiebraker; // tiebreaker sent by the phase king
	public Integer phaseKing; // phase king defined by PhaseKing.java
	
	public boolean flag = true; // flag that keeps the listen thread running
	
	MulticastCommunication(String group, int socket){ // class constructor
		this.socket = socket;
		this.groupAddress = group;
	}
	
	public void talk(Integer pos, Boolean data, Integer valuePK, PublicKey publicKey, byte[] maj) throws IOException, SocketException { // null if not in use
		
		JSONArray info = new JSONArray();
		InetAddress group = InetAddress.getByName(this.groupAddress);
		MulticastSocket s = new MulticastSocket(this.socket);
		s.joinGroup(group);
		
		// if the process is sendind its information, the maj variable will be null. If the phase king is sending the tiebreaker, the other variables will be null
		
		if(maj != null) {
			
			info.put(0, "tiebraker");
			info.put(1, Base64.getEncoder().encodeToString(maj));
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
			
			if(publicKey != null) {
				
				info.put(4, Base64.getEncoder().encodeToString(publicKey.getEncoded()));
				
				
			}
		}
		
		byte [] m = info.toString().getBytes(); ; // convert convert json object to string than to byte array
		
		// sending datagram packet
	    
		DatagramPacket messageOut = new DatagramPacket(m, m.length, group, this.socket);
		s.send(messageOut);
		s.leaveGroup(group);
		s.close();
		
	}
	
	public void listen() throws IOException, SocketException { // method that contains the thread to capture packets
		
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
						
						setData(new JSONArray(new String(in.getData()))); // get the json array and send to setData method
					}
					
				} catch (SocketException e){System.out.println("Socket: " + e.getMessage());
				} catch (IOException e){System.out.println("IO: " + e.getMessage());
				} catch (InvalidKeyException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (NoSuchPaddingException e) {
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					e.printStackTrace();
				} catch (BadPaddingException e) {
					e.printStackTrace();
				} catch (InvalidKeySpecException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {if (s != null) s.close(); }
			}
		};
		
		thread.start();
	}
	
	public void setData(JSONArray info) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeySpecException, InterruptedException {
		
		if(info.getString(0).equals("processInfo")) { // catching info
			Integer id = info.getInt(1); // the id turns into a processesData index for this process
			info.remove(0); // removing "processInfo" from info JSONArray
			info.remove(0); // removing id from info JSONArray
			this.processesData.put(id.toString(), info); // put the information in processesData array
		}
		
		else if (info.getString(0).equals("tiebraker")){ // catching tiebraker
			
			
			
			byte[] output = Base64.getDecoder().decode(info.getString(1).getBytes()); // getting encrypted value as byte array
			
			while(this.phaseKing == null) { //prevent failure causing by unsyncing
				Thread.sleep(100);
			}

			byte[] publicKeyData = Base64.getDecoder().decode(processesData.getJSONArray(this.phaseKing.toString()).get(2).toString()); // getting phase king's public key as byte array
			X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyData);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PublicKey phaseKingPK = kf.generatePublic(spec); // converting from byte array to PublicKey Object
//			PublicKey phaseKingPK = PublicKey.class.cast(processesData.getJSONArray(this.phaseKing.toString()).get(2));
//			System.out.println(phaseKingPK);
			byte[] decryptedOutput = Cryptography.decryptData(output, phaseKingPK); // decrypting value
			String textFormat = new String(decryptedOutput);
			
			if(!textFormat.equals("true") && !textFormat.equals("false")) {
				System.out.println("The tie braker was not sent by the phase king");
				System.exit(0);
			}
			
			this.tiebraker = Boolean.valueOf(textFormat);
		}
	}
	
	public void stopListen() { // stop thread
		this.flag = false;
	}
	
	public void setPK(int pk) { // set phase king
		this.phaseKing = pk;
	}
}
