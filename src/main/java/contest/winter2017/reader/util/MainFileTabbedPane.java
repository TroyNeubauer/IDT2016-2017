package contest.winter2017.reader.util;

import java.util.*;

import javax.swing.*;

import contest.winter2017.ohsfile.*;
import contest.winter2017.reader.panel.*;

public class MainFileTabbedPane extends JTabbedPane {
	private ArrayList<MainPanel> tabs;
	private int tabCount = 0;
	
	public MainFileTabbedPane() {
		super(JTabbedPane.TOP);
		tabs = new ArrayList<MainPanel>();
	}
	
	public void add(MainFile file, int tabNumber) {
		int index = getTabCount();
		MainPanel panel = new MainPanel(file, tabNumber);
		tabs.add(index, panel);
		insertTab("Main File #" + (tabNumber + 1), null, panel, "", index);
		setSelectedIndex(index);
	}
	
	public void add(MainFile file) {
		add(file, tabCount++);
	}
	
	public boolean hasTabs() {
		return getTabCount() > 0;
	}

	public void remove(MainFile file) {
		int index = tabs.indexOf(file);
		if(index >= 0) {
			tabs.remove(index);
			super.removeTabAt(index);
		}
	}
	
	public void removeSelected() {
		super.removeTabAt(super.getSelectedIndex());
		tabs.remove(super.getSelectedIndex());
	}

	public void refresh() {
		List<MainFile> files = new ArrayList<MainFile>();
		List<Integer> numbers = new ArrayList<Integer>();
		for(MainPanel p : tabs) {
			files.add(p.getFile());
			numbers.add(p.getNumber());
		}
		removeAll();
		tabs.clear();
		int i = 0;
		for(MainFile f : files) {
			add(f, numbers.get(i));
			i++;
		}
	}
	
}
