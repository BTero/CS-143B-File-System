import java.util.ArrayList;


public class FileSystem {

	private static String studentName = "Brian Tero";
	private static String studentID = "90121402";
	private static String uciNetID = "BTero";

	// first 64 bytes are the data blocks being read from and written to; the rest are information blocks of the buffer
	// 64 beginning of is_open; 68 beginning of block_no; 72 beginning of desc_index; 76 beginning of file_size; 80 beginning of cur_pos
	private static final int bufferSize = 84;
	private static final int maxSize = 4;

	private static IOSystem io;
	private static char[][] desc_table;
	private static char[] directory;
	private static char[][] oft;
	private static int descriptors = 0;

	public FileSystem(){
		io = new IOSystem();
		desc_table = new char[7][64];
		oft = new char[maxSize][bufferSize];
		directory = new char[192];

		loadDescTable(0);
		allocateOFT(0);

		for(int i = 0; i < directory.length; i++){
			directory[i] = '0';
		}

		// find a block for directory to start from
		// allocate first block in directory descriptor and in oft
		int index = searchBitmap();
		desc_table[0][index] = '1';
		char[] indexTemp = itc(index);
		for(int i = 0; i < 4; i++){
			oft[0][i + 68] = indexTemp[i];
			desc_table[1][i + 4] = indexTemp[i];
		}
		descriptors = 1;
	}

	public FileSystem(String restoreFile){
		io = new IOSystem(restoreFile);
		desc_table = new char[7][64];
		oft = new char[maxSize][bufferSize];
		directory = new char[192];

		// restore information to data structures
		loadDescTable(1);
		loadDir();
		allocateOFT(1);
		descriptors = countDescriptors();
	}

	public int searchBitmap(){
		int index = -1;
		for(int i = 0; i < 64; i++){
			if(desc_table[0][i] == '0'){
				return i;
			}
		}
		return index;
	}

	public int cti(char[] data){
		int temp = 0;
		temp += Character.getNumericValue(data[0]) * 1000;
		temp += Character.getNumericValue(data[1]) * 100;
		temp += Character.getNumericValue(data[2]) * 10;
		temp += Character.getNumericValue(data[3]) * 1;
		return temp;
	}

	public void itc(int block, int pos, int val){
		// converts int to char array and places it in the corresponding location given
		char[] temp = ("" + val).toCharArray();
		for(int i = 0; i < 4; i++){
			desc_table[block][pos + i] = temp[i];
		}
	}

	public char[] itc(int val){
		char[] temp = new char[4];
		char[] temp1 = new char[4];
		temp1 = ("" + val).toCharArray();
		if(temp1.length == 3){
			temp[0] = '0';
			temp[1] = temp1[0];
			temp[2] = temp1[1];
			temp[3] = temp1[2];
		}else if(temp1.length == 2){
			temp[0] = '0';
			temp[1] = '0';
			temp[2] = temp1[0];
			temp[3] = temp1[1];
		}else{
			temp[0] = '0';
			temp[1] = '0';
			temp[2] = '0';
			temp[3] = temp1[0];
		}
		return temp;
	}

	public void allocateOFT(int restored){
		if(restored == 1){
			char[] size = new char[4];

			for(int i = 0; i < 4; i++){
				size[i] = desc_table[1][i];
			}

			// allocate size in oft entry and allocate cur_pos
			int dirSize = cti(size);
			for(int i = 0; i < 4; i++){
				oft[0][i + 64] = size[i];		// size
				oft[0][i + 80] = '0';			// cur_pos
				oft[0][i + 72] = '0';			// file_desc_index
			}			

			// allocate buffer to one that has space
			if(dirSize > 128){
				char[] index3 = new char[4];

				for(int i = 0; i < 4; i++){
					index3[i] = desc_table[1][12 + i];
				}

				// allocate block_no
				for(int i = 0; i < 4; i++){
					oft[0][i + 68] = index3[i];
				}

				char[] block3 = io.read_block(cti(index3));

				for(int i = 0; i < 64; i++){
					oft[0][i] = block3[i];
				}
			}else if(dirSize > 64){
				char[] index2 = new char[4];

				for(int i = 0; i < 4; i++){
					index2[i] = desc_table[1][8 + i];
				}

				// allocate block_no
				for(int i = 0; i < 4; i++){
					oft[0][i + 68] = index2[i];
				}

				char[] block2 = io.read_block(cti(index2));

				for(int i = 0; i < 64; i++){
					oft[0][i] = block2[i];
				}
			}else{
				char[] index1 = new char[4];

				for(int i = 0; i < 4; i++){
					index1[i] = desc_table[1][4 + i];
				}

				// allocate block_no
				for(int i = 0; i < 4; i++){
					oft[0][i + 68] = index1[i];
				}

				char[] block1 = io.read_block(cti(index1));

				for(int i = 0; i < 64; i++){
					oft[0][i] = block1[i];
				}
			}
			oft[0][67] = '1';
		}else{
			for(int i = 0; i < maxSize; i++){
				for(int j = 0; j < bufferSize; j++){
					oft[i][j] = '0';
				}
			}
			// 64 beginning of is_open; 68 beginning of block_no; 72 beginning of desc_index; 76 beginning of file_size; 80 beginning of cur_pos
			oft[0][67] = '1';		// directory buffer is always open.
		}

	}

	public void loadDir(){
		char[] size = new char[4];

		for(int i = 0; i < 4; i++){
			size[i] = desc_table[1][i];
		}

		int dirSize = cti(size);
		if(dirSize > 128){
			char[] index1 = new char[4];
			char[] index2 = new char[4];
			char[] index3 = new char[4];

			for(int i = 0; i < 4; i++){
				index1[i] = desc_table[1][4 + i];
				index2[i] = desc_table[1][8 + i];
				index3[i] = desc_table[1][12 + i];
			}

			char[] block1 = io.read_block(cti(index1));
			char[] block2 = io.read_block(cti(index2));
			char[] block3 = io.read_block(cti(index3));

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

			char[] block1 = io.read_block(cti(index1));
			char[] block2 = io.read_block(cti(index2));

			for(int i = 0; i < 64; i++){
				directory[i] = block1[i];
				directory[i + 64] = block2[i];
			}
		}else{
			char[] index1 = new char[4];

			for(int i = 0; i < 4; i++){
				index1[i] = desc_table[1][4 + i];
			}

			char[] block1 = io.read_block(cti(index1));

			for(int i = 0; i < 64; i++){
				directory[i] = block1[i];
			}
		}
	}

	public void loadDescTable(int restored){
		if(restored == 1){
			// read data blocks into desc_table
			for(int i = 0; i < 7; i++){
				char[] temp = io.read_block(i);
				for(int j = 0; j < 64; j++){
					desc_table[i][j] = temp[j];
				}
			}
		}else{
			// initialize desc_table
			for(int i = 0; i < 7; i++){
				for(int k = 0; k < 64; k++){
					desc_table[i][k] = '0';
				}
			}
			for(int i = 0; i < 7; i++){
				desc_table[0][i] = '1';
			}
		}
		
		
	}

	public int countDescriptors(){
		int count = 0;
		for(int i = 1; i < 7; i++){
			for(int k = 0; k < 64; k++){
				if(k % 16 == 0){
					char[] temp = new char[4];
					temp[0] = desc_table[i][k + 4];
					temp[1] = desc_table[i][k + 5];
					temp[2] = desc_table[i][k + 6];
					temp[3] = desc_table[i][k + 7];
					if(cti(temp) != 0){
						count++;
					}
				}
			}
		}
		return count;
	}

	public int findDescriptor(){
		// find a free file descriptor
		int index = -1;
		for(int i = 1; i < 7; i++){
			for(int k = 0; k < 64; k++){
				if(k % 16 == 0){
					char[] temp = new char[4];
					temp[0] = desc_table[i][k + 4];
					temp[1] = desc_table[i][k + 5];
					temp[2] = desc_table[i][k + 6];
					temp[3] = desc_table[i][k + 7];
					if(cti(temp) == 0){
						// found a free file descriptor
						index = k / 16;
						index += (i - 1) * 4;
						return index;
					}
				}
			}
		}
		return index;
	}

	public int searchDir(String name){
		// finds if a file of <name> already exists
		int index = -1;
		char[] temp = new char[4];
		if(descriptors == 24){
			// reached the end of directory
			return -2;
		}
		for(int i = 0; i < directory.length; i++){
			if(i % 8 == 0){
				temp[i % 8] = directory[i];
				temp[(i % 8) + 1] = directory[i + 1];
				temp[(i % 8) + 2] = directory[i + 2];
				temp[(i % 8) + 3] = directory[i + 3];
				if(name.equals(String.valueOf(temp))){
					index = i / 8;
					return index;
				}
			}
		}
		return index;
	}

	public int searchDir(){
		// finds first free directory entry
		int index = -1;
		char[] temp = new char[4];
		for(int i = 0; i < directory.length; i++){
			if(i % 8 == 0){
				temp[(i % 8)] = directory[i];
				temp[(i % 8) + 1] = directory[i + 1];
				temp[(i % 8) + 2] = directory[i + 2];
				temp[(i % 8) + 3] = directory[i + 3];
				if(String.valueOf(temp).equals("0000")){
					return i;
				}
			}

		}
		return index;
	}

	public int searchOFT(){
		char[] temp = new char[4];
		for(int i = 0; i < 64; i++){
			if(i % 8 == 0){
				temp[(i % 8)] = oft[0][i];
				temp[(i % 8) + 1] = oft[0][i + 1];
				temp[(i % 8) + 2] = oft[0][i + 2];
				temp[(i % 8) + 3] = oft[0][i + 3];
				if(String.valueOf(temp).equals("0000")){
					return i;		// return index of where to place new directory entry
				}
			}
		}
		return -1;		// returns -1 when there is no more space within directory buffer
	}


	public void switchBlock(int buffer, int size){
		// used for when OFT entry is full
		char[] desc_indexTemp = new char[4];
		char[] block_noTemp = new char[4];

		// retrieve block_no to write back to and file descriptor index
		for(int i =0 ; i < 4; i++){
			desc_indexTemp[i] = oft[buffer][72 + i];
			block_noTemp[i] = oft[buffer][68 + i];
		}
		int desc_index = cti(desc_indexTemp);
		int block_no = cti(block_noTemp);
		io.write_block(block_no, oft[buffer]);
		int new_block_no = searchBitmap();
		block_noTemp = itc(new_block_no);

		int desc_block = (desc_index / 4) + 1;
		int desc_pos = (desc_index % 4) * 16;
		clearOFTEntry(buffer);

		if(size == 64){
			for(int i = 0; i < 4; i++){
				desc_table[desc_block][desc_pos + 8 + i] = block_noTemp[i];
				oft[buffer][i + 68] = block_noTemp[i];		// updates block_no to the new one
				oft[buffer][i + 80] = '0';		// updates cur_pos to zero
			}

		}else if(size == 128){
			for(int i = 0; i < 4; i++){
				desc_table[desc_block][desc_pos + 12 + i] = block_noTemp[i];
				oft[buffer][i + 68] = block_noTemp[i];		// updates block_no to the new one
				oft[buffer][i + 80] = '0';		// updates cur_pos to zero
			}
		}
	}

	public void clearOFTEntry(int buffer){
		for(int i = 0; i < 64; i++){
			oft[buffer][i] = '0';
		}
	}

	public void clearOFT(int buffer){
		for(int i = 0; i < bufferSize; i++){
			oft[buffer][i] = '0';
		}
	}

	public String create(String name){
		String status = "";
		// search directory if a file of the name already exists
		int desc_index = searchDir(name);
		if(desc_index == -1){			// file not found
			// create file in directory and file descriptor and place it in the OFT entry
			char[] cName = new char[4];
			char[] temp = name.toCharArray();
			if(name.length() == 4){
				cName[0] = temp[0];		cName[1] = temp[1];
				cName[2] = temp[2];		cName[3] = temp[3];
			}else if(name.length() == 3){
				cName[0] = '0';		cName[1] = temp[0];
				cName[2] = temp[1];		cName[3] = temp[2];
			}else if(name.length() == 2){
				cName[0] = '0';		cName[1] = '0';
				cName[2] = temp[0];		cName[3] = temp[1];
			}else{
				cName[0] = '0';		cName[1] = '0';
				cName[2] = '0';		cName[3] = temp[0];
			}

			// search for a free file descriptor
			desc_index = findDescriptor();
			int dir_index = searchDir();
			char[] desc_temp = itc(desc_index);
			int block_no = (desc_index / 4) + 1;
			int pos = (desc_index % 4) * 16;
			int oft_index = searchOFT();
			int data_block = searchBitmap();
			if(data_block == -1){
				status = "Creation failed. No more logical blocks to allocate.";
			}else{
				char[] data_blockTemp = itc(data_block);
				desc_table[0][data_block] = '1';
				char[] oft_sizeTemp = new char[4];
				for(int i = 0; i < 4; i++){
					oft_sizeTemp[i] = oft[0][76 + i];
				}
				int oft_size = cti(oft_sizeTemp);			

				// OFT is full must open next buffer
				if(oft_index == -1){
					switchBlock(0, oft_size);
				}

				oft_size += 8;
				oft_sizeTemp = itc(oft_size);

				for(int i = 0; i < 4; i++){
					// fill directory entry
					directory[dir_index + i] = cName[i];
					directory[dir_index + i + 4] = desc_temp[i];

					// fill file descriptor
					desc_table[block_no][pos + 4] = data_blockTemp[i];

					// fill oft entry
					oft[0][oft_index + i] = cName[i];
					oft[0][oft_index + i + 4] = desc_temp[i];

					// update oft size
					oft[0][76 + i] = oft_sizeTemp[i]; 
				}
				descriptors++;
			}
		}else if(desc_index == -2){
			// reached the end of directory, no more space to create a new file
			status = "No more space in directory to fulfil file creation.";

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
		int is_open = Character.getNumericValue(oft[index][67]);
		
		if(is_open == 1){
			char[] block_noTemp = new char[4];
			for(int i = 0; i < 4; i++){
				block_noTemp[i] = oft[index][68 + i];
			}
			int block_no = cti(block_noTemp);
			io.write_block(block_no, oft[index]);
			clearOFT(index);
		}
	}

	public void read(int index, int count){

	}

	public void write(int index, char c, int count){

	}

	public void seek(int index, int pos){

	}

	public void directory(){

	}

	public void save(String out){
		//		char[]is_openTemp = new char[4];
		int is_open;

		for(int i = 0; i < 4; i++){
			close(i);
		}
		// save desc_table in ldisk
		for(int i = 0; i < 7; i++){
			io.write_block(i, desc_table[i]);
		}
		io.save(out);
	}
}
