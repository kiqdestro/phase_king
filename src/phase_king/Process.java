package phase_king;

import java.util.Random;

public class Process {
	
	int id;
	boolean data;
	int valuePK;
	
	Process(int id) {
		
		this.id = id;
		Random randomObj = new Random();
		this.data = randomObj.nextBoolean();
		this.valuePK = Math.abs(randomObj.nextInt() % 10);
	}
}
