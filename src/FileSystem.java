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
	private static int restored = 0; // 0 false 1 true
	private static int initialized = 0;

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
		initialized = 1;
	}

	public FileSystem(String restoreFile){
		io = new IOSystem(restoreFile);
		desc_table = new char[7][64];
		oft = new char[maxSize][bufferSize];
		directory = new char[192];

		// restore information to data structures
		loadDescTable(1);
		loadDir();
		allocateOFT(0);
		allocateOFT(1);
		descriptors = countDescriptors();
		restored = 1;
	}
	
	public int restored(){
		return restored;
	}
	
	public int initialized(){
		return initialized;
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
		if(name.length() == 3){
			name = 0 + name;
		}else if(name.length() == 2){
			name = "0" + "0" + name;
		}else if(name.length() == 1){
			name = "0" + "0" + "0" + name;
		}
		for(int i = 0; i < directory.length; i++){
			if(i % 8 == 0){
				temp[i % 8] = directory[i];
				temp[(i % 8) + 1] = directory[i + 1];
				temp[(i % 8) + 2] = directory[i + 2];
				temp[(i % 8) + 3] = directory[i + 3];
				if(name.equals(String.valueOf(temp))){
					return i;
				}

			}
		}
		if(descriptors == 24){
			// reached the end of directory
			return -2;
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

	public int searchOFT(){		// finds an allocatable spot in the oft
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

	public int searchOFTDir(int buffer){	// searches directory in oft
		int index = -1;
		char[] block_noTemp = new char[4];

		for(int i =0 ; i < 4; i++){
			block_noTemp[i] = oft[buffer][68 + i];
		}
		int block_no = cti(block_noTemp);
		io.write_block(block_no, oft[buffer]);

		char[] blockTemp1 = new char[4];
		char[] blockTemp2 = new char[4];
		char[] blockTemp3 = new char[4];
		for(int i = 0; i < 4; i++){
			blockTemp1[i % 4] = desc_table[1][4 + i];
			blockTemp2[i % 4] = desc_table[1][8 + i];
			blockTemp3[i % 4] = desc_table[1][12 + i];
		}
		int block1 = cti(blockTemp1);
		int block2 = cti(blockTemp2);
		int block3 = cti(blockTemp3);

		if(blockTemp1[0] == block_noTemp[0] &&
				blockTemp1[1] == block_noTemp[1] &&
				blockTemp1[2] == block_noTemp[2] &&
				blockTemp1[3] == block_noTemp[3]){
			if(block2 != 0){
				char[] data = io.read_block(block2);
				for(int i = 0; i < 64; i++){
					oft[0][i] = data[i];
				}
				for(int i = 0; i < 4; i++){
					oft[0][i + 68] = blockTemp2[i];
				}
			}
		}else if(blockTemp2[0] == block_noTemp[0] &&
				blockTemp2[1] == block_noTemp[1] &&
				blockTemp2[2] == block_noTemp[2] &&
				blockTemp2[3] == block_noTemp[3]){
			if(block3 != 0){
				char[] data = io.read_block(block3);
				for(int i = 0; i < 64; i++){
					oft[0][i] = data[i];
				}
				for(int i = 0; i < 4; i++){
					oft[0][i + 68] = blockTemp3[i];
				}
			}else if(block2 != 0){
				char[] data = io.read_block(block1);
				for(int i = 0; i < 64; i++){
					oft[0][i] = data[i];
				}
				for(int i = 0; i < 4; i++){
					oft[0][i + 68] = blockTemp1[i];
				}
			}
		}else if(blockTemp3[0] == block_noTemp[0] &&
				blockTemp3[1] == block_noTemp[1] &&
				blockTemp3[2] == block_noTemp[2] &&
				blockTemp3[3] == block_noTemp[3]){
			if(block1 != 0){
				char[] data = io.read_block(block1);
				for(int i = 0; i < 64; i++){
					oft[0][i] = data[i];
				}
				for(int i = 0; i < 4; i++){
					oft[0][i + 68] = blockTemp1[i];
				}
			}
		}


		return index;
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
		int desc_block_no = (desc_index / 4) + 1;
		int desc_pos = (desc_index % 4) * 16;
		int block_no = cti(block_noTemp);
		io.write_block(block_no, oft[buffer]);


		int new_block_no = searchBitmap();
		block_noTemp = itc(new_block_no);
		clearOFTEntry(buffer);

		if(size == 64){
			char[] temp = new char[4];
			for(int i = 0; i < 4; i++){
				temp[i] = desc_table[desc_block_no][desc_pos + 8 + i];
			}
			int tempno = cti(temp);
			if(tempno == 0){
				for(int i = 0; i < 4; i++){
					desc_table[desc_block_no][desc_pos + 8 + i] = block_noTemp[i];
					oft[buffer][i + 68] = block_noTemp[i];		// updates block_no to the new one
					oft[buffer][i + 80] = '0';		// updates cur_pos to zero
				}
				desc_table[0][new_block_no] = '1';
			}else{
				// next block has been allocated and needs to read into oft entry
				char[] data = io.read_block(tempno);
				for(int i = 0; i < 64; i++){
					oft[buffer][i] = data[i];
				}
				for(int i = 0; i < 4; i++){
					oft[buffer][68 + i] = temp[i];
				}
			}
		}else if(size == 128){
			char[] temp = new char[4];
			for(int i = 0; i < 4; i++){
				temp[i] = desc_table[desc_block_no][desc_pos + 12 + i];
			}
			int tempno = cti(temp);
			if(tempno == 0){
				for(int i = 0; i < 4; i++){
					desc_table[desc_block_no][desc_pos + 12 + i] = block_noTemp[i];
					oft[buffer][i + 68] = block_noTemp[i];		// updates block_no to the new one
					oft[buffer][i + 80] = '0';		// updates cur_pos to zero
				}
				desc_table[0][new_block_no] = '1';
			}else{
				// next block has been allocated and needs to read into oft entry
				char[] data = io.read_block(tempno);
				for(int i = 0; i < 64; i++){
					oft[buffer][i] = data[i];
				}
				for(int i = 0; i < 4; i++){
					oft[buffer][68 + i] = temp[i];
				}
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

	public char[] stc(String s){
		char[] cName = new char[4];
		char[] temp = s.toCharArray();
		if(s.length() == 4){
			cName[0] = temp[0];		cName[1] = temp[1];
			cName[2] = temp[2];		cName[3] = temp[3];
		}else if(s.length() == 3){
			cName[0] = '0';		cName[1] = temp[0];
			cName[2] = temp[1];		cName[3] = temp[2];
		}else if(s.length() == 2){
			cName[0] = '0';		cName[1] = '0';
			cName[2] = temp[0];		cName[3] = temp[1];
		}else{
			cName[0] = '0';		cName[1] = '0';
			cName[2] = '0';		cName[3] = temp[0];
		}
		return cName;
	}

	public String create(String name){
		String status = "";
		// search directory if a file of the name already exists
		int desc_index = searchDir(name);
		if(desc_index == -1){			// file not found
			// create file in directory and file descriptor and place it in the OFT entry
			char[] cName = stc(name);
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
					oft_index = searchOFT();
				}
				oft_size += 8;
				oft_sizeTemp = itc(oft_size);
				for(int i = 0; i < 4; i++){
					// fill directory entry
					directory[dir_index + i] = cName[i];
					directory[dir_index + i + 4] = desc_temp[i];
					
					// fill file descriptor
					desc_table[block_no][pos + 4 + i] = data_blockTemp[i];

					// fill oft entry
					oft[0][oft_index + i] = cName[i];
					oft[0][oft_index + i + 4] = desc_temp[i];

					// update oft size and directory file descriptor size
					oft[0][76 + i] = oft_sizeTemp[i];
					desc_table[1][i] = oft_sizeTemp[i];
				}
				descriptors++;
				status = "File " + name + " created.";
			}
		}else if(desc_index == -2){
			// reached the end of directory, no more space to create a new file
			status = "No more space in directory to fulfil file creation.";

		}else{	// file exists
			status = "File already exists.";
		}
		return status;
	}

	public String delete(String name){
		String status = "";
		int dir_index = searchDir(name);
		if(dir_index == -1){
			status = "File does not exist.";
		}else{
			char[] desc_indexTemp = new char[4];
			for(int i = 0; i < 4; i++){
				desc_indexTemp[i] = directory[dir_index + i + 4];
			}
			int desc_index = cti(desc_indexTemp);
			int desc_block_no = (desc_index / 4) + 1;
			int desc_pos = (desc_index % 4) * 16;
			// so we have the desc_index of where the file descriptor is located
			// retrieve the file size
			char[] dir_sizeTemp = new char[4];
			for(int i = 0; i < 4; i++){
				dir_sizeTemp[i] = desc_table[1][i];
			}
			int dir_size = cti(dir_sizeTemp);

			int oft_index = searchOFT(name);
			if(oft_index == -1){
				status = "File found in directory but not stored in OFT.";
			}else{
				// update OFT directory entry
				// clear directory entry in OFT
				for(int i = 0; i < 8; i++){
					oft[0][oft_index + i] = '0';
				}
				// reduce directory size
				dir_size -= 8;
				dir_sizeTemp = itc(dir_size);
				// update OFT's directory size and directory's file descriptor
				for(int i = 0; i < 4; i++){
					oft[0][i + 76] = dir_sizeTemp[i];
					desc_table[1][i] = dir_sizeTemp[i];
				}
				// delete file from directory
				for(int i = 0; i < 4; i++){
					directory[dir_index + i] = '0';
				}
				// delete file descriptor's data and delete logical blocks of data used by descriptor
				char[] desc_sizeTemp = new char[4];
				for(int i = 0; i < 4; i++){
					desc_sizeTemp[i] = desc_table[desc_block_no][desc_pos + i];
				}
				int desc_size = cti(desc_sizeTemp);

				if(desc_size >= 128){
					char[] blockTemp1 = new char[4];
					char[] blockTemp2 = new char[4];
					char[] blockTemp3 = new char[4];

					for(int i = 0; i < 4; i++){
						blockTemp1[i] = desc_table[desc_block_no][desc_pos + i + 4];
						blockTemp2[i] = desc_table[desc_block_no][desc_pos + i + 8];
						blockTemp3[i] = desc_table[desc_block_no][desc_pos + i + 12];
					}
					int block1 = cti(blockTemp1);
					int block2 = cti(blockTemp2);
					int block3 = cti(blockTemp3);

					//clear logical blocks used by the file descriptor
					clearDataBlock(block1);
					clearDataBlock(block2);
					clearDataBlock(block3);
				}else if(desc_size >= 64){
					char[] blockTemp1 = new char[4];
					char[] blockTemp2 = new char[4];

					for(int i = 0; i < 4; i++){
						blockTemp1[i] = desc_table[desc_block_no][desc_pos + i + 4];
						blockTemp2[i] = desc_table[desc_block_no][desc_pos + i + 8];
					}
					int block1 = cti(blockTemp1);
					int block2 = cti(blockTemp2);

					//clear logical blocks used by the file descriptor
					clearDataBlock(block1);
					clearDataBlock(block2);
				}else{
					char[] blockTemp1 = new char[4];

					for(int i = 0; i < 4; i++){
						blockTemp1[i] = desc_table[desc_block_no][desc_pos + i + 4];
					}
					int block1 = cti(blockTemp1);

					//clear logical blocks used by the file descriptor
					clearDataBlock(block1);
				}
				// clear file descriptor in reserved descriptor table
				for(int i = 0; i < 16; i++){
					desc_table[desc_block_no][desc_pos + i] = '0';
				}
				descriptors--;
				status = "File " + name + " deleted.";
				
				if(dir_size == 0){
					char[] block_noTemp	= new char[4];
					for(int i = 0; i < 4; i++){
						block_noTemp[i] = oft[0][68 +  i];
					}
					int block_no = cti(block_noTemp);
					io.write_block(block_no, oft[0]);
					for(int i = 0; i < 4; i++){
						block_noTemp[i] = desc_table[1][4 + i];
						oft[0][68 + i] = desc_table[1][4 + i];
					}
					block_no = cti(block_noTemp);
					char[] data = io.read_block(block_no);
					for(int i = 0; i < 64; i++){
						oft[0][i] = data[i];
					}
				}
			}
		}
		return status;
	}

	public int searchOFT(String name){
		int index = -1;
		char[] cName = stc(name);
		char[] file_sizeTemp = new char[4];
		for(int i = 0; i < 4; i++){
			file_sizeTemp[i] = oft[0][i + 76];
		}
		int file_size = cti(file_sizeTemp);
		int count;
		if(file_size >= 128){
			count = 3;
		}else if(file_size >= 64){
			count = 2;
		}else{
			count = 1;
		}

		do{
			for(int i = 0; i < 64; i++){
				if(i % 8 == 0 &&
						oft[0][i] == cName[i % 8] && oft[0][i + 1] == cName[(i % 8) + 1] &&
						oft[0][i + 2] == cName[(i % 8) + 2] && oft[0][i + 3] == cName[(i % 8) + 3]){
					// file found in oft buffer
					//					System.out.println(i);
					return i;
				}
			}

			if(count != 0){
				searchOFTDir(0);
			}count--;
		}while(count != -1);
		return index;
	}

	public void clearDataBlock(int block_no){
		char[] reset = new char[64];
		for(int i = 0; i < 64; i++){
			reset[i] = '0';
		}
		desc_table[0][block_no] = '0';
		io.write_block(block_no, reset);
	}

	public String open(String name){
		String status = "";
		int dir_index = searchDir(name);	// location within the directory; not the file descriptor index
		if(dir_index == -1){
			status = "Open failed, file does not exist.";
		}else{
			int is_open;
			char[] is_openTemp = new char[4];
			for(int i = 1; i < 4; i++){
				for(int k = 0; k < 4; k++){
					is_openTemp[k] = oft[i][k + 64];
				}
				is_open = cti(is_openTemp);
				if(is_open == 0){		// OFT entry is open
					// load file descriptor into oft entry
					is_open = 1;
					is_openTemp = itc(is_open);

					char[] desc_indexTemp = new char[4];
					char[] file_sizeTemp = new char[4];
					for(int m = 0; m < 4; m++){
						desc_indexTemp[m] = directory[dir_index + m + 4];

						// update desc_index in oft
						oft[i][m + 72] = directory[dir_index + m + 4];
						// update is_open in oft
						oft[i][m + 64] = is_openTemp[m];
					}

					int desc_index = cti(desc_indexTemp);
					int desc_block = (desc_index / 4) + 1;
					int desc_pos = (desc_index % 4) * 16;
					for(int m = 0; m < 4; m++){
						file_sizeTemp[m] = desc_table[desc_block][desc_pos + m];

						// update file size in oft
						oft[i][m + 76] = desc_table[desc_block][desc_pos + m];
					}
					int file_size = cti(file_sizeTemp);

					//update block_no depending on file size in oft
//					if(file_size >= 128){
//						for(int n = 0; n < 4; n++){
//							oft[i][n + 68] = desc_table[desc_block][desc_pos + n + 12];
//						}
//					}else if(file_size >= 64){
//						for(int n = 0; n < 4; n++){
//							oft[i][n + 68] = desc_table[desc_block][desc_pos + n + 8];
//						}
//					}else{
//						for(int n = 0; n < 4; n++){
//							oft[i][n + 68] = desc_table[desc_block][desc_pos + n + 4];
//						}
//					}
					char[] block_noTemp = new char[4];
					for(int l = 0; l < 4; l++){
						block_noTemp[l] = desc_table[desc_block][desc_pos + l + 4];
					}
					int block_no = cti(block_noTemp);
					for(int l = 0; l < 4; l++){
						oft[i][l + 68] = block_noTemp[l];
					}
					char[] data = io.read_block(block_no);
					for(int l = 0; l < 64; l++){
						oft[i][l] = data[l];
					}

					return "File loaded into index " + i + ".";
				}
			}
			status = "OFT table is full.";
		}
		return status;
	}

	public String close(int index){
		String status = "";
		int is_open = Character.getNumericValue(oft[index][67]);

		if(is_open == 1){
			char[] block_noTemp = new char[4];
			for(int i = 0; i < 4; i++){
				block_noTemp[i] = oft[index][68 + i];
			}
			int block_no = cti(block_noTemp);
			io.write_block(block_no, oft[index]);
			clearOFT(index);
			status = "OFT entry at " + index + " closed.";
		}else{
			status = "OFT entry at " + index + " was not opened.";
		}
		return status;
	}

	public String read(int index, int count){
		String status = "";
		int is_open = Character.getNumericValue(oft[index][67]);

		if(is_open == 1){
			char[] cur_posTemp = new char[4];
			char[] file_sizeTemp = new char[4];
			for(int i = 0; i < 4; i++){
				cur_posTemp[i] = oft[index][i + 80];
				file_sizeTemp[i] = oft[index][i + 76];
			}
			int cur_pos = cti(cur_posTemp);
			int file_size = cti(file_sizeTemp);
			
			if(count + cur_pos > 192){
				status = "Read failed, " + count + " exceeds maximum file length.";
			}else{
				status = count + " bytes read: ";
				for(int i = 0; i < 64; i++){
					System.out.print(oft[index][i]);
				}System.out.println();
				for(int i = 0; i < count; i++){
					if(cur_pos == 64){
						switchBlock(index, 64);
					}else if(cur_pos == 128){
						switchBlock(index, 128);
					}
					status += oft[index][(cur_pos % 64)];
					cur_pos++;
				}
				cur_posTemp = itc(cur_pos);
				for(int i = 0; i < 4; i++){
					oft[index][i + 80] = cur_posTemp[i];
				}
				
			}
		}else{
			status = "Read failed. OFT entry at " + index + " was not opened.";
		}
		return status;
	}

	public String write(int index, char c, int count){
		String status = "";
		int is_open = Character.getNumericValue(oft[index][67]);
		if(is_open == 1){
			char[] cur_posTemp = new char[4];
			char[] file_sizeTemp = new char[4];
			for(int i = 0; i < 4; i++){
				cur_posTemp[i] = oft[index][i + 80];
				file_sizeTemp[i] = oft[index][i + 76];
			}
			int cur_pos = cti(cur_posTemp);
			int file_size = cti(file_sizeTemp);

			if(count + cur_pos > 192){
				status = "Write failed, " + count + " exceeds maximum file length.";
			}else{
				for(int i = 0; i < count; i++){
					if(cur_pos == 64){
						switchBlock(index, 64);
					}else if(cur_pos == 128){
						switchBlock(index, 128);
					}
					oft[index][(cur_pos % 64)] = c;
					cur_pos++;
				}
				file_size += count;
				file_sizeTemp = itc(file_size);
				cur_posTemp = itc(cur_pos);
				for(int i = 0; i < 4; i++){
					oft[index][i + 76] = file_sizeTemp[i];
					oft[index][i + 80] = cur_posTemp[i];
				}
				status = count + " bytes written.";
			}
		}else{
			status = "Write failed. OFT entry at " + index + " was not opened.";
		}
		return status;
	}

	public String seek(int index, int pos){
		String status = "";
		int is_open = Character.getNumericValue(oft[index][67]);
		if(is_open == 1){
			char[] cur_posTemp = itc(pos);
			char[] desc_indexTemp = new char[4];
			for(int i = 0; i < 4; i++){
				desc_indexTemp[i] = oft[index][72 + i];
			}
			int desc_index = cti(desc_indexTemp);
			int desc_block = (desc_index / 4) + 1;
			int desc_pos = (desc_index % 4) * 16;

			if(pos >= 128){
				if(desc_table[desc_block][desc_pos + 8] == '0' &&
						desc_table[desc_block][desc_pos + 9] == '0' &&
						desc_table[desc_block][desc_pos + 10] == '0' &&
						desc_table[desc_block][desc_pos + 11] == '0'){
					int newBlock_no = allocateBlock(desc_block, desc_pos + 8);

					if(desc_table[desc_block][desc_pos + 12] == '0' &&
							desc_table[desc_block][desc_pos + 13] == '0' &&
							desc_table[desc_block][desc_pos + 14] == '0' &&
							desc_table[desc_block][desc_pos + 15] == '0'){
						newBlock_no = allocateBlock(desc_block, desc_pos + 12);
					}

					char[] newBlock_noTemp = itc(newBlock_no);
					for(int i = 0; i < 4; i++){
						oft[index][i + 72] = newBlock_noTemp[i];
					}
				}else if(desc_table[desc_block][desc_pos + 12] == '0' &&
						desc_table[desc_block][desc_pos + 13] == '0' &&
						desc_table[desc_block][desc_pos + 14] == '0' &&
						desc_table[desc_block][desc_pos + 15] == '0'){
					int newBlock_no = allocateBlock(desc_block, desc_pos + 12);
					char[] newBlock_noTemp = itc(newBlock_no);
					for(int i = 0; i < 4; i++){
						oft[index][i + 72] = newBlock_noTemp[i];
					}
				}
			}else if(pos >= 64){
				if(desc_table[desc_block][desc_pos + 8] == '0' &&
						desc_table[desc_block][desc_pos + 9] == '0' &&
						desc_table[desc_block][desc_pos + 10] == '0' &&
						desc_table[desc_block][desc_pos + 11] == '0'){
					int newBlock_no = allocateBlock(desc_block, desc_pos + 8);
					char[] newBlock_noTemp = itc(newBlock_no);

					for(int i = 0; i < 4; i++){
						oft[index][i + 72] = newBlock_noTemp[i];
					}
				}
			}else{
				char[] block_noTemp = new char[4];
				for(int i = 0; i < 4; i++){
					block_noTemp[i] = oft[index][68 + i];
				}
				int block_no = cti(block_noTemp);
				io.write_block(block_no, oft[index]);
				for(int i = 0; i < 4; i++){
					block_noTemp[i] = desc_table[desc_block][desc_pos + i + 4];
					oft[index][i + 68] = desc_table[desc_block][desc_pos + i + 4];
				}
				block_no = cti(block_noTemp);
				char[] data = io.read_block(block_no);
				for(int i = 0; i < 64; i++){
					oft[index][i] = data[i];
				}
			}

			for(int i = 0; i < 4; i++){
				oft[index][80 + i] = cur_posTemp[i];
			}			
			status = "Current pos is " + pos;
		}else{
			status = "OFT entry at " + index + " was not opened.";
		}
		return status;
	}

	public int allocateBlock(int desc_block, int desc_pos){
		int newBlock_no = searchBitmap();
		char[] newBlock_noTemp = itc(newBlock_no);
		for(int i = 0; i < 4; i++){
			desc_table[desc_block][desc_pos] = newBlock_noTemp[i];
		}
		desc_table[0][newBlock_no] = '1';
		return newBlock_no;
	}

	public String cts(char[] data){
		String s = "";
		for(int i = 0; i < 4; i++){
			if(data[i] != '0'){
				s += data[i];
			}
		}
		return s;
	}

	public String directory(){
		String status = "";
		if(descriptors == 1){
			status = "No files in directory.";
		}else{
			char[] temp = new char[4];
			for(int i = 0; i < directory.length; i++){
				if(i % 8 == 0){
					temp[(i % 8)] = directory[i];			temp[(i % 8) + 1] = directory[i + 1];
					temp[(i % 8) + 2] = directory[i + 2];	temp[(i % 8) + 3] = directory[i + 3];
					status += cts(temp);
				}
			}
		}
		return status;
	}

	public String save(String out){
		for(int i = 0; i < 4; i++){
			close(i);
		}
		// save desc_table in ldisk
		for(int i = 0; i < 7; i++){
			io.write_block(i, desc_table[i]);
		}
		io.save(out);
		return "Disk saved.";
	}
}
