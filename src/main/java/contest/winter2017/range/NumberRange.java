package contest.winter2017.range;

public abstract class NumberRange<T extends Number> extends Range<T> {

	private T min, max;
	
	public NumberRange(T min, T max) {
		super();
		this.min = min;
		this.max = max;
	}

	/**
	 * @returns min
	 */
	public T getMin() {
		return min;
	}
	
	public T getMax() {
		return max;
	}
	
	/**
	 * Method sets the minimum value to generate an appropriate number from
	 * 
	 * @param newMin is the minimum value that minValue is reassigned to
	 */
	public void setMin(T newMin){
		min = newMin;
	}
	
	
	/**
	 * Method sets the maximum value to generate an appropriate number from
	 * 
	 * @param newMax is the minimum value that minValue is reassigned to
	 */
	public void setMax(T newMax){
		max = newMax;
	}
	

}
