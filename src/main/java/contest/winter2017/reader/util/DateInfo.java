package contest.winter2017.reader.util;

import java.util.*;

/**
 * This class just holds a date and weather of not entries before of after that date need to be shown
 * @author Troy Neubauer
 *
 */
public class DateInfo {

	private Date date;
	private boolean beforeOfAfter;

	private DateInfo() {
	}

	public DateInfo(Date date, boolean beforeOfAfter) {
		this.date = date;
		this.beforeOfAfter = beforeOfAfter;
	}

	public Date getDate() {
		return date;
	}

	public boolean isBeforeOfAfter() {
		return beforeOfAfter;
	}

	@Override
	public String toString() {
		return date.toString() + " Before or after " + beforeOfAfter;
	}

}
