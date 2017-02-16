package contest.winter2017.reader.panel;

import java.io.*;
import java.util.*;

import javax.swing.*;

import com.troyberry.util.*;

import contest.winter2017.ohsfile.*;

public class MainFileInfoPanel extends JScrollPane {
	
	private JPanel panel;

	public MainFileInfoPanel(MainFile file, List<MainFileEntry> entries) {
		super();
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createTitledBorder("Main File Details"));
		add("Entry Count: " + String.format("%,d", entries.size()));
		add("Source File: ");
		add("" + file.getTopFile());
		File mainFile = file.getMainFile();
		if(mainFile.exists()) {
			add("LastModified: ");
			add(MiscUtil.epochToString(mainFile.lastModified()));
		}
		int totalTests = 0;
		for(MainFileEntry e : entries) {
			totalTests += e.getPassCount();
			totalTests += e.getFailCount();
			totalTests += e.getSecutiryTestCount();
		}
		add("Total Tests*: " + String.format("%,d", totalTests));
		add("*The total of all Black Box and Basic tests for each entry");
		
		
		super.setViewportView(panel);
	}
	
	private void add(String text) {
		panel.add(new JLabel(text));
	}

}
