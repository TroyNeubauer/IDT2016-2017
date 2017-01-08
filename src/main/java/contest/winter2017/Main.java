package contest.winter2017;

import org.apache.commons.cli.*;

/**
 * Entry-point class for the black-box testing framework
 * 
 * @author IDT
 */
public class Main {

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
	 * cli key for limitting the output to contain only the parseable
	 * YAML report
	 */
	public static final String TOOL_CHAIN = "toolChain";

	/**
	 * Entry-point method for the black-box testing framework
	 * 
	 * @param args
	 *            - String array of command line arguments
	 */
	public static void main(String[] args) {


		CommandLineParser parser = new DefaultParser();

		Options options = new Options();
		options.addOption(JAR_TO_TEST_PATH, true, "path to the executable jar to test");
		options.addOption(JACOCO_OUTPUT_PATH, true, "path to directory for jacoco output");
		options.addOption(JACOCO_AGENT_JAR_PATH, true, "path to the jacoco agent jar");
		options.addOption(HELP, false, "help");
		options.addOption(ALT_HELP, false, "help");
		options.addOption(BLACK_BOX_TESTS, true, "number of black box test to be executed");
		options.addOption(TIME_GOAL, true, "test time goal in minutes");
		options.addOption(TOOL_CHAIN, false, "output only the YAML report");

		try {
			CommandLine cliArgs = parser.parse(options, args);
			

			if (cliArgs != null) {
				//int bbTests = -1;
				int timeGoal = 0;
				boolean toolChain = false;
				
				// if we have the three arguments we need for exploratory
				// black-box testing, initialize and execute the tester.
				if (cliArgs.hasOption(JAR_TO_TEST_PATH) && cliArgs.hasOption(JACOCO_OUTPUT_PATH)
						&& cliArgs.hasOption(JACOCO_AGENT_JAR_PATH)) {

					String jarToTestPath = cliArgs.getOptionValue(JAR_TO_TEST_PATH);
					String jacocoOutputDirPath = cliArgs.getOptionValue(JACOCO_OUTPUT_PATH);
					String jacocoAgentJarPath = cliArgs.getOptionValue(JACOCO_AGENT_JAR_PATH);


					// the Tester class contains all of the logic for the
					// testing framework
					Tester tester = new Tester();
					if (tester.init(jarToTestPath, jacocoOutputDirPath, jacocoAgentJarPath)) {
						tester.executeBasicTests(); // this is the simple
													// testing that we have
													// implemented - likely no
													// need to change this code
													// much
						tester.executeSecurityTests(); // this is the security
														// vulnerability testing
														// that we want you to
														// implement
					}

					// if the user has requested help
				} else if (cliArgs.hasOption(HELP) || cliArgs.hasOption(ALT_HELP)) {

					printHelp(options);

					// user did not request help and we had an inadequate number
					// of arguments
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
	 * private static method used to print the application help
	 */
	private static void printHelp(Options options) {
		String header = "\n";
		String footer = "\nFor additional information about the testing framework, please see the documentation provided by IDT.";

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("com.idtus.contest.winter2017.framework", header, options, footer, true);
	}

}
