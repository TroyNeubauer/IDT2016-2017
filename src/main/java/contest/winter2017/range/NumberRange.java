package contest.winter2017.range;

public abstract class NumberRange<T extends Number> extends Range<T> {

	private T min, max;
	
	public NumberRange(T min, T max) {
		super();
		this.min = min;
		this.max = max;
	}

	public T getMin() {
		return min;
	}

	public T getMax() {
		return max;
	}
	
	public void setMin(T newMin){
		min = newMin;
	}
	
	public void setMax(T newMax){
		max = newMax;
	}
	

}
