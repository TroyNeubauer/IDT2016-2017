package contest.winter2017.ohsfile;

/**
 * Represents a single execution of a testable jar
 * @author Troy Neubauer
 *
 */
public abstract class ExecutedTest {
	
	private final String argsUsed;
	private final String outputOut, outputErr;
	
	public ExecutedTest(String argsUsed, String outputOut, String outputErr) {
		this.argsUsed = argsUsed;
		this.outputOut = outputOut;
		this.outputErr = outputErr;
	}
	
	public String getArgsUsed() {
		return argsUsed;
	}
	public String getOutputOut() {
		return outputOut;
	}
	public String getOutputErr() {
		return outputErr;
	}

	@Override
	public String toString() {
		return "ExecutedTest [argsUsed=" + argsUsed + ", outputOut=" + outputOut + ", outputErr=" + outputErr + "]";
	}
	
	

}
