package contest.winter2017;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.jar.*;

import org.jacoco.core.analysis.*;
import org.jacoco.core.data.*;
import org.jacoco.core.tools.*;

import com.troyberry.util.*;

import contest.winter2017.ohsfile.*;
import contest.winter2017.range.*;

/**
 * Class that will handle execution of basic tests and exploratory security test
 * on a black-box executable jar.
 * 
 * Example code that we used to guide our use of Jacoco code coverage was
 * found @ http://www.eclemma.org/jacoco/trunk/doc/api.html
 * 
 * @author IDT
 */
public class Tester {

	/**
	 * suffix for all jacoco output files
	 */
	private static final String JACOCO_OUTPUT_FILE_SUFFIX = "_jacoco.exec";

	/**
	 * horizontal line shown between test output
	 */
	private static final String HORIZONTAL_LINE = "-------------------------------------------------------------------------------------------";

	/**
	 * path of the jar to test as a String
	 */
	private String jarToTestPath = null;

	/**
	 * path of the directory for jacoco output as a String
	 */
	private String jacocoOutputDirPath = null;

	/**
	 * path to the jacoco agent library as a String
	 */
	private String jacocoAgentJarPath = null;

	/**
	 * path to the file for jacoco output as a String
	 */
	private String jacocoOutputFilePath = null;

	/**
	 * basic tests that have been extracted from the jar under test
	 */
	private List<Test> tests = null;

	/**
	 * parameter factory that can be used to help figure out parameter
	 * signatures from the blackbox jars
	 */
	private ParameterFactory parameterFactory = null;

	/**
	 * bbTests is the number of black box tests to execute
	 */
	private final int BBTESTS;

	/**
	 * timeGoal is the amount time in milliseconds that we have to match
	 */
	private final long TIMEGOAL;

	/**
	 * if true, only the YAML report is in the output
	 */
	private final boolean TOOLCHAIN;

	private final boolean STOPATBBTESTS;

	private final OHSFile outputFile;

	/**
	 * array list of unique errors seen
	 */
	ArrayList<String> errors = new ArrayList<String>();

	public Tester(int bbTests, int timeGoal, boolean toolChain, boolean stopAtBBTests) {
		this.outputFile = new OHSFile();
		outputFile.setTimestamp(System.currentTimeMillis());
		this.BBTESTS = bbTests;
		this.TIMEGOAL = timeGoal; // If user didn't enter a time goal, this
									// number will be negative. if we don't set
									// a variable called time to end and instead
									// do currenttime-timegoal >0 this can work
									// bc there is no time goal
		this.TOOLCHAIN = toolChain;
		this.STOPATBBTESTS = stopAtBBTests;

	}

	//////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////

	/**
	 * Method that will initialize the Framework by loading up the jar to test,
	 * and then extracting parameters, parameter bounds (if any), and basic
	 * tests from the jar.
	 * 
	 * @param initJarToTestPath
	 *            - String representing path of the jar to test
	 * @param initJacocoOutputDirPath
	 *            - String representing path of the directory jacoco will use
	 *            for output
	 * @param initJacocoAgentJarPath
	 *            - String representing path of the jacoco agent jar
	 * @return boolean - false if initialization encounters an Exception, true
	 *         if it does not
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean init(String initJarToTestPath, String initJacocoOutputDirPath, String initJacocoAgentJarPath) {

		this.jarToTestPath = initJarToTestPath;
		this.jacocoOutputDirPath = initJacocoOutputDirPath;
		this.jacocoAgentJarPath = initJacocoAgentJarPath;

		File jarFileToTest = new File(this.jarToTestPath);
		this.jacocoOutputFilePath = this.jacocoOutputDirPath + "\\" + jarFileToTest.getName().replaceAll("\\.", "_")
				+ JACOCO_OUTPUT_FILE_SUFFIX;

		outputFile.setJarFileName(initJarToTestPath);
		try {
			outputFile.setHashOfJarFile(Utils.createChecksum(jarFileToTest));
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Unable to generate checksum of jar file to test");
		}

		File jacocoOutputFile = new File(this.jacocoOutputFilePath);
		if (jacocoOutputFile != null && jacocoOutputFile.exists()) {
			jacocoOutputFile.delete();
		}

		URL fileURL = null;
		URL jarURL = null;
		try {

			// load up the jar under test so that we can access information
			// about the class from 'TestBounds'
			fileURL = jarFileToTest.toURI().toURL();
			String jarUrlTemp = "jar:" + jarFileToTest.toURI().toString() + "!/";
			jarURL = new URL(jarUrlTemp);
			URLClassLoader cl = URLClassLoader.newInstance(new URL[] { fileURL });
			JarURLConnection jarURLconn = null;
			jarURLconn = (JarURLConnection) jarURL.openConnection();

			// figuring out where the entry-point (main class) is in the jar
			// under test
			Attributes attr = null;
			attr = jarURLconn.getMainAttributes();
			String mainClassName = attr.getValue(Attributes.Name.MAIN_CLASS);

			// loading the TestBounds class from the jar under test
			String mainClassTestBoundsName = mainClassName + "TestBounds";
			Class<?> mainClassTestBounds = null;
			try {
				mainClassTestBounds = cl.loadClass(mainClassTestBoundsName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			if (!TOOLCHAIN)
				System.out.println("CLASS: " + mainClassTestBoundsName);
			// use reflection to invoke the TestBounds class to get the usage
			// information from the jar
			Method testBoundsMethod = null;
			testBoundsMethod = mainClassTestBounds.getMethod("testBounds");

			Object mainClassTestBoundsInstance = null;
			mainClassTestBoundsInstance = mainClassTestBounds.newInstance();

			Map<String, Object> mainClassTestBoundsMap = null;
			// Runs the testBounds method
			mainClassTestBoundsMap = (Map<String, Object>) testBoundsMethod.invoke(mainClassTestBoundsInstance);

			// instantiating a new Parameter Factory using the Test Bounds map
			this.parameterFactory = new ParameterFactory(mainClassTestBoundsMap);

			// get a list of basic tests from the TestBounds class
			this.tests = new ArrayList<Test>();
			List testList = (List) mainClassTestBoundsMap.get("tests");
			for (Object inTest : testList) {
				this.tests.add(new Test((Map) inTest));
			}

		} catch (Exception e) {
			// if we have an exception during initialization, display the error
			// to the user and return a false status
			System.out.println("ERROR: An exception occurred during initialization.");
			e.printStackTrace();
			return false;
		}

		// if we did not encounter an exception during initialization, return a
		// true status
		return true;
	}

	/**
	 * This is the half of the framework that IDT has completed. We are able to
	 * pull basic tests directly from the executable jar. We are able to run the
	 * tests and assess the output as PASS/FAIL.
	 * 
	 * You likely do not have to change this part of the framework. We are
	 * considering this complete and want your team to focus more on the
	 * SecurityTests.
	 */
	public void executeBasicTests() {

		int passCount = 0;
		int failCount = 0;

		// iterate through the lists of tests and execute each one
		for (Test test : this.tests) {

			// instrument the code to code coverage metrics, execute the test
			// with given parameters, then show the output
			Output output = instrumentAndExecuteCode(test.getParameters().toArray());
			String result = new String();
			for (Object o : test.getParameters()) {
				result += o.toString();
			}
			outputFile.addBasicTest(new BasicTest(result, output.getStdOutString(), output.getStdErrString(),
					test.getStdOutExpectedResultRegex(), test.getStdErrExpectedResultRegex()));
			if (!TOOLCHAIN)
				printBasicTestOutput(output);

			// determine the result of the test based on expected output/error
			// regex
			if (output.getStdOutString().matches(test.getStdOutExpectedResultRegex())
					&& output.getStdErrString().matches(test.getStdErrExpectedResultRegex())) {
				if (!TOOLCHAIN)
					System.out.println("basic test result: PASS");
				passCount++;
			} else {
				if (!TOOLCHAIN)
					System.out.println("basic test result: FAIL ");
				failCount++;

				// since we have a failed basic test, show the expectation for
				// the stdout
				if (!output.getStdOutString().matches(test.getStdOutExpectedResultRegex())) {
					if (!TOOLCHAIN) {
						System.out.println("\t ->stdout: " + output.getStdOutString());
						System.out.println(
								"\t ->did not match expected stdout regex: " + test.getStdOutExpectedResultRegex());
					}
				}

				// since we have a failed basic test, show the expectation for
				// the stderr
				if (!output.getStdErrString().matches(test.getStdErrExpectedResultRegex())) {
					if (!TOOLCHAIN) {
						System.out.println("\t ->stderr: " + output.getStdErrString());
						System.out.println(
								"\t ->did not match expected stderr regex: " + test.getStdErrExpectedResultRegex());
					}
				}
			}
			if (!TOOLCHAIN)
				System.out.println(HORIZONTAL_LINE);
		}
		// print the basic test results and the code coverage associated with
		// the basic tests
		double percentCovered = generateSummaryCodeCoverageResults();
		outputFile.setPercentCoveredForBasicTests((float) percentCovered);
		if (!TOOLCHAIN) {
			System.out.println("basic test results: " + (passCount + failCount) + " total, " + passCount + " pass, "
					+ failCount + " fail, " + StringFormatter.clip(percentCovered, 2) + " percent covered");
			System.out.println(HORIZONTAL_LINE);
		}
	}

	public void executeSecurityTests() {
		if (!TOOLCHAIN) {
			System.out.println();
			System.out.println("Starting security tests");
		}
		int passCount = 0;
		int failCount = 0;
		long startTime = System.currentTimeMillis();
		long timeToEnd = startTime + TIMEGOAL;
		int testCount = 0;

		/*
		 * There are five stages in our black box testing method for unbounded
		 * jars. Stage 1: 5% of the specified number of tests will test the
		 * typical edge cases of the parameter types one argument at a time
		 * (i.e. for an integer, an edge case may be a negative number) Stage 2:
		 * 5% will test typical edge cases of the parameter types with multiple
		 * arguments at a time Stage 3: 10% of the tests will test the security
		 * tests with inappropriate values in attempt to reach the jar's catch
		 * statements Stage 4: 40% of the tests will test one argument at a time
		 * with an appropriate parameters entered Stage 5: the rest of the tests
		 * will test multiple arguments with appropriate parameters entered
		 */

		/*
		 * For bounded jars there are three stages. Stage 1: 5% of the specified
		 * number of tests will test the typical edge cases of the parameter
		 * types (i.e. for an integer, an edge case may be a negative number)
		 * Stage 2: 10% of the tests will test the security tests with
		 * inappropriate values in attempt to reach the jar's catch statements
		 * Stage 3: the rest of the tests will test multiple arguments with
		 * appropriate parameters entered
		 */

		/*
		 * If the user specified the number of tests but not a time goal or
		 * didn't specify a time goal or number of tests, the stages will be
		 * split based on the number of tests specified or the default number of
		 * tests to be executed (1000). If the user specified a time goal but
		 * did not specify a number of tests, the stages will be split based on
		 * the time.
		 */
		long stageComparer;
		if (STOPATBBTESTS)
			stageComparer = BBTESTS;
		else
			stageComparer = (int) (TIMEGOAL);// total time
		long s1End = (long) (stageComparer * .05);
		long s2End = (long) (stageComparer * .05 + s1End);
		long s3End = (long) (stageComparer * .1 + s2End);
		long s4End = (long) (stageComparer * .4 + s3End);// only used for
															// unbounded jars

		// each row represents a different parameter
		// each row holds a parameter we need to test in the first index, values
		// in the subsequent indexes
		// UPDATE 1/30 - holds a different dependent parameter in each row.
		// subsequent values are values to test. know if dependent if it has a
		// dependent param map
		List<List<Parameter>> allDependentParametersLists = new ArrayList<List<Parameter>>();
		// List<String> previousParameterStrings = new ArrayList<String>();

		// handles dependent
		if (!this.parameterFactory.isBounded()) {
			// gets all enumerated values. The indexes of each enumerated value
			// correspond to the row index of potentialParametersLists.
			// UPDATE 1/30- gets all enumperatedParameters, but will only hold
			// nondependent parameters. the dependent ones will be removed later
			// List<String> enumeratedParameters =
			// this.parameterFactory.getNext(previousParameterStrings).get(0).getEnumerationValues();
			List<String> allNondependentParams = this.parameterFactory.getNext(new ArrayList<String>()).get(0)
					.getEnumerationValues();
			// generateValues(this.parameterFactory.getNext(new
			// ArrayList<String>()).get(0), "appropriate");//formats them as
			// well
			// corresponds to the rows of dependentPotentialParameterLists
			List<String> nondependentParamsToTest = new ArrayList<String>();
			for (String param : allNondependentParams) {
				nondependentParamsToTest.add(generateValue(param, "appropriate"));
			}
			List<String> allDependentParameters = new ArrayList<String>();

			for (int k = allNondependentParams.size() - 1; k >= 0; k--) {
				// need an array list to get the next potential parameter
				ArrayList<String> dummyPrevParamString = new ArrayList<String>();
				dummyPrevParamString.add(allNondependentParams.get(k));
				// puts enumerated value as first index
				List<Parameter> potentialParameters = this.parameterFactory.getNext(dummyPrevParamString);

				// enumerated value is a dependent parameter
				if (!potentialParameters.isEmpty()) {
					allDependentParameters.add(0, allNondependentParams.get(k));
					allDependentParametersLists.add(0, potentialParameters);// adds
																			// the
																			// dependent
																			// parameter
																			// to
																			// a
																			// row

					// dummyPrevParamString used only to find other potential
					// parameters for a dependent parameter
					dummyPrevParamString = new ArrayList<String>();
					dummyPrevParamString.add(allNondependentParams.get(k));// will
																			// be
																			// "--Integer"
																			// or
																			// something
					while (!potentialParameters.isEmpty()) {// make a dummy
															// previous
															// parameter strings
															// using the
															// generate values
															// method?
						// check if its an enumeration. if it is, get the list
						// of enumerations and you can start testing that

						dummyPrevParamString.add(
								generateValue(potentialParameters.get(potentialParameters.size() - 1), "appropriate"));
						potentialParameters = this.parameterFactory.getNext(dummyPrevParamString);
						if (!potentialParameters.isEmpty())
							allDependentParametersLists.get(0).add(potentialParameters.get(0));
					}
					allNondependentParams.remove(k);
				}
			}

			// Parameter stupid = this.parameterFactory.getNext(new
			// ArrayList<String>()).get(0);
			// System.out.println("TEST");
			// System.out.println(generateValues(this.parameterFactory.getNext(new
			// ArrayList<String>()).get(0), "appropriate"));
			// System.out.println(generateValues(stupid, "edge"));

			// just stuff to make sure everything works -- delete later
			// System.out.println("nondependent enums: " +
			// enumeratedParametersNondependent);
			// for(int k = 0; k < dependentPotentialParametersLists.size();
			// k++){
			// System.out.println("type: " +
			// dependentPotentialParametersLists.get(k).get(0).getType());
			// System.out.println("enum: " +
			// enumeratedParametersDependent.get(k));
			// System.out.println("dependentPotentialParameters: " +
			// dependentPotentialParametersLists.get(k));
			// }

			// Parameter nondependentParam = this.parameterFactory.getNext(new
			// ArrayList<String>()).get(0);

			// RANDOM EVERYTHING
			/*
			 * timeGoal will be less than 0 if user did not enter a timeGoal or
			 * bbTests
			 */
			// while there are still more bbTests to run or there is extra time,
			// tests will continue to be performed

			while (STOPATBBTESTS ? (testCount < BBTESTS) : (System.currentTimeMillis() < timeToEnd)) {

				// this list's slots mirror those in
				// dependentPotentialParametersLists but are Objects we can test
				// rather than parameters

				// makes deep copy so we can remove values from them later to
				// test and not repeat any of them
				// List<String> enumeratedParametersNondependentCopy =
				// makeDeepCopy(enumeratedParametersNondependent);
				// System.out.println(enumeratedParametersNondependent + "
				// enumnondependent1");
				// List<String> enumeratedParametersNondependentCopy = new
				// ArrayList<String>();
				// for(String param : enumeratedParametersNondependent){
				// enumeratedParametersNondependentCopy.add(param);
				// }

				int numOfNondependentToTest = 0;
				int numOfDependentToTest = 0;
				// at stage 1 and stage 4, we test 1 argument at a time
				if ((STOPATBBTESTS && (testCount < s1End || testCount > s3End && testCount < s4End))
						|| (!STOPATBBTESTS && (startTime + s1End > System.currentTimeMillis())
								|| (startTime + s3End > System.currentTimeMillis())
										&& (startTime + s4End > System.currentTimeMillis()))) {
					// sometimes test a dependent arg, sometimes test a
					// nondependent arg
					if ((int) (Math.random() * 2) == 0) {
						numOfNondependentToTest = 1;
					} else {
						numOfDependentToTest = 1;
					}
				} else {
					numOfNondependentToTest = (int) (Math.random() * allNondependentParams.size());
					numOfDependentToTest = (int) (Math.random() * allDependentParametersLists.size());
				}

				String paramTestType;
				if (STOPATBBTESTS && s2End > testCount
						|| !STOPATBBTESTS && startTime + s2End > System.currentTimeMillis()) {
					paramTestType = "edge";
					// enumeratedParametersNondependentCopy =
					// generateValues(this.parameterFactory.getNext(new
					// ArrayList<String>()).get(0), "edge");
				} else if (STOPATBBTESTS && s3End > testCount
						|| !STOPATBBTESTS && startTime + s3End > System.currentTimeMillis()) {
					paramTestType = "inappropriate";
					// enumeratedParametersNondependentCopy =
					// generateValues(this.parameterFactory.getNext(new
					// ArrayList<String>()).get(0), "innapropriate");
				} else {
					paramTestType = "appropriate";
					// enumeratedParametersNondependentCopy =
					// generateValues(this.parameterFactory.getNext(new
					// ArrayList<String>()).get(0), "appropriate");
				}
				// TODO: replace patterns here
				// why is it not a deep copy
				//// enumeratedParametersNondependentCopy =
				// generateValues(this.parameterFactory.getNext(new
				// ArrayList<String>()).get(0),paramTestType);
				// this wouldn't even work because it would return all of the
				// enumerated parameters

				// replaces any formatted nondependent parameter args
				// List<String> enumeratedParametersNondependentCopy = new
				// ArrayList<String>();
				nondependentParamsToTest.clear();
				for (String param : allNondependentParams) {
					nondependentParamsToTest.add(generateValue(param, paramTestType));
				}

				List<Object> parameters = new ArrayList<Object>();
				// nondependent
				for (int k = 0; k < numOfNondependentToTest && allNondependentParams.size() != 0; k++) {
					// removes a nondependent parameter at a random index and
					// adds it to parameters
					parameters.add(
							nondependentParamsToTest.remove((int) (Math.random() * nondependentParamsToTest.size())));
				}

				// dependent
				List<List<Object>> dependentParametersListsToTest = new ArrayList<List<Object>>();
				// fills dependentParameters with random, appropriate values
				for (int r = 0; r < allDependentParametersLists.size(); r++) {
					dependentParametersListsToTest.add(new ArrayList<Object>());
					for (int c = 0; c < allDependentParametersLists.get(r).size(); c++) {
						dependentParametersListsToTest.get(r)
								.add(generateValue(allDependentParametersLists.get(r).get(c), paramTestType));
					}
				}

				//formats any of the arguments that need formatting
				List<String> dependentParametersToTest = new ArrayList<String>();
				for (String param : allDependentParameters) {
					dependentParametersToTest.add(generateValue(param, paramTestType));
				}
				// List<String> enumeratedParametersDependentCopy = new
				// ArrayList<String>();
				// for(String param : enumeratedParametersDependent){
				// enumeratedParametersDependentCopy.add(param);
				// }

				for (int k = 0; k < numOfDependentToTest && dependentParametersListsToTest.size() != 0; k++) {
					int row = (int) (Math.random() * dependentParametersToTest.size());
					parameters.add(dependentParametersToTest.get(row));
					//System.out.println(dependentParametersToTest.get(row) + "row");
					// removes a dependent parameter at a random index and adds
					// it to parameters
					for (int c = 0; c < allDependentParametersLists.get(row).size(); c++) {
						// if its an enumerated value, it will add a random enumeration
						//System.out.println("row and types" +generateValue(allDependentParametersLists.get(row).get(c), paramTestType));
						parameters.add(generateValue(dependentParametersListsToTest.get(row).get(c), paramTestType));
					}
					dependentParametersToTest.remove(row);
					dependentParametersListsToTest.remove(row);
				}

				if (allDependentParameters.size() == 0)
					parameters.add((new StringRange()).random(paramTestType));
				if (executeAndPrintResults(parameters.toArray(), paramTestType))
					passCount++;
				else
					failCount++;
				testCount++;
			}

		}

		// handles fixed
		else {

			String paramTestType;
			List<Parameter> fixedParameters = this.parameterFactory.getFixedParametersList();
			Object[] parameters = new Object[fixedParameters.size()];

			// starting tests
			while (STOPATBBTESTS ? (testCount < BBTESTS) : (System.currentTimeMillis() < timeToEnd)) {

				if (STOPATBBTESTS && s2End > testCount
						|| !STOPATBBTESTS && startTime + s2End > System.currentTimeMillis()) {
					paramTestType = "edge";
				} else if (STOPATBBTESTS && s3End > testCount
						|| !STOPATBBTESTS && startTime + s3End > System.currentTimeMillis()) {
					paramTestType = "inappropriate";
				} else {
					paramTestType = "appropriate";
				}

				for (int k = 0; k < parameters.length; k++) {
					parameters[k] = generateValue(fixedParameters.get(k), paramTestType);
				}
				if (executeAndPrintResults(parameters, paramTestType))
					passCount++;
				else
					failCount++;
				testCount++;
			}

		}
		double percentCovered = generateSummaryCodeCoverageResults();
		if (!TOOLCHAIN) {
			System.out.println("security test results: " + (passCount + failCount) + " total, " + passCount + " pass, "
					+ failCount + " fail, " + StringFormatter.clip(percentCovered, 2) + " percent covered");
			System.out.println(HORIZONTAL_LINE);
		}

		// YAML output
		System.out.println("Total predefined tests run: " + testCount);
		System.out.println("Number of predefined tests that passed: " + passCount);
		System.out.println("Number of predefined tests that failed: " + failCount);
		System.out.println("Total code coverage percentage: " + percentCovered);
		System.out.println("Unique error count: " + errors.size());
		System.out.println("Errors seen:");
		for (String err : errors)
			System.out.println("\t-" + err);
		// showCodeCoverageResultsExample();

	}

	/**
	 * Method will return an List of values that corresponds to the type and
	 * passed into the method.
	 * 
	 * @param parameter
	 *            is the parameter that the value(s) will be generated from.
	 * @param testParamType
	 *            is the type of value that should be generated. type will have
	 *            the value of either "edge" for edge cases, "inappropriate" for
	 *            a value that is not of the same type of the parameter's type,
	 *            or "appropriate" for a value that is the same type of the
	 *            parameter's type.
	 * 
	 * @returns a list containing one value to be executed as a parameter. If
	 *          the parameter is enumerated, it returns a list of enumerated
	 *          values
	 */
	public String generateValue(Object objectParameter, String paramTestType) {
		// make a range object later?
		// List<String> previousParameterStrings = new ArrayList<String>(); //
		// start with a blank parameter list since we are going to start with
		// the first parameter
		// List<Parameter> potentialParameters =
		// this.parameterFactory.getNext(previousParameterStrings);
		// Parameter potentialParameter = potentialParameters.get(0);
		// List<String> parameterStrings = new ArrayList<String>();
		String parameterString = "";
		if (objectParameter instanceof Parameter) {
			Parameter parameter = (Parameter) objectParameter;
			// if a parameter is optional, randomly decide whether to use it or
			// not
			if (parameter.isOptional() && (int) (Math.random() * 2) == 1) {
				return "";
			}
			// if parameter is an enumeration, the method returns the enumerated
			// values
			if (parameter.isEnumeration()) {// if the parameter is an
											// enumeration, the execute security
											// tests method should have already
											// known this and should plan
											// accordingly
				String enumeratedValue = parameter.getEnumerationValues().get(parameter.getEnumerationValues().size());
				// parameterStrings = parameter.getEnumerationValues().get(0) +
				// " "; // dumb logic - given a list of options, always use the
				// first one

				// if the parameter has internal format (eg.
				// "<number>:<number>PM EST")
				if (parameter.isFormatted()) {
					// loop over the areas of the format that must be replaced
					// with values
					List<Object> formatVariableValues = new ArrayList<Object>();
					for (Class type : parameter.getFormatVariables(enumeratedValue)) {
						if (type == Integer.class) {
							Range range = new IntRange();
							formatVariableValues.add(range.random(paramTestType));
						} else if (type == String.class) {
							Range range = new StringRange();
							formatVariableValues.add(range.random(paramTestType));
						}
					}
					// build the formatted parameter string with the chosen
					// values (eg. 1:1PM EST)
					enumeratedValue = parameter.getFormattedParameter(enumeratedValue, formatVariableValues);

					/*
					 * parameterString = parameter.getFormattedParameter(
					 * parameterString, formatVariableValues);
					 */

					// previousParameterStrings.add(parameterString);
				}
				return enumeratedValue;
				// if it is not an enumeration parameter, it is either an
				// Integer, Double, or String
			} else {
				if (parameter.getType() == Integer.class) {
					IntRange range = new IntRange();
					if (parameter.getMax() != null) {
						range.setMax((Integer) parameter.getMax());
					}
					if (parameter.getMin() != null) {
						range.setMin((Integer) parameter.getMin());
					}
					parameterString = range.random(paramTestType) + " ";
				} else if (parameter.getType() == Double.class) {
					DoubleRange range = new DoubleRange();
					if (parameter.getMax() != null) {
						range.setMax((Double) parameter.getMax());
					}
					if (parameter.getMin() != null) {
						range.setMin((Double) parameter.getMin());
					}
					parameterString = range.random(paramTestType) + " "; 
				} else if (parameter.getType() == String.class) {
					Range range = new StringRange();

					// if the parameter has internal format (eg.
					// "<number>:<number>PM EST")
					if (parameter.isFormatted()) {

						// loop over the areas of the format that must be
						// replaced and choose values
						List<Object> formatVariableValues = new ArrayList<Object>();
						for (Class type : parameter.getFormatVariables()) {
							if (type == Integer.class) {
								Range intRange = new IntRange();
								formatVariableValues.add(intRange.random(paramTestType)); 
							} else if (type == String.class) {
								formatVariableValues.add(range.random(paramTestType)); 
							}
						}
						// build the formatted parameter string with the chosen
						// values (eg. 1:1PM EST)
						parameterString = parameter.getFormattedParameter(formatVariableValues);
					} else {
						parameterString = range.random(paramTestType) + ""; 
					}

				} else {
					parameterString = "unknown type";// should we do something
														// about this
				}
			}
		}
		// formatted parameters to be reformatted
		else if (objectParameter instanceof String) {
			parameterString = (String) objectParameter;
			Range range;
			while (parameterString.indexOf("<<REPLACE_ME_INT>>") != -1) {
				range = new IntRange();
				parameterString = parameterString.substring(0, parameterString.indexOf("<<REPLACE_ME_INT>>"))
						+ range.random(paramTestType)
						+ parameterString.substring(parameterString.indexOf("<<REPLACE_ME_INT>>") + 18);
			}
			while (parameterString.indexOf("<<REPLACE_ME_STRING>>") != -1) {
				range = new StringRange();
				parameterString = parameterString.substring(0, parameterString.indexOf("<<REPLACE_ME_STRING>>"))
						+ range.random(paramTestType)
						+ parameterString.substring(parameterString.indexOf("<<REPLACE_ME_STRING>>") + 21);
			}
		}
		return parameterString;
	}

	/**
	 * Method will execute an individual security test and print its results.
	 * 
	 * @param toTest
	 *            is the array of parameters to be passed into the
	 *            instrumentAndExecuteCode method
	 * @param paramTestType
	 *            is a String that represents whether the values generated for
	 *            the security test were appropriate or an edge case, meaning
	 *            they should not have an exception, or inappropriate, meaning
	 *            that an exception was expected to be generated.
	 * 
	 * @return true if the security test passed, false if not. A security test
	 *         passes if no exceptions occur or if an exception was expected and
	 *         generated
	 */
	public boolean executeAndPrintResults(Object[] toTest, String paramTestType) {
		Output output = instrumentAndExecuteCode(toTest);
		String result = new String();
		for (Object o : toTest) {
			result += o.toString();
		}
		float covered = (float) generateSummaryCodeCoverageResults();
		outputFile
				.addSecurityTest(new SecurityTest(result, output.getStdOutString(), output.getStdErrString(), covered));
		if (!TOOLCHAIN)
			printBasicTestOutput(output);
		boolean passed = false;
		if (paramTestType.equals("appropriate") || paramTestType.equals("edge")) {
			if (output.getStdErrString().indexOf("Exception") != -1) {
				passed = false;
			} else {
				passed = true;
			}
		} else {// if error was expected, the security test passes no matter
				// what
			passed = true;
		}
		// if error is unique, adds it to the errors arrayList
		if (!output.getStdErrString().equals("")) {
			boolean uniqueError = true;
			for (int k = 0; k < errors.size(); k++) {
				if (output.getStdErrString().equals(errors.get(k))) {
					uniqueError = false;
					break;
				}
			}
			if (uniqueError)
				errors.add(output.getStdErrString());
		}
		if (!TOOLCHAIN) {// should this stuff be printed in the exec.sec test
							// method?
			System.out.print("security test result: ");
			if (passed)
				System.out.println("PASSED");
			else
				System.out.println("FAILED");
			System.out.println(HORIZONTAL_LINE);
		}
		return passed;
	}

	public OHSFile getOHSFile() {
		return outputFile;
	}

	//////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////

	/**
	 * This method will instrument and execute the jar under test with the
	 * supplied parameters. This method should be used for both basic tests and
	 * security tests.
	 * 
	 * An assumption is made in this method that the word java is recognized on
	 * the command line because the user has already set the appropriate
	 * environment variable path.
	 * 
	 * @param parameters
	 *            - array of Objects that represents the parameter values to use
	 *            for this execution of the jar under test
	 * 
	 * @return Output representation of the standard out and standard error
	 *         associated with the run
	 */
	private Output instrumentAndExecuteCode(Object[] parameters) {

		Process process = null;
		Output output = null;

		// we are building up a command line statement that will use java -jar
		// to execute the jar
		// and uses jacoco to instrument that jar and collect code coverage
		// metrics
		String command = "java";
		try {
			command += " -javaagent:" + this.jacocoAgentJarPath + "=destfile=" + this.jacocoOutputFilePath;
			command += " -jar " + this.jarToTestPath;
			for (Object o : parameters) {
				command += " " + o.toString();
			}

			// show the user the command to run and prepare the process using
			// the command
			if (!TOOLCHAIN)
				System.out.println("command to run: " + command);
			process = Runtime.getRuntime().exec(command);

			// prepare the stream needed to capture standard output
			InputStream isOut = process.getInputStream();
			InputStreamReader isrOut = new InputStreamReader(isOut);
			BufferedReader brOut = new BufferedReader(isrOut);
			StringBuffer stdOutBuff = new StringBuffer();

			// prepare the stream needed to capture standard error
			InputStream isErr = process.getErrorStream();
			InputStreamReader isrErr = new InputStreamReader(isErr);
			BufferedReader brErr = new BufferedReader(isrErr);
			StringBuffer stdErrBuff = new StringBuffer();

			String line;
			boolean outDone = false;
			boolean errDone = false;

			// while standard out is not complete OR standard error is not
			// complete
			// continue to probe the output/error streams for the applications
			// output
			while (!outDone || !errDone) {

				// monitoring the standard output from the application
				boolean outReady = true;
				if (outReady) {
					line = brOut.readLine();
					if (line == null) {
						outDone = true;
					} else {
						stdOutBuff.append(line);
					}
				}

				// monitoring the standard error from the application
				boolean errReady = true;
				if (errReady) {
					line = brErr.readLine();
					if (line == null) {
						errDone = true;
					} else {
						stdErrBuff.append(line);
					}
				}

				// if standard out and standard error are not ready, wait for
				// 5ms
				// and try again to monitor the streams
				if (!outReady && !errReady) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						// NOP
					}
				}
			}

			// we now have the output as an object from the run of the black-box
			// jar
			// this output object contains both the standard output and the
			// standard error
			output = new Output(stdOutBuff.toString(), stdErrBuff.toString());

		} catch (IOException e) {
			System.out.println("ERROR: IOException has prevented execution of the command: " + command);
		}

		return output;
	}

	/**
	 * Method used to print the basic test output (std out/err)
	 * 
	 * @param output
	 *            - Output object containing std out/err to print
	 */
	private void printBasicTestOutput(Output output) {
		System.out.println("stdout of execution: " + output.getStdOutString());
		System.out.println("stderr of execution: " + output.getStdErrString());
	}

	/**
	 * Method used to print raw code coverage stats including hits/probes
	 * 
	 * @throws IOException
	 */
	private void printRawCoverageStats() {
		System.out.printf("exec file: %s%n", this.jacocoOutputFilePath);
		System.out.println("CLASS ID         HITS/PROBES   CLASS NAME");

		try {
			File executionDataFile = new File(this.jacocoOutputFilePath);
			final FileInputStream in = new FileInputStream(executionDataFile);
			final ExecutionDataReader reader = new ExecutionDataReader(in);
			reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
				public void visitSessionInfo(final SessionInfo info) {
					System.out.printf("Session \"%s\": %s - %s%n", info.getId(), new Date(info.getStartTimeStamp()),
							new Date(info.getDumpTimeStamp()));
				}
			});
			reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
				public void visitClassExecution(final ExecutionData data) {
					System.out.printf("%016x  %3d of %3d   %s%n", Long.valueOf(data.getId()),
							Integer.valueOf(getHitCount(data.getProbes())), Integer.valueOf(data.getProbes().length),
							data.getName());
				}
			});
			reader.read();
			in.close();
		} catch (IOException e) {
			System.out.println(
					"Unable to display raw coverage stats due to IOException related to " + this.jacocoOutputFilePath);
		}
		System.out.println();
	}

	/**
	 * Method used to get hit count from the code coverage metrics
	 * 
	 * @param data
	 *            - boolean array of coverage data where true indicates hits
	 * @return int representation of count of total hits from supplied data
	 */
	private int getHitCount(final boolean[] data) {
		int count = 0;
		for (final boolean hit : data) {
			if (hit) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Method for generating code coverage metrics including instructions,
	 * branches, lines, methods and complexity.
	 * 
	 * @return double representation of the percentage of code covered during
	 *         testing
	 */
	private double generateSummaryCodeCoverageResults() {
		double percentCovered = 0.0;
		long total = 0;
		long covered = 0;
		try {
			// creating a new file for output in the jacoco output directory
			// (one of the application arguments)
			File executionDataFile = new File(this.jacocoOutputFilePath);
			ExecFileLoader execFileLoader = new ExecFileLoader();
			execFileLoader.load(executionDataFile);

			// use CoverageBuilder and Analyzer to assess code coverage from
			// jacoco output file
			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);

			// analyzeAll is the way to go to analyze all classes inside a
			// container (jar or zip or directory)
			analyzer.analyzeAll(new File(this.jarToTestPath));

			for (final IClassCoverage cc : coverageBuilder.getClasses()) {

				// report code coverage from all classes that are not the
				// TestBounds class within the jar
				if (!cc.getName().endsWith("TestBounds")) {
					total += cc.getInstructionCounter().getTotalCount();
					total += cc.getBranchCounter().getTotalCount();
					total += cc.getLineCounter().getTotalCount();
					total += cc.getMethodCounter().getTotalCount();
					total += cc.getComplexityCounter().getTotalCount();

					covered += cc.getInstructionCounter().getCoveredCount();
					covered += cc.getBranchCounter().getCoveredCount();
					covered += cc.getLineCounter().getCoveredCount();
					covered += cc.getMethodCounter().getCoveredCount();
					covered += cc.getComplexityCounter().getCoveredCount();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		percentCovered = ((double) covered / (double) total) * 100.0;
		return percentCovered;
	}

	/**
	 * This method shows an example of how to generate code coverage metrics
	 * from Jacoco
	 * 
	 * @return String representing code coverage results
	 */
	private String generateDetailedCodeCoverageResults() {
		String executionResults = "";
		try {
			File executionDataFile = new File(this.jacocoOutputFilePath);
			ExecFileLoader execFileLoader = new ExecFileLoader();
			execFileLoader.load(executionDataFile);

			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);

			analyzer.analyzeAll(new File(this.jarToTestPath));

			for (final IClassCoverage cc : coverageBuilder.getClasses()) {
				executionResults += "Coverage of class " + cc.getName() + ":\n";
				executionResults += getMetricResultString("instructions", cc.getInstructionCounter());
				executionResults += getMetricResultString("branches", cc.getBranchCounter());
				executionResults += getMetricResultString("lines", cc.getLineCounter());
				executionResults += getMetricResultString("methods", cc.getMethodCounter());
				executionResults += getMetricResultString("complexity", cc.getComplexityCounter());

				// adding this to a string is a little impractical with the size
				// of some of the files,
				// so we are commenting it out, but it shows that you can get
				// the coverage status of each line
				// if you wanted to add debug argument to display this level of
				// detail at command line level....
				//
				// for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
				// executionResults += "Line " + Integer.valueOf(i) + ": " +
				// getStatusString(cc.getLine(i).getStatus()) + "\n";
				// }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return executionResults;
	}

	/**
	 * Method to translate the Jacoco line coverage status integers to Strings.
	 * 
	 * @param status
	 *            - integer representation of line coverage status provided by
	 *            Jacoco
	 * @return String representation of line coverage status (not covered,
	 *         partially covered, fully covered)
	 */
	@SuppressWarnings("unused")
	private String getStatusString(final int status) {
		switch (status) {
		case ICounter.NOT_COVERED:
			return "not covered";
		case ICounter.PARTLY_COVERED:
			return "partially covered";
		case ICounter.FULLY_COVERED:
			return "fully covered";
		}
		return "";
	}

	/**
	 * Method to translate the counter data and units into a human readable
	 * metric result String
	 * 
	 * @param unit
	 * @param counter
	 * @return
	 */
	private String getMetricResultString(final String unit, final ICounter counter) {
		final Integer missedCount = Integer.valueOf(counter.getMissedCount());
		final Integer totalCount = Integer.valueOf(counter.getTotalCount());
		return missedCount.toString() + " of " + totalCount.toString() + " " + unit + " missed\n";
	}

	/**
	 * This method is not meant to be part of the final framework. It was
	 * included to demonstrate three different ways to tap into the code
	 * coverage results/metrics using jacoco.
	 * 
	 * This method is deprecated and will be removed from the final product
	 * after your team completes development. Please do not add additional
	 * dependencies to this method.
	 */
	@Deprecated
	private void showCodeCoverageResultsExample() {

		// Below is the first example of how to tap into code coverage metrics
		double result = generateSummaryCodeCoverageResults();
		System.out.println("\n");
		System.out.println("percent covered: " + StringFormatter.clip(result, 2));

		// Below is the second example of how to tap into code coverage metrics
		System.out.println("\n");
		printRawCoverageStats();

		// Below is the third example of how to tap into code coverage metrics
		System.out.println("\n");
		System.out.println(generateDetailedCodeCoverageResults());
	}

}
