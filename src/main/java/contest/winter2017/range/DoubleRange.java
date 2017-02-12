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

	public String getAppropriate() {
		return Maths.randRange(getMin(), getMax())+"";
	}
	
	public String getInappropriate(){
		String[] inappropriateValues = {"string", "" + (getMin() - 1), "" + (getMax() + 1)};
		return inappropriateValues[(int)(Math.random() * inappropriateValues.length)];
	}
	
	/**
	 * returns a typical edge cases for a double
	 */
	public String getGeneralEdgeCase(){
		Double[] edges = {0.0, -50.0, Double.MAX_VALUE, Double.MIN_VALUE};
		return edges[(int)(Math.random() * edges.length)] + "";
	}
	
	public void setMin(Double newMin){
		super.setMin(newMin);
	}
	
	public void setMax(Double newMax){
		super.setMax(newMax);
	}
}
