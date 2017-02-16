package contest.winter2017.reader.search;

/**
 * A storage class for storing what information should be searched for
 * @author Troy Neubauer
 *
 */
public class SearchData {
	
	private boolean useSearchText;
	private String searchString;
	private boolean invert;
	private FilterType filterType;
	
	public static SearchData basic() {
		return new SearchData(FilterType.NAME, false);
	}
	
	public SearchData(String searchString, boolean invert) {
		this.useSearchText = true;
		this.searchString = searchString;
		this.invert = invert;
	}

	public SearchData(FilterType filterType, boolean invert) {
		this.useSearchText = false;
		this.filterType = filterType;
		this.invert = invert;
	}

	
	public boolean usesSearchText() {
		return useSearchText;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.useSearchText = true;
		this.searchString = searchString;
	}

	public boolean isInverted() {
		return invert;
	}

	public void setInverted(boolean invert) {
		this.invert = invert;
	}

	public FilterType getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}
	
	
}
