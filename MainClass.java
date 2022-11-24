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
		FileWriter fileWriter = new FileWriter("PRINT" + id);
		PrintWriter printWriter = new PrintWriter(fileWriter);
	}
}

class PrintJobThread
	extends Thread
{
	StringBuffer line = new StringBuffer(); // only allowed one line to reuse for read from disk and print to printer

	PrintJobThread(String fileToPrint)
	{
	}

	public void run()
	{
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
	// private Hashtable<String, FileInfo> T = new Hashtable<String, FileInfo>();

	DirectoryManager()
	{
	}

	void enter(StringBuffer fileName, FileInfo file)
	{
	}

	FileInfo lookup(StringBuffer fileName)
	{
		return null;
	}
}

class ResourceManager
{
}

class DiskManager
{
}

class PrinterManager
{
}

class UserThread
	extends Thread
{
	UserThread(int id) // my commands come from an input file with name USERi where i is my user id
	{
	}

	public void run()
	{
	}
}


