package contest.winter2017.range;

public abstract class Range <T> {
	
	public abstract String getGeneralEdgeCase();
	public abstract String getInappropriate();
	public abstract String getAppropriate();
	public String random(String paramTestType) {
		if(paramTestType.equals("appropriate")){
			return getAppropriate();
		}
		else if(paramTestType.equals("inappropriate")){
			return getInappropriate();
		}
		else
			return getGeneralEdgeCase();
	}
}
