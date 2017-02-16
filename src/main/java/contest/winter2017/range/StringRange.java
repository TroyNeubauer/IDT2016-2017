package contest.winter2017.range;

import com.troyberry.math.*;

public class StringRange extends Range<String> {

	private int minLength, maxLength;
	private String charSet;

	/**
	 * Creates a new String Range which represents a how a random string should
	 * be calculated.
	 */
	public StringRange() {
		super();
		this.minLength = 1;
		this.maxLength = 50;
		this.charSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	}

	/**
	 * Creates a new String Range which represents a how a random string should
	 * be calculated.
	 * 
	 * @param charSet
	 *            The characters that can be in a randomly generated string
	 * @param minLength
	 *            The minimum length that the string can be
	 * @param maxLength
	 *            The maximum length that the string can be
	 */
	public StringRange(String charSet, int minLength, int maxLength) {
		super();
		this.minLength = minLength;
		this.maxLength = maxLength;
	}

	public int getMinLength() {
		return minLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public String getCharSet() {
		return charSet;
	}

	public void setMinLength(int newMin) {
		minLength = newMin;
	}

	public void setMaxLength(int newMax) {
		maxLength = newMax;
	}

	public void setCharSet(String newSet) {
		charSet = newSet;
	}

	/**
	 * @returns a random String whose length is in between minLength and
	 *          maxLength
	 */
	@Override
	public String getAppropriate() {
		return getAppropriate(this.minLength, this.maxLength);
	}

	/**
	 * This method generates a random String whose length is in between
	 * minLength and maxLength
	 * 
	 * @param minLength
	 *            is the minimum length of the string to be generated
	 * @param maxLength
	 *            is the maximum length of the string to be generated
	 * 
	 * @returns a random String whose length is in between minLength and
	 *          maxLength
	 */
	public String getAppropriate(int minLength, int maxLength) {
		int length = Maths.randRange(minLength, maxLength);
		String result = new String();

		for (int i = 0; i < length; i++) {
			result += charSet.charAt(Maths.randRange(0, charSet.length() - 1));
		}
		return "\"" + result + "\"";
	}

	/**
	 * @returns returns String representation that is a value that is either
	 *          outside the range of a certain type or a value of a different
	 *          type
	 */
	@Override
	public String getInappropriate() {
		// violateMinLength and ViolateMaxLength are Strings that tests if jar
		// throws an exception
		// if the String's length is less that the min or max length that the
		// parameter may specify
		String violateMinLength = getAppropriate(0, minLength - 1);
		String violateMaxLength = getAppropriate(maxLength + 1, maxLength + 100);
		String[] inappropriateValues = { "", "\"      \"", violateMinLength, violateMaxLength };
		return inappropriateValues[(int) (Math.random() * inappropriateValues.length)];
	}

	/**
	 * @returns a String representation typical edge case of a double
	 */
	@Override
	public String getGeneralEdgeCase() {
		String[] edges = { "", "\"         \"", ",~`" };
		return edges[(int) (Math.random() * edges.length)];
	}

}
