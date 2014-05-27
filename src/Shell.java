import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;


public class Shell {

	private static String studentName = "Brian Tero";
	private static String studentID = "90121402";
	private static String uciNetID = "BTero";
	
	public static BufferedReader br;
	private static PrintStream fileData;
	private static File outFile;
	
	private static FileSystem fs;

	public Shell(){
		
	}
	
	public void run(String inFile, int inputType){			// inputType;		// 0 = console; 1 = file
		if(inputType == 1){
			parseFile(inFile);
		}else{
			// parse from console
			// For testing purposes. To be able to test individual functions for correctness
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	        System.out.print("Enter Commands");
	        try {
	        	String line = br.readLine();
	        	String[] s = line.split(" ");
				if(s[0].equalsIgnoreCase("in")){
					if(s.length == 2){
						// restore
						init(s[1]);
					}else{
						// initialize
						init();
					}
				}else if(s[0].equalsIgnoreCase("cr")){
					if(s.length == 2){
						create(s[1]);
					}else{
						fileData.println("File creation resulted in error.");
					}
				}else if(s[0].equalsIgnoreCase("de")){
					if(s.length == 2){
						delete(s[1]);
					}else{
						fileData.println("File deletion resulted in error.");
					}
				}else if(s[0].equalsIgnoreCase("op")){
					if(s.length == 2){
						open(s[1]);
					}else{
						fileData.println("File could not be opened.");
					}
				}else if(s[0].equalsIgnoreCase("cl")){
					if(s.length == 2){
						close(s[1]);
					}else{
						fileData.println("File could not be closed.");
					}
				}else if(s[0].equalsIgnoreCase("rd")){
					if(s.length == 3){
						read(Integer.parseInt(s[1]), Integer.parseInt(s[2]));
					}else{
						fileData.println("File read resulted in error.");
					}
				}else if(s[0].equalsIgnoreCase("wr")){
					if(s.length == 4){
						write(Integer.parseInt(s[1]), s[2], Integer.parseInt(s[3]));
					}else{
						fileData.println("File write resulted in error.");
					}
				}else if(s[0].equalsIgnoreCase("sk")){
					if(s.length == 3){
						seek(Integer.parseInt(s[1]), Integer. parseInt(s[2]));
					}else{
						fileData.println("File seek resulted in error.");
					}
				}else if(s[0].equalsIgnoreCase("dr")){
					directory();
				}else if(s[0].equalsIgnoreCase("sv")){
					if(s.length == 2){
						save(s[1]);
					}else{
						fileData.println("File save resulted in error.");
					}
				}else if(s[0].equals("exit")){
					br.close();
					System.exit(0);
				}else{
					fileData.println("Incorrect Command. Enter valid Command.");
				}
				
			} catch (IOException e) {
				e.printStackTrace();
	            System.err.println("Invalid Format!");
			}
		}
	}
	
	public void parseFile(String inFile){
		try{
			br = new BufferedReader(new FileReader(inFile));
//			outFile = new FileOutputStream(studentID + ".txt");
			outFile = new File(studentID + ".txt");
//			outFile = new File("D:\\" + studentID + ".txt");
//			file = new File("F:\\" + studentID + ".txt");
			fileData = new PrintStream(outFile);
			
			String line = br.readLine();
			while(line != null){
				String[] s = line.split(" ");
				if(s[0].equalsIgnoreCase("in")){
					if(s.length == 2){
						// restore
						init(s[1]);
					}else{
						// initialize
						init();
					}
				}else if(s[0].equalsIgnoreCase("cr")){
					if(s.length == 2){
						System.out.println(s[1]);
						create(s[1]);
					}else{
						fileData.println("File creation resulted in error.");
					}
				}else if(s[0].equalsIgnoreCase("de")){
					if(s.length == 2){
						delete(s[1]);
					}else{
						fileData.println("File deletion resulted in error.");
					}
				}else if(s[0].equalsIgnoreCase("op")){
					if(s.length == 2){
						open(s[1]);
					}else{
						fileData.println("File could not be opened.");
					}
				}else if(s[0].equalsIgnoreCase("cl")){
					if(s.length == 2){
						close(s[1]);
					}else{
						fileData.println("File could not be closed.");
					}
				}else if(s[0].equalsIgnoreCase("rd")){
					if(s.length == 3){
						read(Integer.parseInt(s[1]), Integer.parseInt(s[2]));
					}else{
						fileData.println("File read resulted in error.");
					}
				}else if(s[0].equalsIgnoreCase("wr")){
					if(s.length == 4){
						write(Integer.parseInt(s[1]), s[2], Integer.parseInt(s[3]));
					}else{
						fileData.println("File write resulted in error.");
					}
				}else if(s[0].equalsIgnoreCase("sk")){
					if(s.length == 3){
						seek(Integer.parseInt(s[1]), Integer. parseInt(s[2]));
					}else{
						fileData.println("File seek resulted in error.");
					}
				}else if(s[0].equalsIgnoreCase("dr")){
					directory();
				}else if(s[0].equalsIgnoreCase("sv")){
					if(s.length == 2){
						save(s[1]);
					}else{
						fileData.println("File save resulted in error.");
					}
				}else{
					fileData.println("Incorrect Command. Enter valid Command.");
				}
				line = br.readLine();
			}
			
			br.close();
		}catch(IOException e){
			e.printStackTrace();
			System.exit(-2);
		}
	}

	public void init(String restoreFile){
		fs = new FileSystem(restoreFile);
		if(fs.restored() == 1){
			fileData.println("Disk restored.");
		}else{
			fileData.println("Disk restoration failed.");
		}
	}
	
	public void init(){
		fs = new FileSystem();
		if(fs.initialized() == 1){
			fileData.println("Disk initialized.");
		}else{
			fileData.println("Disk initialization failed");
		}
	}

	public void create(String name){
		fileData.println(fs.create(name));
	}

	public void delete(String name){
		fileData.println(fs.delete(name));
	}

	public void open(String name){
		fileData.println(fs.open(name));
	}

	public void close(String index){
		fileData.println(fs.close(Integer.parseInt(index)));
	}

	public void read(int index, int count){
		fileData.println(fs.read(index, count));
	}

	public void write(int index, String c, int count){
		// convert string to byte version before passing to manager
		fileData.println(fs.write(index, c.charAt(0), count));
	}

	public void seek(int index, int pos){
		fileData.println(fs.seek(index, pos));
	}

	public void directory(){
		fileData.println(fs.directory());
	}

	public void save(String fileName){
		fileData.println(fs.save(fileName));
	}
}
