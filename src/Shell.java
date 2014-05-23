import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;


public class Shell {

	private static String studentName = "Brian Tero";
	private static String studentID = "90121402";
	private static String uciNetID = "BTero";
	
	public static BufferedReader br;
//	public static FileOutputStream outFile;
	private static PrintStream fileData;
	private static File outFile;
	
	private static Manager m;

	public Shell(){
		m = new Manager();
	}
	
	public void run(String inFile, int inputType){			// inputType;		// 0 = console; 1 = file
		if(inputType == 1){
			parseFile(inFile);
		}else{
			// parse from console
			// For testing purposes. To be able to test individual functions for correctness
		}
	}
	
	public void parseFile(String inFile){
		try{
			br = new BufferedReader(new FileReader(inFile));
//			outFile = new FileOutputStream(studentID + ".txt");
			outFile = new File("D:\\" + studentID + ".txt");
//			file = new File("F:\\" + studentID + ".txt");
			fileData = new PrintStream(outFile);

		}catch(IOException e){
			e.printStackTrace();
			System.exit(-2);
		}
	}

	public void init(){

	}

	public void create(){

	}

	public void delete(){

	}

	public void open(){

	}

	public void close(){

	}

	public void read(){

	}

	public void write(){

	}

	public void seek(){

	}

	public void directory(){

	}

	public void save(){

	}
}
