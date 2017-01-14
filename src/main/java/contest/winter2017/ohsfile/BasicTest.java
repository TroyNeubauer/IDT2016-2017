package contest.winter2017.ohsfile;

/**
 * Represents the execution of a basic test
 * @author Troy Neubauer
 *
 */
public class BasicTest extends ExecutedTest {
	
	private boolean pass;
	private String expectedOut, expectedErr;

	public BasicTest(String argsUsed, String outputOut, String outputErr) {
		super(argsUsed, outputOut, outputErr);
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
	
	

}
