package contest.winter2017.ohsfile;

/**
 * Represents the execution of a basic test
 * @author Troy Neubauer
 *
 */
public class BasicTest extends ExecutedTest {
	
	private boolean pass;
	private String expectedOut, expectedErr;

	public BasicTest(String argsUsed, String outputOut, String outputErr, String expectedOut, String expectedErr) {
		super(argsUsed, outputOut, outputErr);
		this.expectedOut = expectedOut;
		this.expectedErr = expectedErr;
		this.pass = expectedOut.equals(outputOut) && expectedErr.equals(outputErr);
	}

	public boolean isPass() {
		return pass;
	}

	public String getExpectedOut() {
		return expectedOut;
	}

	public String getExpectedErr() {
		return expectedErr;
	}

	@Override
	public String toString() {
		super.toString();
		return "BasicTest [argsUsed=" + getArgsUsed() + ", outputOut=" + getOutputOut() + ", outputErr=" + getOutputErr() + "pass=" + pass + ", expectedOut=" + expectedOut + ", expectedErr=" + expectedErr + "]";
	}
	
	

}
