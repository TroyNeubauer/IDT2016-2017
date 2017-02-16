package contest.winter2017.reader.panel;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import contest.winter2017.ohsfile.*;
import contest.winter2017.reader.*;
import contest.winter2017.reader.search.*;
import contest.winter2017.reader.util.*;

public class SearchPanel extends JPanel {

	private MainPanel mainPanel;
	private JTextField search;
	private SearchModes searchModes;
	private boolean usesSearchBar = false, flipped = false, beforeOfAfter = false;
	private DateSelectorPanel dateSelectorPanel;
	private JCheckBox enableDateFilter;
	private BooleanButton beforeOrAfterButton;

	public SearchPanel(MainFile file, MainPanel mainPanel) {
		super();
		this.mainPanel = mainPanel;
		setBorder(BorderFactory.createTitledBorder("Search"));
		setLayout(new GridBagLayout());
		
		BooleanButton b = new BooleanButton("A-Z", "Z-A", true);
		b.getState();
		b.setFont(new Font("", Font.BOLD, 15));

		search = new JTextField("", 20);
		search.setFont(new Font("", Font.PLAIN, 20));
		
		
		search.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				usesSearchBar = true;
				mainPanel.fireSearchEvent(new SearchData(search.getText(), flipped));
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
				
		searchModes = new SearchModes();
		
		
		searchModes.addActionListener((ActionEvent e) -> {
			usesSearchBar = false;
			mainPanel.fireSearchEvent(new SearchData(searchModes.getType(), flipped));
		});

		JLabel order = new JLabel(" Order: ");
		order.setFont(new Font("", Font.BOLD, 15));
		
		JLabel sortBy = new JLabel(" Sory By: ");
		sortBy.setFont(new Font("", Font.BOLD, 15));
				
		b.addActionListener((ActionEvent e) -> {
			flipped = !flipped;
			if (usesSearchBar) mainPanel.fireSearchEvent(new SearchData(search.getText(), flipped));
			else mainPanel.fireSearchEvent(new SearchData(searchModes.getType(), flipped));

		});
		
		beforeOrAfterButton = new BooleanButton(" After ", "Before", false);
		beforeOrAfterButton.addActionListener((ActionEvent e) -> {
			beforeOfAfter = !beforeOfAfter;
			updateDate();
		});
		beforeOrAfterButton.setEnabled(false);
		
		enableDateFilter = new JCheckBox("Enable Date Filter");
		enableDateFilter.addActionListener((ActionEvent e) -> {
			beforeOrAfterButton.setEnabled(enableDateFilter.isSelected());
			dateSelectorPanel.setEnabled(enableDateFilter.isSelected());
			updateDate();
		});
		
		dateSelectorPanel = new DateSelectorPanel(this);
		dateSelectorPanel.setEnabled(false);
		
		GridBagConstraints g = new GridBagConstraints();
		g.weightx = 1.0;
		g.weighty = 1.0;
		g.gridx = 0;
		g.gridy = 0;
		g.gridwidth = 1;
		g.gridheight = 1;
		g.fill = GridBagConstraints.NONE;
		g.anchor = GridBagConstraints.NORTHWEST;
		g.insets = new Insets(0, 10, 0, 10);
		
		int x = 0;
		g.gridx = x;
		g.gridwidth = 3;
		add(search, g);
		
		x += 3;
		g.gridx = x;
		g.gridwidth = 1;
		add(sortBy, g);
		
		x += 1;
		g.gridx = x;
		g.gridwidth = 2;
		add(searchModes, g);
		
		x += 2;
		g.gridwidth = 1;
		g.gridx = x;
		add(order, g);

		x++;
		g.gridwidth = 4;
		g.gridx = x;
		add(b, g);

		x = 0;
		g.gridwidth = 1;
		g.gridx = x;
		g.gridy = 1;
		add(enableDateFilter, g);

		x++;
		g.gridx = x;
		g.gridy = 1;
		add(beforeOrAfterButton, g);

		x++;
		int inserts = 5;
		g.insets = new Insets(0, inserts, 0, inserts);
		g.gridx = x;
		g.gridy = 1;
		g.gridwidth = 5;
		add(dateSelectorPanel, g);

	}

	public void updateTimeUnit(boolean time12Hour) {
		dateSelectorPanel.updateTimeUnit(time12Hour);
	}
	
	/**
	 * Called when the Date
	 */
	public void updateDate() {
		mainPanel.updateDate();
	}
	
	/**
	 * Getter for the selected date and the stats of the Before/ After button
	 */
	public DateInfo getDate() {
		if(enableDateFilter.isSelected()) {
			return new DateInfo(dateSelectorPanel.getDate(), beforeOfAfter);
		}
		return null;
	}

}
