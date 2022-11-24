import java.io.*;
import java.util.*;

/* rm PRINTER* && javac MainClass.java && java MainClass -1 -1 -1 */
// Know when to use join() properly, otherwise it will prevent concurrency
public class MainClass
{
	public static void main(String args[]) throws InterruptedException
	{
		int users = 0,
			disks = 0,
			printers = 0;
		
		if (args.length != 3
			|| args[0].length() < 2
			|| args[1].length() < 2
			|| args[2].length() < 2)
		{
			return;
		}
		users = Integer.valueOf(args[0].substring(1, args[0].length()));
		disks = Integer.valueOf(args[1].substring(1, args[1].length()));
		printers = Integer.valueOf(args[2].substring(1, args[2].length()));
		
		DiskManager diskMan = new DiskManager(disks);
		PrinterManager printMan = new PrinterManager(printers);
		DirectoryManager dirMan = new DirectoryManager();
		
		// assign statics
		UserThread.dirMan = dirMan;
		UserThread.diskMan = diskMan;
		PrintJobThread.diskMan = diskMan;
		PrintJobThread.dirMan = dirMan;
		PrintJobThread.printMan = printMan;
		
		// init users
		UserThread userThreads[] = new UserThread[users];
		for (int i = 0; i < userThreads.length; i++)
		{
			userThreads[i] = new UserThread(i);
			userThreads[i].start();
			/* userThreads[i].join(); */ 
		}
	}
}

class Disk
{
	static final int NUM_SECTORS = 2048;
	StringBuffer sectors[] = new StringBuffer[NUM_SECTORS];
	Disk()
	{
		sectors = new StringBuffer[NUM_SECTORS];
		for(int i = 0; i < NUM_SECTORS; i++)
		{
			sectors[i] = new StringBuffer();
		}
	}
	void write(int sector, StringBuffer data) throws InterruptedException
	{
		/* Thread.sleep(800); */
		System.out.println("Writing to sector: " + data);
		sectors[sector].setLength(0);
		sectors[sector].append(data);
	}
	void read(int sector, StringBuffer data) throws InterruptedException
	{
		/* Thread.sleep(800); */
		data.setLength(0);
		data.append(sectors[sector]);
		System.out.println("Read from sector: " + data);
	}
}

class Printer
{
	int id;
	Printer(int inID)
	{
		id = inID;
	}

	void print(StringBuffer data) throws InterruptedException, IOException
	{
		/* Thread.sleep(2750); */
		FileWriter fileWriter = new FileWriter("PRINTER" + id, true); // true for appending mode
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.println(data);
		System.out.println("PRINTER" + id + " printed: " + data);
		printWriter.close();
	}
}

class FileInfo
{
	int diskNumber;
	int startingSector;
	int fileLength;
}

class DirectoryManager
{
	private Hashtable<String, FileInfo> fileMap = new Hashtable<String, FileInfo>();

	DirectoryManager() {}

	void enter(StringBuffer fileName, FileInfo file) // fileName has to be a StringBuffer since we want to modify it in the UserThread's processLine()
	{
		fileMap.putIfAbsent(fileName.toString(), file);
	}

	FileInfo lookup(StringBuffer fileName)
	{
		return fileMap.get(fileName.toString()); // can return null
	}
}

class ResourceManager {
	boolean isFree[];
	ResourceManager(int numberOfItems) {
		isFree = new boolean[numberOfItems];
		for (int i=0; i<isFree.length; ++i)
			isFree[i] = true;
	}
	synchronized int request() throws InterruptedException
	{
		while (true) {
			for (int i = 0; i < isFree.length; ++i)
				if ( isFree[i] ) {
					isFree[i] = false;
					return i;
				}
			this.wait(); // block until someone releases Resource
		}
	}
	synchronized void release( int index ) {
		isFree[index] = true;
		this.notify(); // let a blocked thread run
	}
}

class DiskManager extends ResourceManager
{
	Disk disks[];
	int freeSector[]; // the current free sector index
	DiskManager(int numberOfItems)
	{
		super(numberOfItems);
		disks = new Disk[numberOfItems];
		freeSector = new int[numberOfItems];
		for (int i = 0; i < numberOfItems; i++)
		{
			disks[i] = new Disk();
			freeSector[i] = 0;
		}
	}
	FileInfo createFileInfo(int diskFree, int fileLength) // diskFree is obtained with request()
	{
		if (freeSector[diskFree] + fileLength >= Disk.NUM_SECTORS)
		{
			return null;
		}
		FileInfo finfo = new FileInfo();
		finfo.diskNumber = diskFree;
		finfo.startingSector = freeSector[diskFree];
		finfo.fileLength = fileLength;
		freeSector[diskFree] += fileLength;
		return finfo;
	}
}

class PrinterManager extends ResourceManager
{
	Printer printers[];
	PrinterManager(int numberOfItems)
	{
		super(numberOfItems);
		printers = new Printer[numberOfItems];
		for (int i = 0; i < numberOfItems; i++)
		{
			printers[i] = new Printer(i);
		}
	}
}

class PrintJobThread extends Thread
{
	static DiskManager diskMan;
	static DirectoryManager dirMan;
	static PrinterManager printMan;
	StringBuffer line = new StringBuffer(); // only allowed one line to reuse for read from disk and print to printer
	StringBuffer fileName = new StringBuffer();

	PrintJobThread(StringBuffer fileToPrint)
	{
		fileName = fileToPrint;
	}

	public void run()
	{
		try
		{
			process();
		}
		catch(IOException except)
		{
			System.out.println(except.toString());
		}
		catch(InterruptedException except)
		{
			System.out.println(except.toString());
		}
	}
	
	void process() throws IOException, InterruptedException
	{
		FileInfo finfo = dirMan.lookup(fileName);
		if (finfo == null) return;
		int freePrinter = printMan.request();
		for (int i = 0; i < finfo.fileLength; i++)
		{
			diskMan.disks[finfo.diskNumber].read(finfo.startingSector + i, line);
			printMan.printers[freePrinter].print(line);
		}
		printMan.release(freePrinter);
	}
}

class UserThread extends Thread
{
	static DiskManager diskMan;
	static DirectoryManager dirMan;
	int id;
	
	UserThread(int inID) // my commands come from an input file with name USERi where i is my user id
	{
		id = inID;
	}

	public void run()
	{
		try
		{
			process();
		}
		catch(FileNotFoundException except)
		{
			System.out.println(except.toString());
		}
		catch(InterruptedException except)
		{
			System.out.println(except.toString());
		}
	}
	
	void process() throws FileNotFoundException, InterruptedException
	{
		File file = new File("USER" + id);
		Scanner scanner = new Scanner(file);
		StringBuffer fileName = new StringBuffer();
		ArrayList<StringBuffer> fdata = new ArrayList<StringBuffer>();
		while(scanner.hasNextLine())
		{
			processLine(scanner.nextLine(), fileName, fdata);
		}
		scanner.close();
	}
	
	void processLine(String line, StringBuffer fileName, ArrayList<StringBuffer> fdata) throws InterruptedException
	{
		System.out.println("Processing " + line);
		StringTokenizer tokLine = new StringTokenizer(line);
		String token = tokLine.nextToken();
		if (token.equals(".save"))
		{
			fileName.setLength(0);
			fileName.append(tokLine.nextToken()); // use nextToken() to extract the virtual fileName
		}
		else if (token.equals(".end"))
		{
			int freeDisk = diskMan.request();
			FileInfo finfo = diskMan.createFileInfo(freeDisk, fdata.size());
			if (finfo == null) return;
			dirMan.enter(fileName, finfo);
			for (int i = 0; i < finfo.fileLength; i++)
			{
				diskMan.disks[freeDisk].write(finfo.startingSector + i, fdata.get(i));
			}
			diskMan.release(freeDisk);
			fdata.clear(); // clear for the next line
		}
		else if (token.equals(".print"))
		{
			PrintJobThread printJob = new PrintJobThread(new StringBuffer(tokLine.nextToken())); // use nextToken() to extract the virtual fileName
			printJob.start(); // start running concurrently
			/* printJob.join(); */
		}
		else // fileName's body
		{
			fdata.add(new StringBuffer(line));
		}
	}
}
















