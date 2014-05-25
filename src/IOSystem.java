import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;


public class IOSystem {

	private static String studentName = "Brian Tero";
	private static String studentID = "90121402";
	private static String uciNetID = "BTero";

	private static File outFile;
	private static PrintStream fileData;

	private static final int L = 64;
	private static final int B = 64;
	private static char[][] ldisk;

	public IOSystem(){
		this.ldisk = new char[L][B];

		// initialize disk to all zeros
		reset();

		// reserve blocks used for file descriptors
		for(int i = 0; i < 8; i++){
			ldisk[0][i] = '1';
		}
	}

	public IOSystem(String inFile){
		this.ldisk = new char[L][B];

		// initialize disk to all zeros
		reset();

		// restore previous IOSystem
		try {
			BufferedReader br = new BufferedReader(new FileReader(inFile));

			String line = br.readLine();
			for(int i = 0; i < L; i++){
				// form string back to original format based on ldisk
				String[] s = line.split(" ");
				
				for(int k = 0; k < s.length; k++){
					ldisk[i][k] = s[k].charAt(0);
				}
//				String fullLine = "";
//				for(int k = 0; k < s.length; k++){
//					fullLine += s[k];
//				}
//
//				// restore line back into ldisk
//
//				for(int j = 0; j < B; j++){
//					ldisk[i][j] = fullLine.getBytes()[j];
//				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public char[] read_block(int i){
		return ldisk[i];
	}

	public void write_block(int i, char[] data){
		for(int j = 0; j < B; j++){
			ldisk[i][j] = data[j];
		}
	}

	public void reset(){
		for(int i = 0; i < L; i++){
			for(int j = 0; j < B; j++){
				ldisk[i][j]	= '0';
			}
		}
	}

	public void save(String out){
		try{
			outFile = new File(out);
			fileData = new PrintStream(outFile);
			for(int i = 0; i < L; i++){
				for(int j = 0; j < B; j++){
					fileData.printf("%s ", ldisk[i][j]);
				}
				fileData.print("\n");
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
}


