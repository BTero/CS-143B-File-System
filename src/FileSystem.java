import java.util.ArrayList;


public class FileSystem {

	private static String studentName = "Brian Tero";
	private static String studentID = "90121402";
	private static String uciNetID = "BTero";
	
	// first 64 bytes are the data blocks being read from and written to; the rest are information blocks of the buffer
	// is_open, block_no, desc_index, file_size, cur_pos
	private static final int bufferSize = 84;
	private static final int maxSize = 4;
	
	private static IOSystem io;
	private static char[][] desc_table;
	private static char[] directory;
	private static char[][] oft;

	public FileSystem(){
		io = new IOSystem();
		desc_table = new char[6][64];
		oft = new char[maxSize][bufferSize];
		directory = new char[192];

		loadDescTable();
		
		for(int i = 0; i < maxSize; i++){
			for(int j = 0; j < bufferSize; j++){
				oft[i][j] = 0;
			}
		}
		// 64 beginning of is_open; 68 beginning of block_no; 72 beginning of desc_index; 76 beginning of file_size; 80 beginning of cur_pos
		oft[0][67] = 1;		// directory buffer is always open.
	}

	public FileSystem(String restoreFile){
		io = new IOSystem(restoreFile);
		desc_table = new char[6][64];
		oft = new char[maxSize][bufferSize];
		directory = new char[192];

		// restore information to data structures
		loadDescTable();
		loadDir();
	}

	public int bti(char[] data){
		int temp = 0;
		temp += data[0] * 1000;
		temp += data[1] * 100;
		temp += data[2] * 10;
		temp += data[3] * 1;
		return temp;
	}
	
	public void itb(int block, int pos, int val){
		// converts int to char array and places it in the corresponding location given
	}
	
	public void loadDir(){
		char[] size = new char[4];
		
		for(int i = 0; i < 4; i++){
			size[i] = desc_table[1][i];
		}
		
		int dirSize = bti(size);
		if(dirSize > 128){
			char[] index1 = new char[4];
			char[] index2 = new char[4];
			char[] index3 = new char[4];
						
			for(int i = 0; i < 4; i++){
				index1[i] = desc_table[1][4 + i];
				index2[i] = desc_table[1][8 + i];
				index3[i] = desc_table[1][12 + i];
			}
			
			char[] block1 = io.read_block(bti(index1));
			char[] block2 = io.read_block(bti(index2));
			char[] block3 = io.read_block(bti(index3));
			
			for(int i = 0; i < 64; i++){
				directory[i] = block1[i];
				directory[i + 64] = block2[i];
				directory[i + 128] = block3[i];
			}
		}else if(dirSize > 64){
			char[] index1 = new char[4];
			char[] index2 = new char[4];
			
			for(int i = 0; i < 4; i++){
				index1[i] = desc_table[1][4 + i];
				index2[i] = desc_table[1][8 + i];
			}
			
			char[] block1 = io.read_block(bti(index1));
			char[] block2 = io.read_block(bti(index2));
			
			for(int i = 0; i < 64; i++){
				directory[i] = block1[i];
				directory[i + 64] = block2[i];
			}
		}else{
			char[] index1 = new char[4];
			
			for(int i = 0; i < 4; i++){
				index1[i] = desc_table[1][4 + i];
			}
			
			char[] block1 = io.read_block(bti(index1));
			
			for(int i = 0; i < 64; i++){
				directory[i] = block1[i];
			}
		}
	}

	public void loadDescTable(){
		// read data blocks into desc_table
		for(int i = 0; i < 7; i++){
			char[] temp = io.read_block(i);
			for(int j = 0; j < 64; j++){
				desc_table[i][j] = temp[j];
			}
		}
	}

	public int searchDir(String name){
		int index = -1;
		char[] temp = new char[4];
		for(int i = 0; i < directory.length; i++){
			if(i % 8 == 0){
				temp[i] = directory[i];
				temp[i + 1] = directory[i + 1];
				temp[i + 2] = directory[i + 2];
				temp[i + 3] = directory[i + 3];
				
			}
			
			
		}
		
		return index;
	}

	public String create(String name){
		String status = "";
		// search directory if a file of the name already exists
		int desc_index = searchDir(name);
		if(desc_index == -1){			// file not found

		}else{	// file exists
			status = "File already exists.";
		}
		return status;
	}

	public void delete(String name){

	}

	public void open(String name){

	}

	public void close(int index){

	}

	public void read(int index, int count){

	}

	public void write(int index, char c, int count){

	}

	public void seek(int index, int pos){

	}

	public void directory(){

	}

	public void save(String fileName){

	}
}
