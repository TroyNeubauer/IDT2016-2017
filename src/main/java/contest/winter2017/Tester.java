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
@SuppressWarnings("all")
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

	private final OHSFile outputFile;


	public Tester(int bbTests, long timeGoal, boolean toolChain) {
		
		this.outputFile = new OHSFile();
		outputFile.setTimestamp(System.currentTimeMillis());
		this.BBTESTS = bbTests;
		this.TIMEGOAL = timeGoal;
		this.TOOLCHAIN = toolChain;

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
		this.jacocoOutputFilePath = this.jacocoOutputDirPath + "\\" + jarFileToTest.getName().replaceAll("\\.", "_") + JACOCO_OUTPUT_FILE_SUFFIX;

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
			if (!TOOLCHAIN) System.out.println("CLASS: " + mainClassTestBoundsName);
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
		StringBuffer resultToPrint = new StringBuffer();

		if (!TOOLCHAIN) {
			System.out.println("Starting Basic Tests\n");
		}

		// iterate through the lists of tests and execute each one
		for (Test test : this.tests) {

			// instrument the code to code coverage metrics, execute the test
			// with given parameters, then show the output
			Output output = instrumentAndExecuteCode(test.getParameters().toArray());
			String result = new String();
			for (Object o : test.getParameters()) {
				result += o.toString();
			}
			outputFile.addBasicTest(new BasicTest(result, output.getStdOutString(), output.getStdErrString(), test.getStdOutExpectedResultRegex(),
					test.getStdErrExpectedResultRegex()));

			resultToPrint.append(printBasicTestOutput(output));

			// determine the result of the test based on expected output/error
			// regex
			if (output.getStdOutString().matches(test.getStdOutExpectedResultRegex())
					&& output.getStdErrString().matches(test.getStdErrExpectedResultRegex())) {
				resultToPrint.append("basic test result: PASS\n");
				passCount++;
			} else {
				resultToPrint.append("basic test result: FAIL\n");
				failCount++;

				// since we have a failed basic test, show the expectation for
				// the stdout
				if (!output.getStdOutString().matches(test.getStdOutExpectedResultRegex())) {

					resultToPrint.append("\t ->stdout: " + output.getStdOutString() + "\n" + "\t ->did not match expected stdout regex: "
							+ test.getStdOutExpectedResultRegex() + "\n");

				}

				// since we have a failed basic test, show the expectation for
				// the stderr
				if (!output.getStdErrString().matches(test.getStdErrExpectedResultRegex())) {
					resultToPrint.append("\t ->stderr: " + output.getStdErrString() + "\n" + "\t ->did not match expected stderr regex: "
							+ test.getStdErrExpectedResultRegex() + "\n");
				}
			}
			if (!TOOLCHAIN) {
				System.out.print(resultToPrint);
				System.out.println(HORIZONTAL_LINE);
			}
			resultToPrint.delete(0, resultToPrint.length());
		}
		// print the basic test results and the code coverage associated with
		// the basic tests
		double percentCovered = generateSummaryCodeCoverageResults();
		if (!TOOLCHAIN) {
			System.out.println("basic test results: " + (passCount + failCount) + " total, " + passCount + " pass, " + failCount + " fail, "
					+ StringFormatter.clip(percentCovered, 2) + " percent covered");
			System.out.println(HORIZONTAL_LINE);
		}
	}

	public void executeSecurityTests(boolean hasTimeGoal, boolean hasBBTests, boolean stopAtBBTests) {
		if (!TOOLCHAIN) {
			if(Main.DEBUG) {
				if(hasTimeGoal && !stopAtBBTests) System.out.println("stopping after " + (TIMEGOAL / 60 / 1000) + " minutes");
				else if(stopAtBBTests)System.out.println("stopping at " + BBTESTS + " security tests");
				
			}
			System.out.println();
			System.out.println("Starting security tests\n");
		}
		int passCount = 0;
		int failCount = 0;
		long startTime = System.currentTimeMillis();
		long timeToEnd = startTime + TIMEGOAL;
		int testCount = 0;
		Map<String, Integer> errors = new HashMap<String, Integer>();

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
		if (stopAtBBTests) stageComparer = BBTESTS;
		else stageComparer = TIMEGOAL;// total time
		long s1End = (long) (stageComparer * .05);
		long s2End = (long) (stageComparer * .05 + s1End);
		long s3End = (long) (stageComparer * .1 + s2End);
		long s4End = (long) (stageComparer * .4 + s3End);// only used for
															// unbounded jars
		// holds fixed parameters
		List<Parameter> fixedParameters = this.parameterFactory.getFixedParametersList();

		// will hold info for dependent parameters
		// Each row will hold the nondependent parameters for a different
		// dependent parameter argument.
		List<String> allNondependentParams = new ArrayList<String>();
		// will hold a dependent parameter argument at each index.
		List<String> allDependentParams = new ArrayList<String>();
		// Each row will hold the dependent parameters for a different
		// dependent parameter argument.
		// the rows of allDependentParameterLists correspond the indexes of
		// allDependentParameters
		List<List<Parameter>> allDependentParamsLists = new ArrayList<List<Parameter>>();
		// copies allNondependentParams and replaces all of the formatted
		// values
		List<String> nondependentParamsToTest = new ArrayList<String>();
		if (!this.parameterFactory.isBounded()) {

			allNondependentParams = this.parameterFactory.getNext(new ArrayList<String>()).get(0).getEnumerationValues();

			for (String param : allNondependentParams) {
				nondependentParamsToTest.add(generateValue(param, "appropriate"));
			}

			for (int k = allNondependentParams.size() - 1; k >= 0; k--) {
				// uses dummyPrevParamString to get the next potential parameter
				ArrayList<String> dummyPrevParamString = new ArrayList<String>();
				dummyPrevParamString.add(allNondependentParams.get(k));
				// gets a next possible parameter argument
				List<Parameter> potentialParameters = this.parameterFactory.getNext(dummyPrevParamString);

				// enumerated value is a dependent parameter
				if (!potentialParameters.isEmpty()) {
					allDependentParams.add(0, allNondependentParams.get(k));
					// adds the dependent parameter to a row in
					// allDependentParamsLists
					allDependentParamsLists.add(0, potentialParameters);

					// dummyPrevParamString used only to find other potential
					// parameters for a dependent parameter
					dummyPrevParamString = new ArrayList<String>();
					dummyPrevParamString.add(allNondependentParams.get(k));

					while (!potentialParameters.isEmpty()) {
						dummyPrevParamString.add(generateValue(potentialParameters.get(potentialParameters.size() - 1), "appropriate"));
						potentialParameters = this.parameterFactory.getNext(dummyPrevParamString);
						if (!potentialParameters.isEmpty()) allDependentParamsLists.get(0).add(potentialParameters.remove(0));// CHANGED
																																// GET
																																// TO
																																// REMOVE
					}
					// if the enumerator is dependent, it is taken out of the
					// allNondependentParams array.
					allNondependentParams.remove(k);
				}
			}
		} 

		// STARTING TESTS
		// while there are still more bbTests to run or the correct number
		// of bbTests have been run
		// but we have not reached the time goal, tests will continue to be
		// performed.
		String paramTestType = "";
		ArrayList<String> parameters = new ArrayList<String>();// parameters to
		// Used with modulus to determine the next test case
		while (stopAtBBTests ? (testCount < BBTESTS || hasTimeGoal && System.currentTimeMillis() < timeToEnd) : (System.currentTimeMillis() < timeToEnd)) {

			if (!TOOLCHAIN) {
				System.out.println("Security Test #" + (testCount + 1));
				if(stopAtBBTests && testCount > BBTESTS && hasTimeGoal && System.currentTimeMillis() < timeToEnd){
					System.out.println("(running additional tests)");
				}
			}

			// changes what type of values should be generated based on
			// the current stage of the test
			if (stopAtBBTests && s2End > testCount || !stopAtBBTests && startTime + s2End > System.currentTimeMillis()) {
				paramTestType = "edge";
			} else if (stopAtBBTests && s3End > testCount || !stopAtBBTests && startTime + s3End > System.currentTimeMillis()) {
				paramTestType = "inappropriate";
			} else {
				paramTestType = "appropriate";
			}

			if (!this.parameterFactory.isBounded()) {
				int numOfNondependentToTest = 0;
				int numOfDependentToTest = 0;
				// at stage 1 and stage 4, we test 1 argument at a time
				if ((stopAtBBTests && (testCount < s1End || testCount > s3End && testCount < s4End))
						|| (!stopAtBBTests && (startTime + s1End > System.currentTimeMillis())
								|| (startTime + s3End > System.currentTimeMillis()) && (startTime + s4End > System.currentTimeMillis()))) {
					// sometimes test a dependent arg, sometimes test one
					// nondependent arg
					if (testCount % 2 == 0) {
						numOfNondependentToTest = 1;
					} else {
						numOfDependentToTest = 1;
					}
				} else {
					numOfNondependentToTest = (int) (Math.random() * allNondependentParams.size());
					numOfDependentToTest = (int) (Math.random() * allDependentParamsLists.size());
				}

				nondependentParamsToTest.clear();
				for (String param : allNondependentParams) {
					nondependentParamsToTest.add(generateValue(param, paramTestType));
				}

				// handling nondependentparameters
				// removes a nondependent parameter at a random index and
				// adds its value to parameters
				for (int k = 0; k < numOfNondependentToTest && allNondependentParams.size() != 0; k++) {
					parameters.add(nondependentParamsToTest.remove((int) (Math.random() * nondependentParamsToTest.size())));
				}

				// handling dependent parameters
				List<List<Object>> dependentParametersListsToTest = new ArrayList<List<Object>>();
				// fills dependentParametersListsToTest with values
				for (int r = 0; r < allDependentParamsLists.size(); r++) {
					dependentParametersListsToTest.add(new ArrayList<Object>());
					for (int c = 0; c < allDependentParamsLists.get(r).size(); c++) {
						dependentParametersListsToTest.get(r).add(generateValue(allDependentParamsLists.get(r).get(c), paramTestType));
					}
				}

				// formats any of the arguments that need formatting
				List<String> dependentParametersToTest = new ArrayList<String>();
				for (String param : allDependentParams) {
					dependentParametersToTest.add(generateValue(param, paramTestType));
				}

				// adds nondependent parameter values to the parameters
				// arrayList
				for (int k = 0; k < numOfDependentToTest && dependentParametersListsToTest.size() != 0; k++) {
					int row = (int) (Math.random() * dependentParametersToTest.size());
					parameters.add(dependentParametersToTest.get(row));
					// removes a dependent parameter at a random index and
					// adds it to parameters
					for (int c = 0; c < allDependentParamsLists.get(row).size(); c++) {
						// if its an enumerated value, it will add a random
						// enumeration
						parameters.add(generateValue(dependentParametersListsToTest.get(row).get(c), paramTestType));
					} 
					dependentParametersToTest.remove(row);
					dependentParametersListsToTest.remove(row);
				}

				// if there are no nondependent parameters, we pass in a
				// random string
				if (allDependentParams.size() == 0) parameters.add((new StringRange()).random(paramTestType));
			} else {// handles fixed parameters
				for (int k = 0; k < this.parameterFactory.getFixedParametersList().size(); k++) {
					parameters.add(generateValue(fixedParameters.get(k), paramTestType));
				}
			}

			// executes the security test
			Output output = instrumentAndExecuteCode(parameters.toArray());
			String result = new String();
			for (Object o : parameters.toArray()) {
				result += o.toString();
			}
			float covered = (float) generateSummaryCodeCoverageResults();
			outputFile.addSecurityTest(new SecurityTest(result, output.getStdOutString(), output.getStdErrString(), covered));

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
			if (output.getStdErrString().indexOf("Exception") != -1) {
				String error = Utils.getErrorType(output.getStdErrString());
				if(!error.isEmpty()) {
					if (!errors.containsKey(error)) {
						errors.put(error, 1);
					} else {
						int count = errors.get(error);
						errors.replace(error, count, count + 1);
					}
				}
			}
			if (!TOOLCHAIN) {
				System.out.print(printBasicTestOutput(output));
				System.out.print("security test result: ");
				if (passed) {
					System.out.println("PASSED");
				} else {
					System.out.println("FAILED");
				}
				System.out.println(HORIZONTAL_LINE);
			}
			if (passed) passCount++;
			else failCount++;
			parameters.clear();
			testCount++;
		}
		finish(testCount, passCount, failCount, errors);

	}

	private void finish(int testCount, int passCount, int failCount, Map<String, Integer> errors) {
		double percentCovered = generateSummaryCodeCoverageResults();
		if (!TOOLCHAIN) {

			// tap into code coverage metrics
			System.out.println(generateDetailedCodeCoverageResults());
			System.out.println(HORIZONTAL_LINE + "\n");

		}

		outputFile.setTotalPercentCovered((float) percentCovered);

		// YAML output
		System.out.println("Total predefined tests run: " + testCount);
		System.out.println("Number of predefined tests that passed: " + passCount);
		System.out.println("Number of predefined tests that failed: " + failCount);
		System.out.println("Total code coverage percentage: " + StringFormatter.clip(percentCovered, 2));
		int uniqueErrorCount = 0;
		for(Integer count : errors.values()) {
			uniqueErrorCount += count.intValue();
		}
		System.out.println("Unique error count: " + uniqueErrorCount);
		System.out.println("Errors seen:");
		for (String err : errors.keySet())
			System.out.println("\t-" + err);
	}

	/**
	 * Method will return a values that corresponds to the type and passed into
	 * the method.
	 * 
	 * @param objectParameter
	 *            is the parameter that the value will be generated from.
	 * @param testParamType
	 *            is the type of value that should be generated. type will have
	 *            the value of either "edge" for edge cases, "inappropriate" for
	 *            a value that is not of the same type of the parameter's type,
	 *            or "appropriate" for a value that is the same type of the
	 *            parameter's type.
	 * 
	 * @returns a value to be executed as a parameter based on the parameter and
	 *          the paramTestType.
	 */
	public String generateValue(Object objectParameter, String paramTestType) {
		String parameterString = "";
		if (objectParameter instanceof Parameter) {
			Parameter parameter = (Parameter) objectParameter;
			// if a parameter is optional, randomly decide whether to use it or
			// not
			if (parameter.isOptional() && (int) (Math.random() * 2) == 1) { return ""; }
			// If the parameter is enumerated, returns a random, formatted,
			// enumerated value
			if (parameter.isEnumeration()) {
				String enumeratedValue = parameter.getEnumerationValues().get(parameter.getEnumerationValues().size() - 1);

				// if the parameter has internal format (eg."<number>:<number>PM
				// EST")
				if (parameter.isFormatted()) {
					// loop over the areas of the format that must be replaced
					// with values
					List<Object> formatVariableValues = new ArrayList<Object>();
					for (Class type : parameter.getFormatVariables(enumeratedValue)) {
						if (type == Integer.class) {
							formatVariableValues.add(generateValue(parameter, paramTestType, "Integer"));
						} else if (type == String.class) {
							formatVariableValues.add(generateValue(parameter, paramTestType, "String"));
						}
					}
					// build the formatted parameter string with the chosen
					// values (eg. 1:1PM EST)
					enumeratedValue = parameter.getFormattedParameter(enumeratedValue, formatVariableValues);
				}
				return enumeratedValue;
				// if it is not an enumeration parameter, it is either an
				// Integer, Double, or String
			} else {
				if (parameter.getType() == Integer.class) {
					parameterString = generateValue(parameter, paramTestType, "Integer") + " ";
				} else if (parameter.getType() == Double.class) {
					parameterString = generateValue(parameter, paramTestType, "Double") + " ";
				} else if (parameter.getType() == String.class) {

					// if the parameter has internal format (eg.
					// "<number>:<number>PM EST")
					if (parameter.isFormatted()) {

						// loop over the areas of the format that must be
						// replaced and choose values
						List<Object> formatVariableValues = new ArrayList<Object>();
						for (Class type : parameter.getFormatVariables()) {
							if (type == Integer.class) {
								formatVariableValues.add(generateValue(parameter, paramTestType, "Integer"));
							} else if (type == String.class) {
								generateValue(parameter, paramTestType, "String");
							}
						}
						// build the formatted parameter string with the chosen
						// values (eg. 1:1PM EST)
						parameterString = parameter.getFormattedParameter(formatVariableValues);
					} else {
						parameterString = generateValue(parameter, paramTestType, "String");
					}

				} else {
					parameterString = "unknown type";
				}
			}
		}
		// formatted parameter arguments to be reformatted
		else if (objectParameter instanceof String) {
			parameterString = (String) objectParameter;
			Range range;
			while (parameterString.indexOf("<<REPLACE_ME_INT>>") != -1) {
				range = new IntRange();
				parameterString = parameterString.substring(0, parameterString.indexOf("<<REPLACE_ME_INT>>")) + range.random(paramTestType)
						+ parameterString.substring(parameterString.indexOf("<<REPLACE_ME_INT>>") + 18);
			}
			while (parameterString.indexOf("<<REPLACE_ME_STRING>>") != -1) {
				range = new StringRange();
				parameterString = parameterString.substring(0, parameterString.indexOf("<<REPLACE_ME_STRING>>")) + range.random(paramTestType)
						+ parameterString.substring(parameterString.indexOf("<<REPLACE_ME_STRING>>") + 21);
			}
		}
		return parameterString;
	}

	/**
	 * This helper method will return a values that corresponds to the type and
	 * passed into the method.
	 * 
	 * @param parameter
	 *            is the parameter that the value will be generated from.
	 * @param testParamType
	 *            is the type of value that should be generated. type will have
	 *            the value of either "edge" for edge cases, "inappropriate" for
	 *            a value that is not of the same type of the parameter's type,
	 *            or "appropriate" for a value that is the same type of the
	 *            parameter's type.
	 * @param type
	 *            is the class type of the value that the generateValue method
	 *            should return
	 * 
	 * @returns a value to be executed as a parameter based on the parameter and
	 *          the paramTestType.
	 */
	public String generateValue(Parameter parameter, String paramTestType, String type) {
		if (type.equals("Integer")) {
			IntRange range = new IntRange();
			if (parameter.getMax() != null) {
				range.setMax((Integer) parameter.getMax());
			}
			if (parameter.getMin() != null) {
				range.setMin((Integer) parameter.getMin());
			}
			return range.random(paramTestType);
		} else if (type.equals("Double")) {
			DoubleRange range = new DoubleRange();
			if (parameter.getMax() != null) {
				range.setMax((Double) parameter.getMax());
			}
			if (parameter.getMin() != null) {
				range.setMin((Double) parameter.getMin());
			}
			return range.random(paramTestType);
		} else {
			StringRange range = new StringRange();
			if (parameter.getMax() != null) {
				range.setMaxLength((Integer) parameter.getMax());
			}
			if (parameter.getMin() != null) {
				range.setMinLength((Integer) parameter.getMin());
			}
			return range.random(paramTestType);
		}
	}

	/**
	 * Method will return the OHSFile generated by the tests.
	 * 
	 * @return the OHSFile generated by the tests
	 */
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
			if (!TOOLCHAIN) System.out.println("command to run: " + command);
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
						Thread.sleep(1);
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
	 * Method returns the basic test output (std out/err)
	 * 
	 * @param output
	 *            - Output object containing std out/err to print
	 * @return
	 */
	private String printBasicTestOutput(Output output) {
		return "stdout of execution: " + output.getStdOutString() + "\nstderr of execution: " + output.getStdErrString() + "\n";
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
					System.out.printf("%016x  %3d of %3d   %s%n", Long.valueOf(data.getId()), Integer.valueOf(getHitCount(data.getProbes())),
							Integer.valueOf(data.getProbes().length), data.getName());
				}
			});
			reader.read();
			in.close();
		} catch (IOException e) {
			System.out.println("Unable to display raw coverage stats due to IOException related to " + this.jacocoOutputFilePath);
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

}
