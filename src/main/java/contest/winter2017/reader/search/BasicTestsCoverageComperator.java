package contest.winter2017.reader.search;

import java.util.*;

import contest.winter2017.ohsfile.*;

public class BasicTestsCoverageComperator implements Comparator<MainFileEntry> {

	@Override
	public int compare(MainFileEntry o1, MainFileEntry o2) {
		if(o1.getPercentCoveredForBasicTests() > o2.getPercentCoveredForBasicTests())return +1;
		if(o1.getPercentCoveredForBasicTests() < o2.getPercentCoveredForBasicTests())return -1;
		return 0;
	}

}
