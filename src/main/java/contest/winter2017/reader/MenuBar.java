package contest.winter2017.reader;

import java.awt.event.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

public class MenuBar extends JMenuBar {

	public MenuBar() {
		JMenu file = new JMenu("File");
		ImageIcon openIcon = null, closeIcon = null, optionsIcon = null, refreshIcon = null, helpIcon = null;
		try {
			openIcon = new ImageIcon(ImageIO.read(Class.class.getResource("/open.png")));
			closeIcon = new ImageIcon(ImageIO.read(Class.class.getResource("/close.png")));
			helpIcon = new ImageIcon(ImageIO.read(Class.class.getResource("/help.png")));
			optionsIcon = new ImageIcon(ImageIO.read(Class.class.getResource("/options.png")));
			refreshIcon = new ImageIcon(ImageIO.read(Class.class.getResource("/refresh.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		JMenuItem open = new JMenuItem("Open", openIcon);
		open.addActionListener((ActionEvent e) -> {
			ReaderMain.showOpenDialog(null);
		});
		
		JMenuItem close = new JMenuItem("Close", closeIcon);
		close.addActionListener((ActionEvent e) -> {
			ReaderMain.getFrame().removeSelectedTab();
		});
		file.add(open);
		file.add(close);
		
		add(file);
		
		JMenu options = new JMenu("Options");
		JMenuItem optionsItem = new JMenuItem("Open Options", optionsIcon);
		optionsItem.addActionListener((ActionEvent e) -> {
			OptionsWindow.showWindow();
		});
		options.add(optionsItem);
		
		JMenuItem refreshItem = new JMenuItem("Refresh Open Files", refreshIcon);
		refreshItem.addActionListener((ActionEvent e) -> {
			ReaderMain.getFrame().refresh();
		});
		options.add(refreshItem);
		
		add(options);
		
		JMenu help = new JMenu("Help");
		help.add(new JMenuItem("Open Help", helpIcon));
		add(help);
	}

}
