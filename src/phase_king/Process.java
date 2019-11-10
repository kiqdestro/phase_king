package phase_king;

import java.util.Random;
import java.security.*;

// Class that create a process and define its properties

public class Process {
	
	int id; // process' id (0 - 4)
	boolean data; // process' initial value
	int valuePK; // random value used to define the phase king later
	
	Process(int id) throws NoSuchAlgorithmException {
		
		this.id = id;
		Random randomObj = new Random();
		this.data = randomObj.nextBoolean();
		this.valuePK = Math.abs(randomObj.nextInt() % 10);
		
	}
}