package contest.winter2017.ohsfile;

/**
 * Represents the execution of a security test
 * @author Troy Neubauer
 *
 */
public class SecurityTest extends ExecutedTest {
	
	private float percentCovered;

	public SecurityTest(String argsUsed, String outputOut, String outputErr, float percentCovered) {
		super(argsUsed, outputOut, outputErr);
		this.percentCovered = percentCovered;
	}

	public float getPercentCovered() {
		return percentCovered;
	}
	
	

}
