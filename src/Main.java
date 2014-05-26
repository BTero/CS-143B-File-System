import java.io.IOException;

public class Main {

	private static String studentName = "Brian Tero";
	private static String studentID = "90121402";
	private static String uciNetID = "BTero";

	private static int inputType = 0;				// 0 = console; 1 = file
	private static String inFile = "input.txt";

	public static void main(String[] args){
		//		Shell s = new Shell();
		//		s.run(inFile, inputType);
		
		// IOSystem test
//		IOSystem io = new IOSystem("test.txt");
//		char[] data = new char[64];
//		data = io.read_block(0);
//		for(int i = 0; i < 64; i++){
//			data[i] = '0';
//			System.out.print(data[i] + " ");
//		}
//		data[0] = '9';
//		data[10] = 'c';
//		io.write_block(0, data);
//		io.save("test.txt");
		
		// File System test
		// initialization test
		FileSystem fs = new FileSystem();
		
		// create test
		fs.create("x");
		fs.create("y");
		fs.save("test.txt");
		
		// delete test
		
		
		// open test
		
		
		// close test

	}
}