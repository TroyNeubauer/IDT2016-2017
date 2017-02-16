package contest.winter2017.reader;

import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import com.troyberry.util.*;
import com.troyberry.util.data.*;

import contest.winter2017.ohsfile.*;

public class EntryRenderer extends JPanel implements ListCellRenderer<Object> {

	public EntryRenderer() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new EmptyBorder(10, 10, 10, 10));
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		super.removeAll();
		MainFileEntry entry = new MainFileEntry();
		entry.read(new TroyReader(((String) value).getBytes()));

		add(new JLabel("Name: " + entry.getName()));
		add(new JLabel("Jar Used: " + new File(entry.getJarFileName()).getName()));
		add(new JLabel(
				(entry.getPassCount() + entry.getFailCount()) + " Basic Tests " + entry.getPassCount() + " Pass " + entry.getFailCount() + " Fail "));
		add(new JLabel("Security Tests " + entry.getSecutiryTestCount() + ", " + StringFormatter.clip(entry.getPercentCoveredForBasicTests(), 2)
				+ "% Covered"));
		add(new JLabel("" + new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date(entry.getTimestamp()))));
		if (isSelected) setBackground(Color.LIGHT_GRAY);
		else setBackground(Color.WHITE);
		return this;
	}

}
