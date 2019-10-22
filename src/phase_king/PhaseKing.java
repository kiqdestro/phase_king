package phase_king;

import java.io.IOException;
import java.net.SocketException;
import java.util.Scanner;
import org.json.*;

public class PhaseKing {
	
	public static void main(String[] args) throws SocketException, IOException, InterruptedException {
		
//		----------------------------------------------------------------------------
		
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Insert process ID: ");
		int id = keyboard.nextInt();
		
		keyboard.close();
		
		Process process = new Process(id);
		MulticastCommunication comm = new MulticastCommunication("228.1.2.3", 6789);
		
		comm.listen();
		
		for (int phase = 0; phase < 2; phase++) { // phases
			
			// round 1 -------------------------------------------------------------------------------------------
			
			comm.talk(process.id, process.data, process.valuePK, null);
			
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
						comm.talk(process.id, process.data, process.valuePK, null);
						break;						
					}
				}
			}
			
			int trues = 0;
			int falses = 0;
			
			for (Integer i = 0; i < 5; i++) {
				val = comm.processesData.getJSONArray(i.toString()).get(0).toString();
				
				if (val == "true") {
					trues = trues + 1;
				}
				
				else {
					falses = falses + 1;
				}
			}
			
			int mult = trues;			
			Boolean maj = true;
			
			if (falses > trues) {
				maj = false;
				mult = falses;
			}
			
			// round 2 -------------------------------------------------------------------------------------------
			
			// define who is the phase leader
			
			int phaseKing = 0;
			
			for (Integer i = 0; i < 5; i++) {
				phaseKing = phaseKing + Integer.parseInt(comm.processesData.getJSONArray(i.toString()).get(1).toString());
			}
			
			phaseKing = phaseKing % 5;
			
			if (process.id == phaseKing) {
				
				comm.talk(null, null, null, maj);
				
			}
			
			if (mult >= 3) {
				process.data = maj;
			}
			
			else {
				process.data = comm.tiebraker;
			}
			
			if(phase == 1) {
				System.out.println("ID: " + process.id + ", value: " + process.data);
				comm.stopListen();
			}
		}
	}
}
