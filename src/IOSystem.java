
public class IOSystem {
	
	private static String studentName = "Brian Tero";
	private static String studentID = "90121402";
	private static String uciNetID = "BTero";
	
	
	private static int L;
	private static int B;
	private static String fileName;
	private static int[][] ldisk;
	
	
	public IOSystem(int L, int B, String fileName){
		this.L = L;
		this.B = B;
		this.fileName = fileName;
		
		
		// initialize ldisk
		this.ldisk = new int[L][B];
	}
	
	public void read_block(int i){
		
	}
	
	public void write_block(int i ){
		
	}
	
	public void reset(){
		
	}
	
	public void save(){
		
	}
	
	public void restore(){
		
	}
}
