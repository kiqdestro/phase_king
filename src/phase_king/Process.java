package phase_king;

import java.util.Random;

public class Process {
	
	int id;
	boolean value;
	
	Process(int id) {
		
		this.id = id;
		Random randomObj = new Random();
		this.value = randomObj.nextBoolean();
	}
}
