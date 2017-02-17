package contest.winter2017.range;

import com.troyberry.math.*;

public class DoubleRange extends NumberRange<Double> {
	
	/**
	 * Creates a new DoubleRange object.
	 */
	public DoubleRange(){
		super(-10000.0, 10000.0);
	}
	
	/**
	 * Creates a new DoubleRange object.
	 * @param min The lowest number that can be returned by the random number generator inclusive
	 * @param max The highest number that can be returned by the random number generator exclusive
	 */
	public DoubleRange(Double min, Double max) {
		super(min, max);
	}
	
	/**
	 * @returns a random String representation of a double within the range of max and min
	 */
	public String getAppropriate() {
		return Maths.randRange(getMin(), getMax())+"";
	}
	
	/**
	 * @returns returns String representation that is 
	 * a value that is either outside the range
	 * of a certain type or a value of a different type
	 */
	public String getInappropriate(){
		String[] inappropriateValues = {"","string", "NOT a double!", "" + (getMin() - 10), "" + (getMax() + 10)};
		return inappropriateValues[(int)(Math.random() * inappropriateValues.length)];
	}
	
	/**
	 * @returns a String representation typical edge case of a double
	 */
	public String getGeneralEdgeCase(){
		Double[] edges = {0.0, -50.0, Double.MAX_VALUE, Double.MIN_VALUE};
		return edges[(int)(Math.random() * edges.length)] + "";
	}
	
}
