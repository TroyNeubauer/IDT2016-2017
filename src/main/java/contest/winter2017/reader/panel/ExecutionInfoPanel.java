package contest.winter2017.reader.panel;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.text.*;

import com.troyberry.util.*;

import contest.winter2017.ohsfile.*;
import contest.winter2017.reader.*;
import contest.winter2017.reader.Options.*;

public class ExecutionInfoPanel extends JScrollPane {

	private JTextArea area;
	private OHSFile file;
	private int errorCountForSecutiryTests;
	private Map<String, Integer> errors;
	private String targetError = null;
	private boolean ShowOrHide;

	public ExecutionInfoPanel() {
		super();
		this.errors = new HashMap<String, Integer>();
	}

	private boolean getErrors(ExecutedTest test) {
		String error = Utils.getErrorType(test.getOutputErr());
		if (error == null || error.isEmpty()) return false;
		if (!errors.keySet().contains(error)) {
			errors.put(error, 1);
		} else {
			int count = errors.get(error);
			errors.replace(error, count, count + 1);
		}
		return true;
	}

	public void init(OHSFile file, String targetError, boolean ShowOrHide, boolean keepPos) {
		if (area == null) keepPos = false;
		int prevPos = super.getViewport().getViewPosition().y;
		this.file = file;
		this.targetError = targetError;
		this.ShowOrHide = ShowOrHide;
		this.errorCountForSecutiryTests = 0;
		this.errors.clear();
		//Create the new text area to get rid of all the old text
		area = new JTextArea("");
		area.setEditable(false);
		area.setOpaque(false);
		super.setViewportView(area);//Set the new viewport for the scroll pane
		if (keepPos) super.getViewport().setViewPosition(new Point(0, prevPos));//Reset the scroll pane to the previous position if we want to keep the pos
		
		//Set the scroll pane to never move when we write to the text area
		DefaultCaret caret = (DefaultCaret) area.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		setBorder(BorderFactory.createTitledBorder("Execution Info"));
		add("Jar tested: " + new File(file.getJarFileName()).getName(), 0);
		add("OHS File Version : " + file.getVersion(), 0);
		add("Time Stamp: " + MiscUtil.epochToString(file.getTimestamp()), 0);
		if (Options.includeBasicTestsInErrSummary) {
			for (BasicTest t : file.getBasicTests()) {
				getErrors(t);
			}
		}
		for (SecurityTest t : file.getSecurityTests()) {
			if (getErrors(t)) errorCountForSecutiryTests++;
		}
		if (errors.keySet().size() > 0) add("Error summary:", 0);
		for (String s : errors.keySet())
			add("Error: " + s + " times " + errors.get(s), 5);

		if (errors.keySet().size() > 0) {
			if (Options.currentErrorType == ErrorType.ANY_IN_ERR_STREAM_IS_ERROR) add((file.getSecurityTests().size() - errorCountForSecutiryTests)
					+ " Non reconisible errors (Errors that don't look like a throwable but have an err stream)", 5);
		}

		if (Options.securityTestsFirst) {
			showSecutiry(file);
			showBasic(file);
		} else {
			showBasic(file);
			showSecutiry(file);
		}
		setWheelScrollingEnabled(true);
	}

	private void showBasic(OHSFile file) {
		add("Basic Tests:", 0);
		add("Pass Count: " + file.getPassCount() + "  Fail Count: " + file.getFailCount() + "  Total: " + (file.getPassCount() + file.getFailCount())
				+ " Total Percent Covered: " + StringFormatter.clip(file.getTotalPercentCovered(), 2), 5);
		List<BasicTest> failTests = file.getFailedBasicTests();
		if (!failTests.isEmpty()) {
			add("Failed tests: ", 5);
			for (BasicTest test : failTests) {
				add("args: " + test.getArgsUsed(), 10);
				add("Out stream: " + test.getOutputOut(), 10);
				if (Options.hideErrStreamsIfBlank && !test.getOutputErr().isEmpty() || !Options.hideErrStreamsIfBlank)
					add("Err stream: " + test.getOutputErr(), 10);
				add("Expected out stream: " + test.getExpectedOut(), 10);
				if (Options.hideErrStreamsIfBlank && !test.getOutputErr().isEmpty() || !Options.hideErrStreamsIfBlank)
					add("Expected err stream: " + test.getExpectedErr(), 10);
				add("", 0);
			}
		}
	}
	
	/**
	 * Adds a line of text with the desired indent to the text area
	 * @param s The String to add
	 * @param indent The number of spaces to add before the String
	 */
	private void add(String s, int indent) {
		for (int i = 0; i < indent; i++) {
			s = " " + s;
		}
		area.append(s + "\n");
	}

	private void showSecutiry(OHSFile file) {
		List<SecurityTest> securityTests = file.getSecurityTests();
		add("Secutiry Tests:  Total: " + securityTests.size(), 0);
		if (!securityTests.isEmpty()) {
			for (SecurityTest test : securityTests) {
				String error = Utils.getErrorType(test.getOutputErr());
				if (Options.onlyShowTestsThatAreErrors && Options.currentErrorType == ErrorType.ONLY_ERR_STREAMS_THAT_INCLUDE_JAVA_ERRORS
						&& error.isEmpty()) {
					continue;
				}
				boolean flag = error.equals(targetError);
				if (ShowOrHide) {
					flag = !flag;
				}
				if ((targetError != null && flag) || targetError == null) {
					if (Options.onlyShowTestsThatAreErrors && test.getOutputErr().isEmpty()) continue;
					add("% covered " + StringFormatter.clip(test.getPercentCovered(), 2), 5);
					add("args: " + (test.getArgsUsed().isEmpty() ? "<No args>" : test.getArgsUsed()), 5);
					add("Out stream: " + (test.getOutputOut().isEmpty() ? "<No out>" : test.getOutputOut()), 5);
					if (Options.hideErrStreamsIfBlank && !test.getOutputErr().isEmpty() || !Options.hideErrStreamsIfBlank) {
						add("Err stream: " + (test.getOutputErr().isEmpty() ? "<No err>" : test.getOutputErr()), 5);
						if (!error.isEmpty()) add("Error found: " + error, 10);
					}
					area.append("\n");
				}
			}
		}
	}

	public OHSFile getFile() {
		return file;
	}

	public void reCalculateText() {
		init(file, targetError, ShowOrHide, true);
	}

	public void reCalculateAdvancedFilter(String filter, boolean ShowOrHide) {
		init(file, filter, ShowOrHide, true);
	}
}
