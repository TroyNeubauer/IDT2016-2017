package contest.winter2017.ohsfile;

import java.io.*;

import com.troyberry.util.data.*;

public class OHSFileIO {
	/** The name of the highest level folder in the directory tree */
	public static final String FILE_DIR_NAME = "OHS IDT Output";
	/**
	 * The path to the folder that contains all .ohsd files from every execution
	 */
	public static final String ALL_FILES_FOLDER = "Files";
	/** The name of the Main File that has basic data about each execution */
	public static final String MAIN_FILE_NAME = "Main File.ohsm";// OHS mainfile
	/** The extension used for all the OHS Data files */
	public static final String EXTENSION = ".ohsd";// OHS Data

	/**
	 * Reads a file on disk to a OHSFile object
	 * 
	 * @param file
	 *            The file to read
	 * @return A loaded OHSFile with all of the de-serialized data in it
	 * @throws IOException
	 *             If an I/O error happens while reading the file
	 */
	public static OHSFile read(File file) throws IOException {
		OHSFile result = new OHSFile();
		try {
			TroyReader reader = null;
			try {
				reader = new TroyReader(file);
			} catch (Exception e) {
				throw new FileNotFoundException("Unable to find OHS file at " + file);
			}
			result.setName(file.getName());
			result.setVersion(reader.readShort());
			result.setTimestamp(reader.readLong());
			byte[] hash = new byte[16];
			for (int i = 0; i < hash.length; i++) {
				hash[i] = reader.readByte();
			}
			result.setHashOfJarFile(hash);
			result.setJarFileName(reader.readString());
			int passCount = reader.readInt();
			int failCount = reader.readInt();
			result.setPassCount(passCount);
			result.setTotalPercentCovered(reader.readFloat());
			int securityTestsConducted = reader.readInt();
			for (int i = 0; i < failCount; i++) {
				String argsUsed = reader.readString();
				String output = reader.readString();
				String err = reader.readString();
				String expectedOut = reader.readString();
				String expectedErr = reader.readString();
				result.addBasicTest(new BasicTest(argsUsed, output, err, expectedOut, expectedErr));
			}

			for (int i = 0; i < securityTestsConducted; i++) {
				float covered = reader.readFloat();
				String argsUsed = reader.readString();
				String output = reader.readString();
				String err = reader.readString();
				result.addSecurityTest(new SecurityTest(argsUsed, output, err, covered));
			}

		} catch (IOException e) {
			System.err.println("Failed to read OHS file at " + file + "!");
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Creates the skeleton of folders in the current directory
	 * 
	 * @throws IOException
	 *             If an I/O Error happens while creating the directories
	 */
	public static void createFolderStructure(File parent) throws IOException {
		File allFilesDir = null;
		if (parent == null) allFilesDir = new File("./" + FILE_DIR_NAME + "/" + ALL_FILES_FOLDER + "/");
		else allFilesDir = new File(parent + "/" + ALL_FILES_FOLDER + "/");
		allFilesDir.mkdirs();
	}

	/**
	 * Writes a OHSFile object to a file on disk. The file will be located in
	 * <code>"./OHS IDT Output/Files/(time stamp).ohsd"</code><br>
	 * This method will create all directories that are not present as well as
	 * creating the main file if necessary. This method will append basic info
	 * about this file (pass count, fail count) (not actual data like the
	 * command line args used) to the main file. <br>
	 * This should be executed after all tests are finished so that the OHSFile
	 * object is full of data to write
	 * 
	 * @param file
	 *            The OHSfile to use
	 * @param parent
	 * @return A Java File object as to where the file was saved on disk
	 * @throws IOException
	 *             If an I/O error happens while writing the file
	 */
	public static void write(OHSFile file, MainFile mainFile, boolean toolChain) throws IOException {
		if (mainFile == null) throw new NullPointerException("mainFile can't be null!");
		if (!mainFile.nameAvilable(file.getName())) {
			if (!toolChain) {
				System.err.println("Warning! The name \"" + file.getName() + "\" is already in use, the new file's name will now be set its timestamp.");
				System.out.println("Please choose a different name next time");
			}

			file.setName(file.getTimestamp() + "");
		}
		String name = (file.getName() == null || file.getName().isEmpty()) ? file.getTimestamp() + "" : file.getName();
		File thisFile = new File(mainFile.getTopFile() + "/" + ALL_FILES_FOLDER + "/" + name + EXTENSION);
		if (!toolChain) System.out.println("Writing OHSD file " + thisFile);
		try {
			TroyWriter writer = new TroyWriter();

			writer.writeShort(file.getVersion());
			writer.writeLong(file.getTimestamp());
			for (byte b : file.getHashOfJarFile()) {
				writer.writeByte(b);
			}
			writer.writeString(file.getJarFileName());
			writer.writeInt(file.getPassCount());
			writer.writeInt(file.getFailCount());
			writer.writeFloat((float) file.getTotalPercentCovered());
			writer.writeInt(file.getSecurityTests().size());
			for (BasicTest test : file.getFailedBasicTests()) {
				writer.writeString(test.getArgsUsed());
				writer.writeString(test.getOutputOut());
				writer.writeString(test.getOutputErr());
				writer.writeString(test.getExpectedOut());
				writer.writeString(test.getExpectedErr());
			}
			for (SecurityTest test : file.getSecurityTests()) {
				writer.writeFloat(test.getPercentCovered());
				writer.writeString(test.getArgsUsed());
				writer.writeString(test.getOutputOut());
				writer.writeString(test.getOutputErr());
			}
			writer.writeByte((byte) 0);

			writer.writeToFile(thisFile);
			mainFile.addEntry(new MainFileEntry(file));

		} catch (Exception e) {
			System.err.println("OHS File creation in folder " + thisFile + " failed!!");
			e.printStackTrace();
		}
	}

}
