package contest.winter2017.range;

import com.troyberry.math.*;

public class DoubleRange extends NumberRange<Double> {
	
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
	
}
