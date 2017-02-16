package contest.winter2017.reader.util;

import java.awt.*;

import javax.swing.*;

import contest.winter2017.reader.search.*;

public class SearchModes extends JComboBox<String> {
	
	public SearchModes() {
		super();
		setFont(new Font("", Font.BOLD, 15));
		setModel(new DefaultComboBoxModel<String>(new String[]{FilterType.NAME.name, FilterType.DATE.name, FilterType.JAR_NAME.name, 
		FilterType.BASIC_TESTS_COUNT.name, FilterType.SECURITY_TESTS_COUNT.name, FilterType.BASIC_TESTS_COVERAGE.name}));
	}
	
	public FilterType getType() {
		String type = getSelectedItem().toString();
		if(type.equals(FilterType.NAME.name)) return FilterType.NAME;
		if(type.equals(FilterType.DATE.name)) return FilterType.DATE;
		if(type.equals(FilterType.JAR_NAME.name)) return FilterType.JAR_NAME;
		if(type.equals(FilterType.BASIC_TESTS_COUNT.name)) return FilterType.BASIC_TESTS_COUNT;
		if(type.equals(FilterType.SECURITY_TESTS_COUNT.name)) return FilterType.SECURITY_TESTS_COUNT;
		if(type.equals(FilterType.BASIC_TESTS_COVERAGE.name)) return FilterType.BASIC_TESTS_COVERAGE;
		return null;
	}
	
}
