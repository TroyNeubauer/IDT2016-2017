package contest.winter2017.reader.search;

import java.util.*;

import com.troyberry.util.*;

import contest.winter2017.ohsfile.*;

/**
 * This class compares two MainFileEntries using a keyword. 
 * This comparator is used to order the Entries list when a keyword is typed into the search bar
 * @author Troy Neubauer
 *
 */
public class KeywordComperator implements Comparator<MainFileEntry> {
	private static final int SCORE_FOR_MATCHING_NUMERICAL_FIELDS = 10;
	private static final int FLOAT_FIELDS_MULTIPLIER = 5;

	private String keyword;

	public KeywordComperator(String keyword) {
		this.keyword = keyword.toLowerCase();
	}

	@Override
	public int compare(MainFileEntry o1, MainFileEntry o2) {
		int score1 = 0, score2 = 0;
		//Compare how similar they keyword is with the names of the entry and jar file tested
		score1 += scoreString(o1.getName().toLowerCase());
		score2 += scoreString(o2.getName().toLowerCase());
		score1 += scoreString(o1.getJarFileName().toLowerCase());
		score2 += scoreString(o2.getJarFileName().toLowerCase());
		//Compare the keyword with the numerical data from the entry (assuming the keyword is a number)
		try {
			long name = Long.parseLong(keyword);
			if (name == o1.getFailCount()) score1 += SCORE_FOR_MATCHING_NUMERICAL_FIELDS;
			if (name == o1.getPassCount()) score1 += SCORE_FOR_MATCHING_NUMERICAL_FIELDS;
			if (name == o1.getSecutiryTestCount()) score1 += SCORE_FOR_MATCHING_NUMERICAL_FIELDS;
			//If they type in the exact timestamp, ensure that it is ontop
			if (name == o1.getTimestamp()) score1 += SCORE_FOR_MATCHING_NUMERICAL_FIELDS * SCORE_FOR_MATCHING_NUMERICAL_FIELDS;

			if (name == o2.getFailCount()) score2 += SCORE_FOR_MATCHING_NUMERICAL_FIELDS;
			if (name == o2.getPassCount()) score2 += SCORE_FOR_MATCHING_NUMERICAL_FIELDS;
			if (name == o2.getSecutiryTestCount()) score2 += SCORE_FOR_MATCHING_NUMERICAL_FIELDS;
			if (name == o2.getTimestamp()) score2 += SCORE_FOR_MATCHING_NUMERICAL_FIELDS * SCORE_FOR_MATCHING_NUMERICAL_FIELDS;

		} catch (Exception ignore) {
		}

		//Compare the keyword with the code coverage
		try {
			float percent = Float.parseFloat(keyword);
			score1 += scoreString(o1.getPercentCoveredForBasicTests() + "") * FLOAT_FIELDS_MULTIPLIER;
			score2 += scoreString(o2.getPercentCoveredForBasicTests() + "") * FLOAT_FIELDS_MULTIPLIER;
		} catch (Exception ignore) {

		}
		//Subtract to yield a <0, 0 or, >0 number for the Collections.sort method to use
		return score2 - score1;
	}

	/**
	 * Returns the maxiumn number of consecutive characters from keyword in s.<br>
	 * If s contains keyword, it will return the length of keyword.<br>
	 * For instance, if the keyword is "Troy is very cool!", and the string to search is "Troy", this method will return 4 
	 * because there are 4 consecutive characters in the string being searched from the keyword.
	 * @param s The string to parse
	 * @return The maxiumn number of consecutive characters from keyword in s
	 */
	private int scoreString(String s) {
		if (s.isEmpty()) return 0;
		s = new String(s);//Copy the string so we can modify it
		if (s.contains(keyword)) return keyword.length();
		int highest = 0;
		int index;
		while ((index = s.indexOf(keyword.charAt(0))) >= 0) {
			int i = 1;
			while ((index + i < s.length()) && (s.charAt(index + i) == keyword.charAt(i))) {
				i++;
			}
			s = s.substring(index + i);
			highest = Math.max(i, highest);
		}

		return highest;
	}

}
