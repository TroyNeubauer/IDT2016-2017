package contest.winter2017.reader.search;

import java.util.*;

import contest.winter2017.ohsfile.*;

public class DateComperator implements Comparator<MainFileEntry> {

	@Override
	public int compare(MainFileEntry o1, MainFileEntry o2) {
		if(o1.getTimestamp() == o2.getTimestamp())return 0;
		if(o1.getTimestamp() > o2.getTimestamp())return +1;
		return -1;
	}


}
