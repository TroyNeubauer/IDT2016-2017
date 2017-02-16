package contest.winter2017.reader.search;

import java.util.*;

import contest.winter2017.ohsfile.*;

public class NameComparator implements Comparator<MainFileEntry> {

	@Override
	public int compare(MainFileEntry o1, MainFileEntry o2) {
		return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
	}


}
