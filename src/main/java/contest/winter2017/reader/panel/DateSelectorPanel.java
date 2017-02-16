package contest.winter2017.reader.panel;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import contest.winter2017.reader.*;

/**
 * This class holds many combo boxes for selecting and managing a date
 * @author Troy Neubauer
 *
 */
public class DateSelectorPanel extends JPanel {

	private SearchPanel panel;
	private static final int MIN_YEAR = 2015;
	private static final String[] MONTHS = new String[] { "January", "Febuary", "March", "April", "May", "June", "July", "August", "September",
			"October", "November", "December" };
	private JComboBox years, months, days, hours, minutes, seconds;
	private Calendar date = Calendar.getInstance();
	private JButton zeroButton;

	public DateSelectorPanel(SearchPanel panel) {
		this.panel = panel;
		this.years = new JComboBox();
		this.months = new JComboBox();
		this.days = new JComboBox();
		this.hours = new JComboBox();
		this.minutes = new JComboBox();
		this.seconds = new JComboBox();
		buildYearsList(years);
		buildMonthsList(months);
		buildDaysList(date, days, months);
		buildHoursList(hours, Options.time12Hour);
		buildMinutesList(minutes);
		buildSecondsList(seconds);
		
		years.setSelectedIndex(date.get(Calendar.YEAR) - MIN_YEAR + 1);
		months.setSelectedIndex(12 - date.get(Calendar.MONTH));
		days.setSelectedIndex(date.get(Calendar.DAY_OF_MONTH) - 1);
		hours.setSelectedIndex(date.get(Calendar.HOUR_OF_DAY));
		minutes.setSelectedIndex(date.get(Calendar.MINUTE));
		seconds.setSelectedIndex(date.get(Calendar.SECOND));
		
		years.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				date.set(Calendar.YEAR, Integer.parseInt(String.valueOf(years.getSelectedItem())));
				buildDaysList(date, days, months);
				panel.updateDate();
			}
		});
		months.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				date.set(Calendar.MONTH, months.getSelectedIndex());
				buildDaysList(date, days, months);
				panel.updateDate();
			}
		});

		days.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				date.set(Calendar.DATE, Integer.parseInt(days.getSelectedItem().toString()));
				panel.updateDate();
			}
		});
		
		hours.addItemListener((ItemEvent e) -> {
			date.set(Calendar.HOUR_OF_DAY, hours.getSelectedIndex());
			if (e.getStateChange() == ItemEvent.SELECTED) panel.updateDate();
		});
		
		minutes.addItemListener((ItemEvent e) -> {
			date.set(Calendar.MINUTE, Integer.parseInt(minutes.getSelectedItem().toString()));
			if (e.getStateChange() == ItemEvent.SELECTED) panel.updateDate();
		});
		
		seconds.addItemListener((ItemEvent e) -> {
			date.set(Calendar.SECOND, Integer.parseInt(seconds.getSelectedItem().toString()));
			if (e.getStateChange() == ItemEvent.SELECTED) panel.updateDate();
		});

		add(months);
		add(days);
		add(years);
		add(hours);
		add(new JLabel(":"));
		add(minutes);
		add(new JLabel(":"));
		add(seconds);
		zeroButton = new JButton("Zero");
		add(zeroButton);
		zeroButton.addActionListener((ActionEvent e) -> {
			hours.setSelectedIndex(0);
			minutes.setSelectedIndex(0);
			seconds.setSelectedIndex(0);
		});
		Calendar c = Calendar.getInstance();
		date.set(Calendar.YEAR, c.get(Calendar.YEAR));
		date.set(Calendar.MONTH, c.get(Calendar.MONTH));
		date.set(Calendar.DATE, c.get(Calendar.DATE));
	}

	private void sendUpdate() {
		panel.updateDate();
	}

	/**
	 * This method builds the list of months
	 * @param years The combo box containing the years
	 */
	private void buildYearsList(JComboBox years) {
		int selectedOld = years.getSelectedIndex();
		years.removeAllItems();
		for (int year = MIN_YEAR; year < date.get(Calendar.YEAR) + 2; year++)
			years.addItem(Integer.toString(year));
		years.setSelectedIndex(Math.min(selectedOld, years.getItemCount()));
	}

	/**
	 * This method builds the list of months
	 * @param months The combo box containing the months
	 */
	private void buildMonthsList(JComboBox months) {
		int selectedOld = months.getSelectedIndex();
		months.removeAllItems();
		for (int monthCount = 0; monthCount < 12; monthCount++)
			months.addItem(MONTHS[monthCount]);
		months.setSelectedIndex(Math.min(selectedOld, months.getItemCount()));
	}

	/**
	 * This method builds the list of years
	 * @param dateIn The current date, which will be used for
	 * the initial date of the lists
	 * @param days The combo box that will contain the days
	 * @param months The combo box that will contain the months
	 */
	private void buildDaysList(Calendar dateIn, JComboBox days, JComboBox months) {
		int selectedOld = days.getSelectedIndex();
		days.removeAllItems();
		dateIn.set(Calendar.MONTH, months.getSelectedIndex());
		int lastDay = date.getActualMaximum(Calendar.DAY_OF_MONTH);

		for (int dayCount = 1; dayCount <= lastDay; dayCount++)
			days.addItem(Integer.toString(dayCount));

		days.setSelectedIndex(Math.min(selectedOld, lastDay - 1));
	}

	/**
	 * This method builds the list of months
	 * @param hours The combo box containing the hours
	 * @param time12Hour 
	 */
	private void buildHoursList(JComboBox hours, boolean time12Hour) {
		int selectedOld = hours.getSelectedIndex();
		hours.removeAllItems();
		for (int hour = 0; hour < 24; hour++)
			hours.addItem(getHour(hour, time12Hour));
		hours.setSelectedIndex(Math.min(selectedOld, hours.getItemCount()));
	}

	/**
	 * This method builds the list of months
	 * @param minutes The combo box containing the minuites
	 */
	private void buildMinutesList(JComboBox minutes) {
		int selectedOld = minutes.getSelectedIndex();
		minutes.removeAllItems();
		for (int minute = 0; minute < 60; minute++)
			minutes.addItem(Integer.toString(minute));
		minutes.setSelectedIndex(Math.min(selectedOld, minutes.getItemCount()));
	}

	/**
	 * This method builds the list of months
	 * @param seconds The combo box containing the seconds
	 */
	private void buildSecondsList(JComboBox seconds) {
		int selectedOld = seconds.getSelectedIndex();
		seconds.removeAllItems();
		for (int second = 0; second < 60; second++)
			seconds.addItem(Integer.toString(second));
		seconds.setSelectedIndex(Math.min(selectedOld, seconds.getItemCount()));
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		years.setEnabled(enabled);
		months.setEnabled(enabled);
		days.setEnabled(enabled);
		hours.setEnabled(enabled);
		minutes.setEnabled(enabled);
		seconds.setEnabled(enabled);
		zeroButton.setEnabled(enabled);
	}

	/**
	 * Rebuilds the list of hours to display the new time unit
	 * @param time12Hour
	 */
	public void updateTimeUnit(boolean time12Hour) {
		buildHoursList(hours, time12Hour);
	}
	
	/**
	 * Returns a String indicating the hour in 12 or 24 hour time
	 * @param hour The hour to use
	 * @param time12Hour Weather or not the time is in 12 or 24 hour
	 * @return The formatted time
	 */
	private String getHour(int hour, boolean time12Hour) {
		if (hour < 0 || hour >= 24) throw new IllegalArgumentException("Illegal hour " + hour);
		if (!time12Hour) return Integer.toString(hour);
		if (hour == 0) return "12 AM";
		if (hour < 12) return Integer.toString(hour) + " AM";
		if (hour == 12) return "12 PM";
		return (hour - 12) + " PM";
	}

	/**
	 * Getter for the current date
	 * @return The current date
	 */
	public Date getDate() {
		return new Date(date.getTimeInMillis());
	}
}
