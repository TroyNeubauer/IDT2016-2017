package contest.winter2017.range;

public abstract class Range<T> {

	/**
	 * @returns a String representation typical edge case of a certain type
	 */
	public abstract String getGeneralEdgeCase();

	/**
	 * @returns returns String representation that is 
	 * a value that is either outside the range
	 * of a certain type or a value of a different type
	 */
	public abstract String getInappropriate();

	/**
	 * @returns a random String representation of a certain type
	 */
	public abstract String getAppropriate();

	/**
	 * @param paramTestType
	 *            is a String representing the type of parameter to be returned.
	 * 
	 * @returns a typical edge case for an integer
	 */
	public String random(String paramTestType) {
		if (paramTestType.equals("appropriate")) {
			return getAppropriate();
		} else if (paramTestType.equals("inappropriate")) {
			return getInappropriate();
		} else
			return getGeneralEdgeCase();
	}
}
