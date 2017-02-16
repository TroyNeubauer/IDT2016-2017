package contest.winter2017.reader;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import contest.winter2017.reader.Options.*;
import contest.winter2017.reader.panel.*;

public class OptionsWindow extends JFrame {
	private static OptionsWindow window;
	private static boolean open = false;
	private static JPanel panel = new JPanel(new GridBagLayout());
	private static GridBagConstraints gbc = new GridBagConstraints();
	
	private static JCheckBox showSecutiryTestsFirst, hideErrStreamsIfBlank, onlyShowTestsThatAreErrors, includeBasicTestsInErrSummary;
	
	static {
		init();
	}
	
	public static void init() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		window = new OptionsWindow();
		window.setSize(650, 540);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		window.setVisible(false);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		showSecutiryTestsFirst = new JCheckBox("Show Secutiry Tests First", Options.securityTestsFirst);
		panel.add(showSecutiryTestsFirst, gbc);
		showSecutiryTestsFirst.addActionListener((ActionEvent e) -> {
			Options.securityTestsFirst = !Options.securityTestsFirst;
			for(MainPanel p : ReaderMain.getFrame().getPanels()) {
				p.reCalculateExecutionInfoPanel();
			}
		});
		
		gbc.gridy++;
		hideErrStreamsIfBlank = new JCheckBox("Hide Err Streams If Blank", Options.hideErrStreamsIfBlank);
		panel.add(hideErrStreamsIfBlank, gbc);
		hideErrStreamsIfBlank.addActionListener((ActionEvent e) -> {
			Options.hideErrStreamsIfBlank = !Options.hideErrStreamsIfBlank;
			for(MainPanel p : ReaderMain.getFrame().getPanels()) {
				p.reCalculateExecutionInfoPanel();
			}
		});
		
		
		gbc.gridy++;
		onlyShowTestsThatAreErrors = new JCheckBox("Only Show Tests That Have Errors", Options.onlyShowTestsThatAreErrors);
		panel.add(onlyShowTestsThatAreErrors, gbc);
		onlyShowTestsThatAreErrors.addActionListener((ActionEvent e) -> {
			Options.onlyShowTestsThatAreErrors = !Options.onlyShowTestsThatAreErrors;
			for(MainPanel p : ReaderMain.getFrame().getPanels()) {
				p.reCalculateExecutionInfoPanel();
			}
		});
		
		gbc.gridy++;
		includeBasicTestsInErrSummary = new JCheckBox("Include Basic Tests In Err Summary", Options.includeBasicTestsInErrSummary);
		panel.add(includeBasicTestsInErrSummary, gbc);
		includeBasicTestsInErrSummary.addActionListener((ActionEvent e) -> {
			Options.includeBasicTestsInErrSummary = !Options.includeBasicTestsInErrSummary;
			for(MainPanel p : ReaderMain.getFrame().getPanels()) {
				p.reCalculateExecutionInfoPanel();
			}
		});
		
		
		gbc.gridy++;
		panel.add(new JLabel("What should count as an error"), gbc);
		
		gbc.gridy++;
		ButtonGroup errorType = new ButtonGroup();
		JRadioButton anyInErrStreamIsError = new JRadioButton("Any data in the error stream counts as an error");
		if(Options.currentErrorType == Options.ErrorType.ANY_IN_ERR_STREAM_IS_ERROR) {
			anyInErrStreamIsError.setSelected(true);
		}
		anyInErrStreamIsError.addActionListener((ActionEvent e) -> {
			Options.currentErrorType = ErrorType.ANY_IN_ERR_STREAM_IS_ERROR;
			for(MainPanel p : ReaderMain.getFrame().getPanels()) {
				p.reCalculateExecutionInfoPanel();
			}
		});
		panel.add(anyInErrStreamIsError, gbc);
		errorType.add(anyInErrStreamIsError);
		
		gbc.gridy++;
		JRadioButton onlyActualErrorsAreErrors = new JRadioButton("Only actual error statments like\"java.lang.NullPointerException\" count as errors.");
		if(Options.currentErrorType == Options.ErrorType.ONLY_ERR_STREAMS_THAT_INCLUDE_JAVA_ERRORS) {
			onlyActualErrorsAreErrors.setSelected(true);
		}
		onlyActualErrorsAreErrors.addActionListener((ActionEvent e) -> {
			Options.currentErrorType = ErrorType.ONLY_ERR_STREAMS_THAT_INCLUDE_JAVA_ERRORS;
			for(MainPanel p : ReaderMain.getFrame().getPanels()) {
				p.reCalculateExecutionInfoPanel();
			}
		});
		panel.add(onlyActualErrorsAreErrors, gbc);
		errorType.add(onlyActualErrorsAreErrors);
		
		gbc.gridy++;
		panel.add(new JLabel("Time in: "), gbc);
		
		gbc.gridx = 1;
		BooleanButton hour12Or24 = new BooleanButton("12 Hour", "24 Hour", Options.time12Hour);
		hour12Or24.addActionListener((ActionEvent e) -> {
			Options.time12Hour = !Options.time12Hour;
			for(MainPanel p : ReaderMain.getFrame().getPanels()) {
				p.updateTimeUnit(Options.time12Hour);
			}
		});
		panel.add(hour12Or24, gbc);
		
		window.add(panel);
	}

	private OptionsWindow() throws HeadlessException {
		super("Options");
	}
	
	public static void showWindow() {
		window.setVisible(true);
	}
	
	public static void hideWindow() {
		window.setVisible(false);
	}

}
