package contest.winter2017.ohsfile;

import java.io.*;
import java.util.*;

import com.troyberry.util.*;
import com.troyberry.util.data.*;

/**
 * This class represents the main file that stores basic information about each execution of our software. This file will be read by the GUI program
 * And will display the basic information about each execution (timestamp, pass count, fail count). When the user clicks on a execution, the OHS
 * file for that execution will be read and all the contents will be displayed.
 * @author Troy Neubauer
 *
 */
public class MainFile {
	private List<MainFileEntry> entries;
	private File mainFile;
	private File topFile;

	private MainFile(File file) throws IOException {
		this.entries = new ArrayList<MainFileEntry>();
		this.mainFile = file;
		this.topFile = file.getParentFile();
	}
	
	/**
	 * Creates any missing directories or the main file if not present.
	 * @param parent The parent file that the save tree will start in. <br>
	 * If parent is null, the tree will be created in the applocation's working directory.<br>
	 * For instance, if parent is "C:/IDT/Test/", The Main file will be created at "C:/IDT/Test/OHS IDT Output/Main File.ohsm", 
	 * and all the individual data files will be stored in "C:/IDT/Test/OHS IDT Output/Files/"
	 * @return A MainFile object that represents MainFile that was just created or the MainFile that already existed
	 * @throws IOException If an I/O Error occurs while the file is being accessed
	 */
	public static MainFile create(File parent) throws IOException {
		File mainFile = null;
		if(parent == null) mainFile = new File("./" +OHSFileIO.FILE_DIR_NAME + "/" + OHSFileIO.MAIN_FILE_NAME);
		else mainFile = new File(parent + "/" + OHSFileIO.MAIN_FILE_NAME);
		MainFile file = new MainFile(mainFile);
		OHSFileIO.createFolderStructure(parent);
		if(!mainFile.exists()) { // If the MainFile doesn't exist...
			// Create it and write 0 as an integer to indicate that there are no entries in this file yet..
			
			TroyWriter writer = new TroyWriter();
			writer.writeInt(0);
			writer.writeToFile(mainFile);// Write the 0 to a new file on disk
		} else {
			//Otherwise update its entries
			file.updateEntries();
		}
		return file;// Return the new object that points at the MainFile and the output directory
	}
	
	/**
	 * Attempts to find and load the OHS file described in the entry using its time stamp
	 * @param entry The entry to use to find the OHSFile
	 * @return The loaded OHSFile
	 * @throws IOException If the file cannot be found or if a I/O Error occurs
	 */
	public OHSFile getOHSFile(MainFileEntry entry) throws IOException {
		return getOHSFile0(entry.getName());
	}
	
	private OHSFile getOHSFile0(String name) throws IOException {
		File file = new File(topFile + "/" + OHSFileIO.ALL_FILES_FOLDER + "/" + name + OHSFileIO.EXTENSION);
		if(file.exists()) {
			return OHSFileIO.read(file);
		}
		throw new FileNotFoundException("Couldnt file the OHS file at \"" + file + "\" !");
		
	}
	
	/**
	 * Returns a list of all entries in this main file. <br>These entries are {@link MainFileEntry}, so they only contain basic information.
	 * To get a OHS file object with all the data in it, call {@link }
	 * @return A list of all the entries in the main file
	 */
	public List<MainFileEntry> getAllEntries() {
		return entries;
	}
	
	public void updateEntries() throws IOException {
		entries.clear();
		TroyReader reader = new TroyReader(mainFile);
		int entryCount = reader.readInt();
		for(int i = 0; i < entryCount; i++){
			MainFileEntry entry = new MainFileEntry();
			entry.read(reader);
			entries.add(entry);
		}
		
	}
	
	/**
	 * Adds an entry to the main file.<br>
	 * This should be called every time a HOS file is written so that the main file can keep track of it
	 * @param entry The entry to add
	 * @throws IOException If an I/O error occurs while the main file is being appended to
	 */
	public void addEntry(MainFileEntry entry) throws IOException {
		entries.add(entry);
		TroyWriter writer = new TroyWriter();
		byte[] prevFile = MiscUtil.readToByteArray(mainFile);
		TroyReader reader = new TroyReader(prevFile);
		int entries = reader.readInt();
		writer.writeInt(entries + 1);
		prevFile = ArrayUtil.subArray(prevFile, 4, prevFile.length);
		writer.writeBytes(prevFile);
		entry.write(writer);
		writer.writeToFile(mainFile);
	}

	public File getTopFile() {
		return topFile;
	}
	
	public File getMainFile() {
		return mainFile;
	}
	
	public boolean nameAvilable(String name) {
		for(MainFileEntry e : entries) {
			if(e.getName().equals(name))return false;
		}
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MainFile other = (MainFile) obj;
		if (entries == null) {
			if (other.entries != null)
				return false;
		} else if (!entries.equals(other.entries))
			return false;
		if (mainFile == null) {
			if (other.mainFile != null)
				return false;
		} else if (!mainFile.equals(other.mainFile))
			return false;
		return true;
	}
	
	
	
}
