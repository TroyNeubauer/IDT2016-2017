package contest.winter2017.range;

import com.troyberry.math.*;

public class StringRange extends Range<String> {
	
	private int minLength, maxLength;
	private String charSet;
	
	/**
	 * Creates a new String Range which represents a how a random string should be calculated.
	 * @param charSet The characters that can be in a randomly generated string
	 * @param minLength The minimum length that the string can be
	 * @param maxLength The maxiumn length that the string can be
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

	@Override
	public String random() {
		int length = Maths.randRange(minLength, maxLength);
		String result = new String();
		
		for(int i = 0; i < length; i++) {
			result += charSet.charAt(Maths.randRange(0, charSet.length()));
		}
		return result;
	}
	
	/**
	 * returns an array of typical string edge cases
	 */
	public String[] generalEdgeCases(){
		String[] output = {""};
		return output;
	}
	
	

}
