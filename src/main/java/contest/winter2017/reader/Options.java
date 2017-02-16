package contest.winter2017.reader;

public class Options {
	
	public static boolean securityTestsFirst = true;
	public static boolean hideErrStreamsIfBlank = false;
	public static boolean onlyShowTestsThatAreErrors = false;
	public static boolean includeBasicTestsInErrSummary = true;
	public static boolean time12Hour = true;
	public static ErrorType currentErrorType = ErrorType.ANY_IN_ERR_STREAM_IS_ERROR;
	
	public enum ErrorType {
		ANY_IN_ERR_STREAM_IS_ERROR, ONLY_ERR_STREAMS_THAT_INCLUDE_JAVA_ERRORS;
	}

	private Options() {
	}

}
