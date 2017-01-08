package contest.winter2017.range;

import com.troyberry.math.*;

public class IntRange extends NumberRange<Integer> {

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
	
	
}
