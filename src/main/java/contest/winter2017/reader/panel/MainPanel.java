package contest.winter2017.reader.panel;

import java.awt.*;
import java.io.*;
import java.util.List;

import javax.swing.*;

import com.troyberry.util.*;

import contest.winter2017.ohsfile.*;
import contest.winter2017.reader.*;
import contest.winter2017.reader.search.*;
import contest.winter2017.reader.util.*;

public class MainPanel extends JPanel {

	private MainFile file;
	private List<MainFileEntry> entries;
	private ExecutionInfoPanel executionInfoPanel;
	private EntriesList entriesList;
	private SearchPanel searchPanel;
	private AdditionalFiltersPanel additionalOptionsPanel;
	private GridBagConstraints gb;
	private int tabNumber;

	public MainPanel(MainFile file, int tabNumber) {
		super();
		this.file = file;
		this.tabNumber = tabNumber;
		setLayout(new GridBagLayout());
		entries = file.getAllEntries();
		gb = new GridBagConstraints();
		
		gb.fill = GridBagConstraints.BOTH;
		gb.gridx = 0;
		gb.gridy = 0;
		gb.weightx = 1.5;
		gb.weighty = 1.0;
		gb.gridwidth = 1;
		gb.gridheight = 3;
		add(new MainFileInfoPanel(file, entries), gb);

		gb.gridx = 2;
		gb.gridy = 0;
		gb.weightx = 3.0;
		gb.weighty = 0.0;
		gb.gridwidth = 2;
		gb.gridheight = 1;
		searchPanel = new SearchPanel(file, this);
		add(searchPanel, gb);

		gb.gridx = 1;
		gb.gridy = 0;
		gb.weightx = 3.0;
		gb.weighty = 1.0;
		gb.gridwidth = 1;
		gb.gridheight = 3;
		entriesList = new EntriesList(entries, this);
		add(entriesList, gb);

		gb.gridx = 2;
		gb.gridy = 1;
		gb.weightx = 3.0;
		gb.weighty = 0.0;
		gb.gridwidth = 2;
		gb.gridheight = 1;
		additionalOptionsPanel = new AdditionalFiltersPanel(this);
		add(additionalOptionsPanel, gb);

		gb.gridx = 2;
		gb.gridy = 2;
		gb.weightx = 3.0;
		gb.weighty = 4.0;
		gb.gridwidth = 2;
		gb.gridheight = 1;

		executionInfoPanel = new ExecutionInfoPanel();
		if(entries.size() > 0)fireEntrySelectedEvent(0);

		add(executionInfoPanel, gb);
	}

	public void fireEntrySelectedEvent(int index) {
		if (index < 0) return;
		if (entries.size() > 0) try {
			OHSFile OHSFile = file.getOHSFile(entries.get(index));
			executionInfoPanel.init(OHSFile, null, true, false);
			additionalOptionsPanel.init(OHSFile);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Could not open file\n" + (ReaderMain.DEBUG ? MiscUtil.getStackTrace(e) : e.getMessage()),
					"Error Opening File", JOptionPane.ERROR_MESSAGE);
			System.err.println("Error! Unable to open OHS file at index " + index + " in the main file");
			e.printStackTrace();
		}
	}

	public void reCalculateExecutionInfoPanel() {
		executionInfoPanel.reCalculateText();
	}

	public void reCalculateAdvancedFilter(String filter, boolean ShowOrHide) {
		executionInfoPanel.reCalculateAdvancedFilter(filter, ShowOrHide);
	}
	
	public void fireSearchEvent(SearchData data) {
		entriesList.setSearchData(data);
	}

	public MainFile getFile() {
		return file;
	}

	public int getNumber() {
		return tabNumber;
	}

	public void updateTimeUnit(boolean time12Hour) {
		searchPanel.updateTimeUnit(time12Hour);
	}

	public void updateDate() {
		if(searchPanel != null) entriesList.updateDate(searchPanel.getDate());
	}

}
