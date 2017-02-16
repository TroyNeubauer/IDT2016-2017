package contest.winter2017.reader.search;

/**
 * An enum representing the filter types in the large ComboBox
 * @author Troy Neubauer
 *
 */
public enum FilterType {
	NAME("Name (Alphabetical)"), DATE("Date"), JAR_NAME("Jar File (Alphabetical)"), 
	BASIC_TESTS_COUNT("Basic Tests Count"), SECURITY_TESTS_COUNT("Security Tests Count"), BASIC_TESTS_COVERAGE("Basic Tests Code Coverage");

	public final String name;

	FilterType(String name) {
		this.name = name;
	}
}
