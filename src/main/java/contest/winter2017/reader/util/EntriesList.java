package contest.winter2017.reader.util;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.troyberry.util.data.*;

import contest.winter2017.ohsfile.*;
import contest.winter2017.reader.*;
import contest.winter2017.reader.panel.*;
import contest.winter2017.reader.search.*;

/**
 * This class represents the list of entries that for a given OHS save
 * @author Troy Neubauer
 *
 */
public class EntriesList extends JScrollPane {
	private JList<String> list;
	private List<MainFileEntry> entries;
	private DateInfo date;
	private SearchData data;

	public EntriesList(List<MainFileEntry> entries, MainPanel panel) {
		super();
		super.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		super.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.entries = entries;
		list = new JList<String>();
		list.setCellRenderer(new EntryRenderer());
		setSearchData(SearchData.basic());
		if (entries.size() > 0) {
			TroyWriter writer = new TroyWriter();
			entries.get(0).write(writer);
			byte[] bytes = writer.getBytes();
			list.setPrototypeCellValue(new String(bytes));
		}
		list.addListSelectionListener((ListSelectionEvent e) -> {
			if (!e.getValueIsAdjusting()) {
				int index = list.getSelectedIndex();
				panel.fireEntrySelectedEvent(index);
			}
		});
		super.setViewportView(list);
	}

	/**
	 * Sets the search data to be used, then sorts and updates the entries
	 * @param data The new data
	 */
	public void setSearchData(SearchData data) {
		this.data = data;
		if (data == null) {
			setEntries(entries);
		} else {
			if (data.usesSearchText()) {
				Collections.sort(entries, new KeywordComperator(data.getSearchString()));
			} else {
				switch (data.getFilterType()) {
				case NAME:
					Collections.sort(entries, new NameComparator());
					break;
				case DATE:
					Collections.sort(entries, new DateComperator());
					break;
				case JAR_NAME:
					Collections.sort(entries, new JarFileComparator());
					break;
				case BASIC_TESTS_COUNT:
					Collections.sort(entries, new BasicTestCountComperator());
					break;
				case SECURITY_TESTS_COUNT:
					Collections.sort(entries, new SecurityTestCountComperator());
					break;
				case BASIC_TESTS_COVERAGE:
					Collections.sort(entries, new BasicTestsCoverageComperator());
					break;
				}
			}
			List<MainFileEntry> filteredEntries = filterWithDate();
			if (data.isInverted()) Collections.reverse(filteredEntries);
			setEntries(filteredEntries);

		}
	}

	/**
	 * Sets the list of entries in the JList to a new set of entries
	 * @param newEntries The new entries to use
	 */
	private void setEntries(List<MainFileEntry> newEntries) {
		String[] names = new String[newEntries.size()];
		int i = 0;
		TroyWriter writer = new TroyWriter();
		for (MainFileEntry entry : newEntries) {
			writer.clear();
			//Write this entry to a byte array
			entry.write(writer);
			byte[] bytes = writer.getBytes();
			//Very sneaky put the byte array into a String so we can put the String into the JList. 
			// The custom renderer will get the bytes in the string, convert those back to a MainFileEntry, then draw the results
			names[i] = new String(bytes);
			i++;
		}
		list.setListData(names);
		setBorder(BorderFactory.createTitledBorder("Entries (" + String.format("%,d", names.length) + ")"));
		if (!newEntries.isEmpty()) list.setSelectedIndex(0);
	}

	/**
	 * Returns a list of Entries that fit within the set date id date is not null
	 * @return
	 */
	private List<MainFileEntry> filterWithDate() {
		if (date == null) return entries;
		List<MainFileEntry> newEntries = new ArrayList<MainFileEntry>();
		for (MainFileEntry entry : entries) {

			boolean flag = entry.getTimestamp() <= date.getDate().getTime();
			if (date.isBeforeOfAfter()) flag = !flag;
			if (flag) {
				newEntries.add(entry);
			}
		}

		return newEntries;

	}

	public void updateDate(DateInfo date) {
		this.date = date;
		setSearchData(data);
	}

}
