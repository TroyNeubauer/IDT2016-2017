package contest.winter2017.range;

import com.troyberry.math.*;

public class IntRange extends NumberRange<Integer> {
	
	/**
	 * Creates a new DoubleRange object.
	 */
	public IntRange(){
		super(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Creates a new IntRange object.
	 * @param min The lowest number that can be returned by the random number generator inclusive
	 * @param max The highest number that can be returned by the random number generator exclusive
	 */
	public IntRange(Integer min, Integer max) {
		super(min, max);
	}

	@Override
	public Integer random() {
		return Maths.randRange(getMin(), getMax());
	}
	
	/**
	 * returns an array of typical edge cases for an integer
	 */
	public Integer[] generalEdgeCases(){
		Integer[] output = {0, -50, Integer.MAX_VALUE, Integer.MIN_VALUE};
		return output;
	}
	
	
}
