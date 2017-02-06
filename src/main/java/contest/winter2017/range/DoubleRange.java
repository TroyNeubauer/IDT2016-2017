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

	@Override
	public Double random() {
		return Maths.randRange(getMin(), getMax());
	}
	
	
	/**
	 * returns an array of typical edge cases for a double
	 */
	public Double[] generalEdgeCases(){
		Double[] output = {0.0, -50.0, Double.MAX_VALUE, Double.MIN_VALUE};
		return output;
	}
	
	public void setMin(Double newMin){
		super.setMin(newMin);
	}
	
	public void setMax(Double newMax){
		super.setMax(newMax);
	}
}
