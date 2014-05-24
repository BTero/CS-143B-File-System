
public class FileSystem {

	private static String studentName = "Brian Tero";
	private static String studentID = "90121402";
	private static String uciNetID = "BTero";


	private class OFT{

		private static final int maxSize = 4;
		
		// first 64 bytes are the data blocks being read from and written to; the rest are information blocks of the buffer
		private static final int bufferSize = 84;		// is_open, block_no, desc_index, file_size, cur_pos
		private byte[][] oft;
		
		public OFT(){
			this.oft = new byte[maxSize][bufferSize];
			
			for(int i = 0; i < maxSize; i++){
				for(int j = 0; j < bufferSize; j++){
					oft[i][j] = 0;
				}
			}
			
			oft[0][67] = 1;		// directory buffer is always open.
		}


	}
	
	private static IOSystem io;
	private static byte[][] desc_table;
	private static OFT oft;

	public FileSystem(){
		io = new IOSystem();
		desc_table = new byte[6][64];
		oft = new OFT();
	}
	
	public FileSystem(String restoreFile){
		io = new IOSystem(restoreFile);
		desc_table = new byte[6][64];
		oft = new OFT();
	}
	
	public void create(String name){

	}

	public void delete(String name){

	}

	public void open(String name){

	}

	public void close(String name){

	}

	public void read(int index, int count){

	}

	public void write(int index, byte c, int count){

	}

	public void seek(int index, int pos){

	}

	public void directory(){

	}

	public void save(String fileName){

	}
}
