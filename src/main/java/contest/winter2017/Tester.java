package contest.winter2017;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import org.jacoco.core.analysis.*;
import org.jacoco.core.data.*;
import org.jacoco.core.tools.*;

import com.troyberry.util.*;

import contest.winter2017.Parameter;
import contest.winter2017.range.*;

/**
 * Class that will handle execution of basic tests and exploratory security test on a black-box executable jar.  
 * 
 * Example code that we used to guide our use of Jacoco code coverage was found @ http://www.eclemma.org/jacoco/trunk/doc/api.html
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
	 * parameter factory that can be used to help figure out parameter signatures from the blackbox jars
	 */
	private ParameterFactory parameterFactory = null;

	/**
	 * bbTests is the number of black box tests to execute
	 */
	private int bbTests;

	/**
	 * timeGoal is the amount time in milliseconds that we have to match
	 */
	private long timeGoal;
	
	/**
	 * if true, only the YAML report is in the output
	 */
	private boolean toolChain;
	
	/**
	 * number of passed security tests
	 */
	private int securityTestsPassCount = 0;
	
	/**
	 * number of failed security tests
	 */
	private int securityTestsFailCount = 0;
	
	/**
	 * array list of unique errors seen
	 */
	ArrayList<String> errors = new ArrayList<String>();
		
	//////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////
	
	/**
	 * Method initializes the additional options specified by the user

	 * @param bbTests - integer representing the number of tests to execute
	 * @param timeGoal - integer representing the time goal in milliseconds
	 */
	public void setAdditionalOptions(int bbTests, int timeGoal, boolean toolChain) {
		this.bbTests = bbTests;
		this.timeGoal = timeGoal * 60000L;
		this.toolChain = toolChain;
	}
	
	/**
	 * Method that will initialize the Framework by loading up the jar to test, and then extracting
	 * parameters, parameter bounds (if any), and basic tests from the jar.
	 * 
	 * @param initJarToTestPath - String representing path of the jar to test
	 * @param initJacocoOutputDirPath - String representing path of the directory jacoco will use for output
	 * @param initJacocoAgentJarPath - String representing path of the jacoco agent jar
	 * @return boolean - false if initialization encounters an Exception, true if it does not
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean init(String initJarToTestPath, String initJacocoOutputDirPath, String initJacocoAgentJarPath) {
		
		this.jarToTestPath = initJarToTestPath;
		this.jacocoOutputDirPath = initJacocoOutputDirPath;
		this.jacocoAgentJarPath = initJacocoAgentJarPath;


		File jarFileToTest = new File(this.jarToTestPath);
		this.jacocoOutputFilePath = this.jacocoOutputDirPath+"\\"+jarFileToTest.getName().replaceAll("\\.", "_")+JACOCO_OUTPUT_FILE_SUFFIX;
		
		File jacocoOutputFile = new File(this.jacocoOutputFilePath);
		if (jacocoOutputFile !=null && jacocoOutputFile.exists()) {
			jacocoOutputFile.delete();
		}
		
		URL fileURL = null;
	    URL jarURL = null;
		try {
			
			// load up the jar under test so that we can access information about the class from 'TestBounds'
			fileURL = jarFileToTest.toURI().toURL();
			String jarUrlTemp = "jar:"+jarFileToTest.toURI().toString()+"!/";
			jarURL = new URL(jarUrlTemp);
			URLClassLoader cl = URLClassLoader.newInstance(new URL[]{fileURL});
			JarURLConnection jarURLconn = null;
			jarURLconn = (JarURLConnection)jarURL.openConnection();

			// figuring out where the entry-point (main class) is in the jar under test
			Attributes attr = null;
			attr = jarURLconn.getMainAttributes();
			String mainClassName = attr.getValue(Attributes.Name.MAIN_CLASS);
			
			// loading the TestBounds class from the jar under test
			String mainClassTestBoundsName = mainClassName+"TestBounds";
			Class<?> mainClassTestBounds = null;
			try {
				mainClassTestBounds = cl.loadClass(mainClassTestBoundsName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			System.out.println("CLASS: " + mainClassTestBoundsName);
			// use reflection to invoke the TestBounds class to get the usage information from the jar
			Method testBoundsMethod = null;
			testBoundsMethod = mainClassTestBounds.getMethod("testBounds");
			
			Object mainClassTestBoundsInstance = null;
			mainClassTestBoundsInstance = mainClassTestBounds.newInstance();

			Map<String, Object> mainClassTestBoundsMap = null;
			// Runs the testBounds method
			mainClassTestBoundsMap =
					(Map<String, Object>)testBoundsMethod.invoke(mainClassTestBoundsInstance);

			// instantiating a new Parameter Factory using the Test Bounds map
			this.parameterFactory = new ParameterFactory(mainClassTestBoundsMap);
			
			// get a list of basic tests from the TestBounds class
			this.tests = new ArrayList<Test>();
			List testList = (List)mainClassTestBoundsMap.get("tests");
			for(Object inTest : testList) {
				this.tests.add(new Test((Map)inTest));
			}

		} catch (Exception e) {
			// if we have an exception during initialization, display the error to the user and return a false status
			System.out.println("ERROR: An exception occurred during initialization.");
			e.printStackTrace();
			return false;
		} 
		
		// if we did not encounter an exception during initialization, return a true status
		return true;
	}
	
	
	/**
	 * This is the half of the framework that IDT has completed. We are able to pull basic tests 
	 * directly from the executable jar. We are able to run the tests and assess the output as PASS/FAIL.
	 * 
	 * You likely do not have to change this part of the framework. We are considering this complete and 
	 * want your team to focus more on the SecurityTests.
	 */
	public void executeBasicTests() {
		
		int passCount = 0;
		int failCount = 0;
		
		// iterate through the lists of tests and execute each one
		for(Test test : this.tests) {
			 
			// instrument the code to code coverage metrics, execute the test with given parameters, then show the output
			Output output = instrumentAndExecuteCode(test.getParameters().toArray());
			printBasicTestOutput(output);
			
			// determine the result of the test based on expected output/error regex
			if(output.getStdOutString().matches(test.getStdOutExpectedResultRegex())
					&& output.getStdErrString().matches(test.getStdErrExpectedResultRegex())) {
				System.out.println("basic test result: PASS");
				passCount++;
			}
			else {
				System.out.println("basic test result: FAIL ");
				failCount++;
				
				// since we have a failed basic test, show the expectation for the stdout
				if(!output.getStdOutString().matches(test.getStdOutExpectedResultRegex())) {
					System.out.println("\t ->stdout: "+output.getStdOutString());
					System.out.println("\t ->did not match expected stdout regex: "+test.getStdOutExpectedResultRegex());
				}
				
				// since we have a failed basic test, show the expectation for the stderr
				if(!output.getStdErrString().matches(test.getStdErrExpectedResultRegex())) {
					System.out.println("\t ->stderr: "+output.getStdErrString());
					System.out.println("\t ->did not match expected stderr regex: "+test.getStdErrExpectedResultRegex());
					
				}
			}
			System.out.println(HORIZONTAL_LINE);
		} 
		// print the basic test results and the code coverage associated with the basic tests
		double percentCovered = generateSummaryCodeCoverageResults();
		System.out.println("basic test results: " + (passCount + failCount) + " total, " + passCount + " pass, " + failCount + " fail, " + StringFormatter.clip(percentCovered, 2) + " percent covered");
		System.out.println(HORIZONTAL_LINE);
	}
	
	
	public void executeSecurityTests() {
		System.out.println();
		long timeToEnd = System.currentTimeMillis() + timeGoal;
		System.out.println("Starting security tests");
		int testIterations = 0;
		int passCount = 0;
		int failCount = 0;
		
		//each row represents a different parameter
		//each row holds a parameter we need to test in the first index, values in the subsequent indexes
		//UPDATE 1/30 - holds a different dependent parameter in each row. subsequent values are values to test. know if dependent if it has a dependent param map
		List<List<Parameter>> dependentPotentialParametersLists = new ArrayList<List<Parameter>>();
		List<String> previousParameterStrings = new ArrayList<String>();
		
		//handles dependent
		if(!this.parameterFactory.isBounded()){
			//gets all enumerated values. The indexes of each enumerated value correspond to the row index of potentialParametersLists.
			//UPDATE 1/30- gets all enumperatedParameters, but will only hold nondependent parameters. the dependent ones will be removed later
			//List<String> enumeratedParameters = this.parameterFactory.getNext(previousParameterStrings).get(0).getEnumerationValues();
			List<String> enumeratedParametersNondependent = generateValues(this.parameterFactory.getNext(previousParameterStrings).get(0));//formats them as well
			//corresponds to the rows of dependentPotentialParameterLists
			List<String> enumeratedParametersDependent = new ArrayList<String>();
			
			for(int k = enumeratedParametersNondependent.size() -1; k >=0; k--){
				//need an array list to get the next potential parameter
				ArrayList<String> dummyPrevParamString = new ArrayList<String>();
				dummyPrevParamString.add(enumeratedParametersNondependent.get(k));
				//puts enumerated value as first index
				List<Parameter> potentialParameters = this.parameterFactory.getNext(dummyPrevParamString);
				
				//enumerated value is a dependent parameter
				if(!potentialParameters.isEmpty()){
					enumeratedParametersDependent.add(0,enumeratedParametersNondependent.get(k));
					dependentPotentialParametersLists.add(0,potentialParameters);//adds the dependent parameter to a row
					
					// dummyPrevParamString used only to find other potential parameters for a dependent parameter
					dummyPrevParamString = new ArrayList<String>();
					dummyPrevParamString.add(enumeratedParametersNondependent.get(k));//will be "--Integer" or something
					while(!potentialParameters.isEmpty()){//make a dummy previous parameter strings using the generate values method?
						//check if its an enumeration. if it is, get the list of enumerations and you can start testing that
						
						dummyPrevParamString.add(generateValues(potentialParameters.get(potentialParameters.size()-1)).get(0));
						potentialParameters = this.parameterFactory.getNext(dummyPrevParamString);
						if(!potentialParameters.isEmpty())
							dependentPotentialParametersLists.get(0).add(potentialParameters.get(0));
					}
					enumeratedParametersNondependent.remove(k);
				}
				
				
			}
			
			//just stuff to make sure everything works -- delete later
			System.out.println("nondependent enums: " + enumeratedParametersNondependent);
			for(int k = 0; k < dependentPotentialParametersLists.size(); k++){
				System.out.println("type: " + dependentPotentialParametersLists.get(k).get(0).getType());
				System.out.println("enum: " + enumeratedParametersDependent.get(k));
				System.out.println("dependentPotentialParameters: " + dependentPotentialParametersLists.get(k));
			}
			
			
			//starting tests
			
			//tests expecting black box to pass
			//runs with a blank argument and with an argument with only a string as an argument
			Object[] firstTests = {"", "random+sStrinG023;.1"};
			for(int k = 0; k<firstTests.length; k++){
				testIterations++;
				Object[] toTest = {firstTests[k]};
				executeAndPrintResults(toTest, false);
			}
			
				
			while(System.currentTimeMillis() < timeToEnd){
				int[] nondependentIndexesToTest = new int[(/*int)(Math.random() * */enumeratedParametersNondependent.size())];
				int[] dependentIndexesToTest = new int[(/*int)(Math.random() **/ enumeratedParametersDependent.size())];
				ArrayList<Object> parameters = new ArrayList<Object>();
				for(int k = 0; k < nondependentIndexesToTest.length; k++){
					
				}
				
			}
			
		}
		
		//handles fixed
		else{
			List<Parameter> fixedParameters = this.parameterFactory.getFixedParametersList();
			
			//starting tests
		}
		
		//System.out.println(enumeratedParameters);
//		List<List<Parameter>> potentialParameterLists = getPotentialParameterLists();
//		System.out.println(potentialParameterLists);
//		//ends when reaches time limit
//		//while(System.currentTimeMillis() < timeToEnd) {
//			//Object[] parameters = new Object[10];//dummy
//			
//			//generates values where no errors are expected and instruments and executes
//			//generateExpectedPassValues();
//			
//			//generates values an error message is expected
//			//generateExpectedFailValues();
//			
//			//Output output = instrumentAndExecuteCode(parameters);
//			//printBasicTestOutput(output);
//			
//			showCodeCoverageResultsExample();
//			testIteration++;
//		//}

		
	}
	
	/**
	 * Method will return an List of values that corresponds to the type and passed into the method.
	 * 
	 * @param parameter is the parameter that different values will be generated from
	 * 
	 * @returns a list containing one value of the type of the parameter that is passed into the
	 * method. If the parameter is enumerated, it returns a list of enumerated values
	 */
	public List<String> generateValues(Parameter parameter){
		//make a range object later?
//		List<String> previousParameterStrings = new ArrayList<String>(); // start with a blank parameter list since we are going to start with the first parameter
//		List<Parameter> potentialParameters = this.parameterFactory.getNext(previousParameterStrings);
//		Parameter potentialParameter = potentialParameters.get(0);
		List<String> parameterStrings = new ArrayList<String>();
		//if parameter is an enumeration, the method returns the enumerated values
		if (parameter.isEnumeration()) {//if the parameter is an enumeration, the execute security tests method should have already known this and should plan accordingly
			List<String> enumeratedValues = parameter.getEnumerationValues();
//			parameterStrings = parameter.getEnumerationValues().get(0) + " "; // dumb logic - given a list of options, always use the first one
			
			//if any enumerated values are formatted, the appropriate values are replaced
			for(int k = 0; k < enumeratedValues.size(); k++){
				// if the parameter has internal format (eg. "<number>:<number>PM EST")
				if(parameter.isFormatted()) {//should this go above the forloop?]
					// loop over the areas of the format that must be replaced and choose values
					List<Object> formatVariableValues = new ArrayList<Object>();
					for(Class type :parameter.getFormatVariables(enumeratedValues.get(k))) {
						if (type == Integer.class){ 
							Range range= new IntRange();
							formatVariableValues.add(range.random()); // dumb logic - always use '1' for an Integer
						} else if (type == String.class) {
							Range range= new StringRange();
							formatVariableValues.add(range.random()); // dumb logic - always use 'one' for a String
						}
					}
					System.out.println("formatvariablevalues" + formatVariableValues);
					//build the formatted parameter string with the chosen values (eg. 1:1PM EST)
					enumeratedValues.set(k,
							parameter.getFormattedParameter(
									enumeratedValues.get(k), formatVariableValues));
					/*parameterString =
							parameter.getFormattedParameter(
									parameterString, formatVariableValues);*/
				}
				//previousParameterStrings.add(parameterString);
			}
			return enumeratedValues;
		// if it is not an enumeration parameter, it is either an Integer, Double, or String
		} else {
			if (parameter.getType() == Integer.class){ 
				IntRange range= new IntRange();
				if(parameter.getMax() != null){
					range.setMax((Integer)parameter.getMax());
				}
				if(parameter.getMin() != null){
					range.setMin((Integer)parameter.getMin());
				}
				parameterStrings.add(range.random() + " ");	// dumb logic - always use '1' for an Integer
			} else if (parameter.getType() == Double.class) {//all the if statements may not be necessary
				DoubleRange range= new DoubleRange();
				if(parameter.getMax() != null){
					range.setMax((Double)parameter.getMax());
				}
				if(parameter.getMin() != null){
					range.setMin((Double)parameter.getMin());
				}
				parameterStrings.add(range.random() + " ");	// dumb logic - always use '1.0' for a Double*/
			} else if (parameter.getType() == String.class) {
				Range range = new StringRange();

				// if the parameter has internal format (eg. "<number>:<number>PM EST")
				if(parameter.isFormatted()) {

					// loop over the areas of the format that must be replaced and choose values
					List<Object> formatVariableValues = new ArrayList<Object>();
					for(Class type : parameter.getFormatVariables()) {
						if (type == Integer.class){ 
							Range intRange= new IntRange();
							formatVariableValues.add(intRange.random()); // dumb logic - always use '1' for an Integer
						} else if (type == String.class) {
							formatVariableValues.add(range.random()); // dumb logic - always use 'one' for a String
						}
					}
					
					//build the formatted parameter string with the chosen values (eg. 1:1PM EST)
					parameterStrings.add(
							parameter.getFormattedParameter(formatVariableValues));
				}
				else {
					parameterStrings.add(range.random() + " ");		// dumb logic - always use 'one' for a String
				}

			} else {
				parameterStrings.add("unknown type");//should we do something about this
			}
		}
		return parameterStrings;
	}
	
	/**
	 * Method will execute an individual security test and print its results.
	 * 
	 * @param toTest is the array of parameters to be passed into the instrumentAndExecuteCode method
	 * @param errorExpected is true if the individual security test is expected to have an error, false if not
	 * 
	 * @return true if the security test passed, false if not
	 */
	public boolean executeAndPrintResults(Object[] toTest, boolean errorExpected){
		Output output = instrumentAndExecuteCode(toTest);
		printBasicTestOutput(output);
		boolean passed = false;
		if(!errorExpected){
			if(output.getStdErrString().length() > 0){
				securityTestsFailCount++;
				System.out.println("security test result: FAIL");
			} else{
				securityTestsPassCount++;
				System.out.println("security test result: PASS");
				passed = true;
			}
		} else//if error was expected, the security test passes no matter what
			securityTestsPassCount++;
		//if error is unique, adds it to the errors arrayList
		if(output.getStdErrString()!=""){
			boolean uniqueError = true;
			for(int k = 0; k < errors.size(); k++){
				if(output.getStdErrString().equals(errors.get(k)))
					uniqueError = false;
			}
			if(uniqueError)
				errors.add(output.getStdErrString());
		}
		System.out.println(HORIZONTAL_LINE);
		return passed;
	}
	
	
	//////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////
	
	/**
	 * This method will instrument and execute the jar under test with the supplied parameters.
	 * This method should be used for both basic tests and security tests.
	 * 
	 * An assumption is made in this method that the word java is recognized on the command line
	 * because the user has already set the appropriate environment variable path. 
	 * 
	 * @param parameters - array of Objects that represents the parameter values to use for this 
	 *                     execution of the jar under test
	 *                     
	 * @return Output representation of the standard out and standard error associated with the run
	 */
	private Output instrumentAndExecuteCode(Object[] parameters) {
			
		Process process = null;
		Output output = null;	
		
		// we are building up a command line statement that will use java -jar to execute the jar
		// and uses jacoco to instrument that jar and collect code coverage metrics
		String command = "java";
		try {	
			command += " -javaagent:" + this.jacocoAgentJarPath + "=destfile=" + this.jacocoOutputFilePath;
			command += " -jar " + this.jarToTestPath;
			for (Object o: parameters) {
				command += " " + o.toString();
			}
			
			// show the user the command to run and prepare the process using the command
			System.out.println("command to run: "+command);
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
			
			// while standard out is not complete OR standard error is not complete
			// continue to probe the output/error streams for the applications output
			while(!outDone || !errDone) {
				
				// monitoring the standard output from the application
				boolean outReady = true;
				if(outReady) {
					line = brOut.readLine();
					if(line == null) {
						outDone = true;
					}
					else {
						stdOutBuff.append(line);
					}
				}
				
				// monitoring the standard error from the application
				boolean errReady = true;
				if(errReady) {
					line = brErr.readLine();
					if(line == null) {
						errDone = true;
					}
					else {
						stdErrBuff.append(line);
					}
				}
				
				// if standard out and standard error are not ready, wait for 5ms 
				// and try again to monitor the streams
				if(!outReady && !errReady)  {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						// NOP
					}
				}
			}	
			
			// we now have the output as an object from the run of the black-box jar
			// this output object contains both the standard output and the standard error
			output = new Output(stdOutBuff.toString(), stdErrBuff.toString());
			
		} catch (IOException e) {
			System.out.println("ERROR: IOException has prevented execution of the command: " + command); 
		}
		
		return output;
	}
	
	
	/**
	 * Method used to print the basic test output (std out/err)
	 * @param output - Output object containing std out/err to print 
	 */
	private void printBasicTestOutput(Output output) {
		System.out.println("stdout of execution: " + output.getStdOutString());
		System.out.println("stderr of execution: " + output.getStdErrString());
	}
	
	
	/**
	 * Method used to print raw code coverage stats including hits/probes
	 * @throws IOException
	 */
	private void printRawCoverageStats()  {
		System.out.printf("exec file: %s%n", this.jacocoOutputFilePath);
		System.out.println("CLASS ID         HITS/PROBES   CLASS NAME");

		try {
			File executionDataFile = new File(this.jacocoOutputFilePath);
			final FileInputStream in = new FileInputStream(executionDataFile);
			final ExecutionDataReader reader = new ExecutionDataReader(in);
			reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
				public void visitSessionInfo(final SessionInfo info) {
					System.out.printf("Session \"%s\": %s - %s%n", info.getId(), new Date(
							info.getStartTimeStamp()),
							new Date(info.getDumpTimeStamp()));
				}
			});
			reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
				public void visitClassExecution(final ExecutionData data) {
					System.out.printf("%016x  %3d of %3d   %s%n",
							Long.valueOf(data.getId()),
							Integer.valueOf(getHitCount(data.getProbes())),
							Integer.valueOf(data.getProbes().length),
							data.getName());
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
	 * @param data - boolean array of coverage data where true indicates hits
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
	 * Method for generating code coverage metrics including instructions, branches, lines, 
	 * methods and complexity. 
	 * 
	 * @return double representation of the percentage of code covered during testing
	 */
	private double generateSummaryCodeCoverageResults() {
		double percentCovered = 0.0;
		long total = 0;
		long covered = 0;
		try {
			// creating a new file for output in the jacoco output directory (one of the application arguments)
			File executionDataFile = new File(this.jacocoOutputFilePath);
			ExecFileLoader execFileLoader = new ExecFileLoader();
			execFileLoader.load(executionDataFile);
			
			// use CoverageBuilder and Analyzer to assess code coverage from jacoco output file
			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(
					execFileLoader.getExecutionDataStore(), coverageBuilder);
			
			// analyzeAll is the way to go to analyze all classes inside a container (jar or zip or directory)
			analyzer.analyzeAll(new File(this.jarToTestPath));
			
			
			for (final IClassCoverage cc : coverageBuilder.getClasses()) {
				
				// report code coverage from all classes that are not the TestBounds class within the jar
				if(!cc.getName().endsWith("TestBounds")) {
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
		
		percentCovered = ((double)covered / (double)total) * 100.0;
		return percentCovered;
	}
	
	
	/**
	 * This method shows an example of how to generate code coverage metrics from Jacoco
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
			final Analyzer analyzer = new Analyzer(
					execFileLoader.getExecutionDataStore(), coverageBuilder);
			
			analyzer.analyzeAll(new File(this.jarToTestPath));
			
			for (final IClassCoverage cc : coverageBuilder.getClasses()) {
				executionResults += "Coverage of class " + cc.getName() + ":\n";
				executionResults += getMetricResultString("instructions", cc.getInstructionCounter());
				executionResults += getMetricResultString("branches", cc.getBranchCounter());
				executionResults += getMetricResultString("lines", cc.getLineCounter());
				executionResults += getMetricResultString("methods", cc.getMethodCounter());
				executionResults += getMetricResultString("complexity", cc.getComplexityCounter());
				
				// adding this to a string is a little impractical with the size of some of the files, 
				// so we are commenting it out, but it shows that you can get the coverage status of each line
				// if you wanted to add debug argument to display this level of detail at command line level.... 
				//
				//for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
					//executionResults += "Line " + Integer.valueOf(i) + ": " + getStatusString(cc.getLine(i).getStatus()) + "\n";
				//}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return executionResults;
	}
	
	
	/**
	 * Method to translate the Jacoco line coverage status integers to Strings.
	 * 
	 * @param status - integer representation of line coverage status provided by Jacoco
	 * @return String representation of line coverage status (not covered, partially covered, fully covered)
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
	 * Method to translate the counter data and units into a human readable metric result String
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
	 * This method is not meant to be part of the final framework. It was included to demonstrate
	 * three different ways to tap into the code coverage results/metrics using jacoco. 
	 * 
	 * This method is deprecated and will be removed from the final product after your team completes 
	 * development. Please do not add additional dependencies to this method. 
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
