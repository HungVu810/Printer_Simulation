import java.io.*;
import java.util.*;

public class MainClass
{
	public static void main(String args[])
	{
		
	}
}

class Disk
{
	static final int NUM_SECTORS = 2048;
	StringBuffer sectors[] = new StringBuffer[NUM_SECTORS];
	Disk()
	{
		sectors = new StringBuffer[NUM_SECTORS];
		for(StringBuffer sector : sectors)
		{
			sector = new StringBuffer();
		}
	}
	void write(int sector, StringBuffer data) throws InterruptedException
	{
		Thread.sleep(800);
		sectors[sector].setLength(0);
		sectors[sector].append(data);
	}
	void read(int sector, StringBuffer data) throws InterruptedException
	{
		Thread.sleep(800);
		data.setLength(0);
		data.append(sectors[sector]);
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
		Thread.sleep(2750);
		FileWriter fileWriter = new FileWriter("PRINTER" + id);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.println(data);
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

	void enter(String fileName, FileInfo file)
	{
		fileMap.putIfAbsent(fileName, file);
	}

	FileInfo lookup(String fileName)
	{
		return fileMap.get(fileName); // can return null
	}
}

class ResourceManager {
	boolean isFree[];
	ResourceManager(int numberOfItems) {
		isFree = new boolean[numberOfItems];
		for (int i=0; i<isFree.length; ++i)
			isFree[i] = true;
	}
	synchronized int request() {
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
		FileInfo finfo = new FileInfo();
		finfo.startingSector = diskFree;
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
		PrinterManager
	}
}

class PrintJobThread
	extends Thread
{
	static DiskManager diskMan;
	static PrinterManager printMan;
	StringBuffer line = new StringBuffer(); // only allowed one line to reuse for read from disk and print to printer
	String fileName = new String();

	PrintJobThread(String fileToPrint)
	{
		fileName = fileToPrint;
	}

	public void run()
	{
		try
		{
			process();
		}
		catch()
		{
		}
	}
	
	void process()
	{
	}
}

class UserThread
	extends Thread
d
	UserThread(int id) // my commands come from an input file with name USERi where i is my user id
	{
	}

	public void run()
	{
	}
}


