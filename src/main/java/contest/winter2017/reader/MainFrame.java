package contest.winter2017.reader;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import contest.winter2017.ohsfile.*;
import contest.winter2017.reader.panel.*;
import contest.winter2017.reader.util.*;

public class MainFrame extends JFrame {
	
	private MainFileTabbedPane pane;
	private GettingStartedPanel gettingStartedPanel;
	private boolean start = true;

	public MainFrame(String title) throws HeadlessException {
		super(title);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException ignore) {
			
		}
		gettingStartedPanel = new GettingStartedPanel();
		
		setLayout(new BorderLayout());
		MenuBar bar = new MenuBar();
		add(bar, BorderLayout.NORTH);
		pane = new MainFileTabbedPane();
		add(gettingStartedPanel, BorderLayout.CENTER);
		
	}
	
	public void addMainFile(MainFile file) throws IOException {
		if(start){
			remove(gettingStartedPanel);
			add(pane, BorderLayout.CENTER);
			start = false;
		}
		pane.add(file);
	}
	
	public void removeMainFile(MainFile file) throws IOException {
		pane.remove(file);
	}
	
	public void removeSelectedTab() {
		if(pane.getTabCount() > 0)pane.remove(pane.getSelectedIndex());
	}
	
	public MainPanel[] getPanels() {
		MainPanel[] result = new MainPanel[pane.getTabCount()];
		for(int i = 0; i < pane.getTabCount(); i++) {
			result[i] = (MainPanel) pane.getComponentAt(i);
		}
		return result;
	}
	
	public void refresh() {
		pane.refresh();
	}

}
