package contest.winter2017.reader.search;

import java.util.*;

import contest.winter2017.ohsfile.*;

public class JarFileComparator implements Comparator<MainFileEntry> {

	@Override
	public int compare(MainFileEntry o1, MainFileEntry o2) {
		if (o1.getJarFileName().equals(o2.getJarFileName())) {
			// If they have the jar file same name, return the one with the most alphabetical name
			return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
		} else {
			// If they have different jar file names, return the one with the most alphabetical jar file name
			return o1.getJarFileName().toLowerCase().compareTo(o2.getJarFileName().toLowerCase());
		}

	}

}
