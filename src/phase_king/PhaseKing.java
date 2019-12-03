package phase_king;

import java.io.IOException;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.json.*;

public class PhaseKing {
	
	public static void main(String[] args) throws SocketException, IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
//		----------------------------------------------------------------------------
		
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Insert process ID: ");
		int id = keyboard.nextInt(); // get PID from keyboard input
		Integer phaseKing = null; // result from phase king calculus
		
		
		keyboard.close();
		
		Process process = new Process(id); // create process
		
		System.out.println("Initial value: " + process.data);
		
		MulticastCommunication comm = new MulticastCommunication("228.1.2.3", 6789); // create instance of the multicast communication class
		
		comm.listen(); // start the listen thread to communication
		
		for (int phase = 0; phase < 2; phase++) { // phase counter
			
			// round 1 -------------------------------------------------------------------------------------------
			
			comm.talk(process.id, process.data, process.valuePK, null); // send process information for the first time
			
			boolean verif = true;
			String val;
			
			while(verif) {
				
				verif = false;
				
				for(Integer i = 0; i < 5; i++) {
					
					try {
						val = comm.processesData.getJSONArray(i.toString()).get(0).toString();
					}
					
					catch (JSONException e) {
						verif = true;
						Thread.sleep(1000);
						comm.talk(process.id, process.data, process.valuePK, null); // send information again
						break;
					}
				}
			}
			
			int trues = 0;
			int falses = 0;
			
			// counting all the values
			
			for (Integer i = 0; i < 5; i++) {
				val = comm.processesData.getJSONArray(i.toString()).get(0).toString();
				
				if (val == "true") {
					trues = trues + 1;
				}
				
				else if (val == "false"){
					falses = falses + 1;
				}
				
				else if (val != "false" && val != "true") {
					System.out.println("couldn't get all values");
					comm.stopListen();
					System.exit(0);
				}
			}
			
			int mult = trues; // the default value is true
			Boolean maj = true;
			
			if (falses > trues) {
				maj = false;
				mult = falses;
			}
			
			// round 2 -------------------------------------------------------------------------------------------
			
			// define who is the phase leader		
			
			phaseKing = 0;
			
			for (Integer i = 0; i < 5; i++) {
				phaseKing = phaseKing + Integer.parseInt(comm.processesData.getJSONArray(i.toString()).get(1).toString());
			}
			
			// the result is different depending on the phase
			
			if (phase == 0) {
				phaseKing = phaseKing % 5;
				comm.phaseKing = phaseKing;
			}
			
			else {
				phaseKing = (phaseKing + 1) % 5;
				comm.phaseKing = phaseKing;
			}
			
			Thread.sleep(1000); // All the processes wait to avoid synchrony problems
			
			
			if (process.id == phaseKing) { // the phase king needs to send the tiebreaker to everyone				
				
				comm.talk(null, null, null, maj);
				
			}
			
			if (mult >= 3) {
				
				process.data = maj;
			}
			
			else {
				while(comm.tiebraker == null) {
					Thread.sleep(200);
				}
				process.data = comm.tiebraker;
			}
		}
		
		System.out.println("ID: " + process.id + ", value: " + process.data); // show the final value and process id
		comm.stopListen(); // stop the listen thread
		System.exit(0);
	}
}
