package contest.winter2017.reader;

import java.io.*;

import javax.swing.*;

import com.troyberry.util.*;

import contest.winter2017.ohsfile.*;

public class ReaderMain {

	private static MainFrame frame;
	public static final boolean DEBUG = false;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				File openRightAway = parseArgs(args);
				frame = new MainFrame("OHS File Reader Program");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(1440, 810);
				frame.setLocation((1920 - frame.getWidth()) / 2, 0);
				frame.setVisible(true);
				showOpenDialog(openRightAway);
			}
		});
	}

	public static File getFileFromUser(JFrame frame) {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setCurrentDirectory(new File("./"));
		fc.setDialogTitle("Open OHS Save");

		int returnVal = fc.showDialog(frame, "Open");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		}
		return null;
	}

	public static MainFrame getFrame() {
		return frame;
	}

	/**
	 * Shows a dialog to open a OHS file and loads that file if a valid file is selected.
	 * @param openRightAway A file to open right away without opening the dialog. If openRightAway is null, 
	 * it will be ignored and the dialog will be opened as usual.
	 */
	public static void showOpenDialog(File openRightAway) {
		File file = null;
		try {
			if (openRightAway == null) {
				file = getFileFromUser(frame);
				if (file == null) return;
			} else {
				file = openRightAway;
			}
			//This code ensures that file points to a folder such as "C:/IDT/Test/OHS IDT Output/" the so called "main folder"
			//If the user selects any file within the main folder, this will correct the file object back to the main folder.
			//If the user selects the main file, We need to correct the current selection "C:/IDT/Test/OHS IDT Output/Main File.ohsm"
			//To "C:/IDT/Test/OHS IDT Output/"
			if (file.isDirectory()) {//If the user selected a folder
				if (file.getName().equals(OHSFileIO.ALL_FILES_FOLDER)) {// If they selected the "/Files/" folder
					file = file.getParentFile(); //Correct this by going up one to the main folder "OHS IDT Output/Files" -> "OHS IDT Output/"
				}
			} else {//If they selected a file
				if (file.getName().equals(OHSFileIO.MAIN_FILE_NAME)) {//If they selected the main file
					file = file.getParentFile();//Correct this by going up one to the main folder "OHS IDT Output/Main File.ohsm" -> "OHS IDT Output/"
				} else if (file.getParentFile().getName().equals(OHSFileIO.ALL_FILES_FOLDER)) { // If they selected a entry in the Files folder
					file = file.getParentFile().getParentFile();//Correct this by going up two folders:
					//"OHS IDT Output/Files/testfile.ohsd" -> "OHS IDT Output/"
				}
			}
			if (!new File(file, OHSFileIO.ALL_FILES_FOLDER).exists()) {//No main file, therefore, this is not an OHS file save
				JOptionPane.showMessageDialog(frame, "Could not open save at\n" + file + "\nCheck the path and try again", "Error Opening File",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			frame.addMainFile(MainFile.create(file));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Could not open file\n" + (ReaderMain.DEBUG ? MiscUtil.getStackTrace(e) : e.getMessage()),
					"Error Opening File", JOptionPane.ERROR_MESSAGE);
			System.err.println("Error! Unable to main file " + file);
			e.printStackTrace();
		}
	}

	private static File parseArgs(String[] args) {
		File file = null;
		if (args.length > 0) {
			if (args.length != 2 || !args[0].contains("open")) {
				System.out.println("Usage:");
				System.out.println("--open <file>");
				System.out.println("Putting double quotes (\") around the file might help!");
				System.exit(1);
			}
			file = new File(args[1]);
			if (!file.exists()) {
				System.out.println(file + " doesn't exist!");
				System.exit(1);
			}
		}
		return file;
	}

}
