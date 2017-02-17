package contest.winter2017;

import java.io.*;

import org.apache.commons.cli.*;
import org.apache.commons.cli.Options;

import contest.winter2017.ohsfile.*;
import contest.winter2017.reader.*;

/**
 * Entry-point class for the black-box testing framework
 * 
 * @author IDT
 */
@SuppressWarnings("all")
public class Main {

	public static boolean DEBUG = true;

	public static File OHSFileSaveLocation = null;
	/**
	 * cli key for path to the executable black-box jar to test
	 */
	public static final String JAR_TO_TEST_PATH = "jarToTestPath";

	/**
	 * cli key for path to the directory to be used to store output generated by
	 * jacoco framework
	 */
	public static final String JACOCO_OUTPUT_PATH = "jacocoOutputPath";

	/**
	 * cli key for path to the jacoco agent jar used to instrument the
	 * executable black-box jar in order to collect code coverage metrics
	 */
	public static final String JACOCO_AGENT_JAR_PATH = "jacocoAgentJarPath";

	/**
	 * cli key for application help
	 */
	public static final String HELP = "help";

	/**
	 * alternative cli key for application help
	 */
	public static final String ALT_HELP = "h";

	/**
	 * cli key for the number of exploratory black box tests to run
	 */
	public static final String BLACK_BOX_TESTS = "bbTests";

	/**
	 * cli key for the test time goal in minutes
	 */
	public static final String TIME_GOAL = "timeGoal";

	/**
	 * cli key for limitting the output to contain only the parseable YAML
	 * report
	 */
	public static final String TOOL_CHAIN = "toolChain";

	/**
	 * where to save the resulting data to
	 */
	public static final String OHS_FILE_OUTPUT = "saveTo";

	/**
	 * weather or not saving is disabled (enabled by default)
	 */
	public static final String DISABLE_OHS_SAVE = "disableSaving";
	
	/**
	 * the name of the OHS file
	 */
	public static final String SAVE_NAME = "saveName";

	/**
	 * weather or not saving is disabled (enabled by default)
	 */
	private static boolean disableSaving = false;

	private static boolean hasTimeGoal, hasBBtests = false;
	private static String saveName;

	/**
	 * Entry-point method for the black-box testing framework
	 * 
	 * @param args
	 *            - String array of command line arguments
	 */
	public static void main(String[] args) {

		CommandLineParser parser = new DefaultParser();

		Options options = new Options();
		options.addOption(JAR_TO_TEST_PATH, true, "Path to the executable jar to test");
		options.addOption(JACOCO_OUTPUT_PATH, true, "Path to directory for jacoco output");
		options.addOption(JACOCO_AGENT_JAR_PATH, true, "Path to the jacoco agent jar");
		options.addOption(HELP, false, "Help");
		options.addOption(ALT_HELP, false, "Help");
		options.addOption(BLACK_BOX_TESTS, true, "Number of black box test to be executed");
		options.addOption(TIME_GOAL, true, "Test time goal in minutes");
		options.addOption(TOOL_CHAIN, false, "Output only the YAML report");
		options.addOption(OHS_FILE_OUTPUT, true, "Where to save the custom file holding test information to");
		options.addOption(DISABLE_OHS_SAVE, false, "Disable saving to an OHS file after testing is complete");
		options.addOption(SAVE_NAME, true, "A name for the OHS file");

		try {
			CommandLine cliArgs = parser.parse(options, args);

			if (cliArgs != null) {

				// if we have the three arguments we need for exploratory
				// black-box testing, initialize and execute the tester.
				if (cliArgs.hasOption(JAR_TO_TEST_PATH) && cliArgs.hasOption(JACOCO_OUTPUT_PATH) && cliArgs.hasOption(JACOCO_AGENT_JAR_PATH)) {

					String jarToTestPath = cliArgs.getOptionValue(JAR_TO_TEST_PATH);
					String jacocoOutputDirPath = cliArgs.getOptionValue(JACOCO_OUTPUT_PATH);
					String jacocoAgentJarPath = cliArgs.getOptionValue(JACOCO_AGENT_JAR_PATH);

					// the Tester class contains all of the logic for the
					// testing framework
					int bbTests = 1000;
					long timeGoal = 5 * 60 * 1000;// 5 minutes * 60 seconds per minutes * 1000 MS per second
					boolean toolChain = false;
					
					//Parse the arguments and set the values
					if (cliArgs.hasOption(BLACK_BOX_TESTS)) {
						if (Integer.parseInt(cliArgs.getOptionValue(BLACK_BOX_TESTS)) < 0) {
							System.out.println("An illegal argument was entered. Please enter a positive integer.");
							printHelp(options);
							return;
						}
						bbTests = Integer.parseInt(cliArgs.getOptionValue(BLACK_BOX_TESTS));
						hasBBtests = true;
					}

					if (cliArgs.hasOption(TIME_GOAL)) {
						if (Integer.parseInt(cliArgs.getOptionValue(TIME_GOAL)) < 0) {
							System.out.println("An illegal argument was entered. Please enter a positive integer.");
							printHelp(options);
							return;
						}
						timeGoal = Long.parseLong(cliArgs.getOptionValue(TIME_GOAL)) * 60L * 1000L;
						hasTimeGoal = true;
					}
					
					if (cliArgs.hasOption(SAVE_NAME)) {
						saveName = cliArgs.getOptionValue(SAVE_NAME);
					}
					
					if (cliArgs.hasOption(TOOL_CHAIN)) {
						toolChain = true;
					}

					if (cliArgs.hasOption(OHS_FILE_OUTPUT)) {
						String path = cliArgs.getOptionValue(OHS_FILE_OUTPUT);
						OHSFileSaveLocation = new File(path);
					} else {
						OHSFileSaveLocation = new File("./" + OHSFileIO.FILE_DIR_NAME + "/");
					}

					if (cliArgs.hasOption(DISABLE_OHS_SAVE)) {
						disableSaving = true;
					}
					boolean stopAtBBTests = true;
					// If they didnt specify anything
					if (!hasTimeGoal && !hasBBtests) {
						stopAtBBTests = true;
						bbTests = 1000;
						timeGoal = 5 * 60L * 1000L;
					}
					// They specified BB tests but not time goal
					if (!hasTimeGoal && hasBBtests) stopAtBBTests = true;

					// They didn't specify BB but specified time goal
					if (hasTimeGoal && !hasBBtests) stopAtBBTests = false;

					// They specified both
					if (hasTimeGoal && hasBBtests) stopAtBBTests = true;

					Tester tester = new Tester(bbTests, timeGoal, toolChain);
					if (tester.init(jarToTestPath, jacocoOutputDirPath, jacocoAgentJarPath)) {

						tester.executeBasicTests();
						tester.executeSecurityTests(hasTimeGoal, hasBBtests, stopAtBBTests);
						if (!disableSaving) {
							MainFile outputFile = null;
							OHSFile file = tester.getOHSFile();
							if(saveName != null) file.setName(saveName);
							try {
								outputFile = MainFile.create(OHSFileSaveLocation);
								OHSFileIO.write(file, outputFile);
							} catch (IOException e) {
								System.err.println("Unable to generate results file!");
								e.printStackTrace();
							}
							runReaderProgram(outputFile);
						}
					}

				} else if (cliArgs.hasOption(HELP) || cliArgs.hasOption(ALT_HELP) || cliArgs.hasOption(TOOL_CHAIN)) {
					
					printHelp(options);

				} else {

					System.out.println("Failed to execute - application requires at least three parameters.");
					printHelp(options);

				}
			}

		} catch (ParseException exp) {
			System.out.println("An error occurred during command line parsing: " + exp.getMessage());
		}
	}

	/**
	 * Runs the reader program
	 * 
	 * @param outputFile
	 */
	private static void runReaderProgram(MainFile outputFile) {
		ReaderMain.main(new String[] { "open", outputFile.getMainFile().toString() });
	}

	/**
	 * private static method used to print the application help
	 */
	private static void printHelp(Options options) {
		String header = "\n";
		String footer = "\nFor additional information about the testing framework, please see the documentation provided by IDT.";

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("com.idtus.contest.winter2017.framework", header, options, footer, true);
	}

}