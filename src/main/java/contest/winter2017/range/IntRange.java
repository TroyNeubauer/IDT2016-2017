package contest.winter2017.range;

import com.troyberry.math.*;

public class IntRange extends NumberRange<Integer> {
	
	/**
	 * Creates a new DoubleRange object.
	 */
	public IntRange(){
		super(-10000, 10000);
	}

	/**
	 * Creates a new IntRange object.
	 * @param min The lowest number that can be returned by the random number generator inclusive
	 * @param max The highest number that can be returned by the random number generator exclusive
	 */
	public IntRange(Integer min, Integer max) {
		super(min, max);
	}

	/**
	 * @returns a random String representation of an integer within the range of max and min
	 */
	@Override
	public String getAppropriate() {
		return Maths.randRange(getMin(), getMax()) + "";
	}
	
	/**
	 * @returns returns String representation that is 
	 * a value that is either outside the range
	 * of a certain type or a value of a different type
	 */
	@Override
	public String getInappropriate(){
		String[] inappropriateValues = {50.0 + "", "string", "" + (getMin() - 1), "" + (getMax() + 1),""};
		return inappropriateValues[(int)(Math.random() * inappropriateValues.length)];
	}
	
	/**
	 * @returns a String representation typical edge case of an integer
	 */
	@Override
	public String getGeneralEdgeCase(){
		Integer[] edges = {0, -50, Integer.MAX_VALUE, Integer.MIN_VALUE};
		return edges[(int)(Math.random() * edges.length)] + "";
	}
	
	
	
}
