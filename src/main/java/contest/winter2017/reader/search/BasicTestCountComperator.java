package contest.winter2017.reader.search;

import java.util.*;

import contest.winter2017.ohsfile.*;

public class BasicTestCountComperator implements Comparator<MainFileEntry> {

	@Override
	public int compare(MainFileEntry o1, MainFileEntry o2) {
		return (o1.getPassCount() + o1.getFailCount()) - (o2.getPassCount() + o2.getFailCount());
	}

}
