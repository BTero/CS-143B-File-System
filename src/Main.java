import java.io.IOException;

public class Main {

	private static String studentName = "Brian Tero";
	private static String studentID = "90121402";
	private static String uciNetID = "BTero";

	private static int inputType = 1;				// 0 = console; 1 = file
	private static String inFile = "input.txt";

	public static void main(String[] args){
		Shell s = new Shell();
		s.run(inFile, inputType);
		
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
//		FileSystem fs = new FileSystem("test1.txt");
		
//		System.out.println(fs.create("x"));
//		System.out.println(fs.create("y"));
//		System.out.println(fs.create("z"));
//		System.out.println(fs.create("c"));
//		System.out.println(fs.create("v"));
//		System.out.println(fs.create("b"));
//		System.out.println(fs.create("n"));
//		System.out.println(fs.create("m"));
//		System.out.println(fs.create("l"));
//		System.out.println(fs.create("k"));
//		System.out.println(fs.delete("x"));
//		System.out.println(fs.delete("y"));
//		System.out.println(fs.delete("z"));
//		System.out.println(fs.delete("c"));
//		System.out.println(fs.delete("v"));
//		System.out.println(fs.delete("b"));
//		System.out.println(fs.delete("n"));
//		System.out.println(fs.delete("m"));
//		System.out.println(fs.delete("l"));
//		System.out.println(fs.delete("k"));
//		System.out.println(fs.create("j"));
//		System.out.println(fs.create("h"));
//		System.out.println(fs.create("g"));
//		System.out.println(fs.create("f"));
//		System.out.println(fs.create("d"));
//		System.out.println(fs.create("s"));
//		System.out.println(fs.create("a"));
//		System.out.println(fs.create("q"));
//		System.out.println(fs.create("w"));
//		System.out.println(fs.create("e"));
//		System.out.println(fs.create("r"));
//		System.out.println(fs.create("t"));
//		System.out.println(fs.create("y"));
//		System.out.println(fs.create("u"));
//		System.out.println(fs.create("i"));
//		
//		System.out.println(fs.open("y"));
//		System.out.println(fs.write(1, 'p', 10));
//		System.out.println(fs.seek(1, 0));
//		System.out.println(fs.read(1, 10));
//		System.out.println(fs.read(2, 10));
		
//		System.out.println(fs.directory());
//		System.out.println(fs.save("test2.txt"));

	}
}