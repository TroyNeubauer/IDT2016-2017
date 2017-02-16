package contest.winter2017.reader.panel;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import contest.winter2017.ohsfile.*;
import contest.winter2017.reader.*;

/**
 * This class represents the additional filters used to filter for specific errors when an Entry is shown
 * @author Troy Neubauer
 *
 */
public class AdditionalFiltersPanel extends JPanel {

	private JCheckBox enableSpecficFilters;
	private JComboBox<String> errors;
	private BooleanButton showOrHideBtn;
	private boolean showOrHide = false;

	public AdditionalFiltersPanel(MainPanel panel) {
		setBorder(BorderFactory.createTitledBorder("Additional Filters"));
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		showOrHideBtn = new BooleanButton(" Hide ", "Show", false);
		showOrHideBtn.addActionListener((ActionEvent e) -> {
			showOrHide = !showOrHide;
			if (errors.getItemCount() > 0 && enableSpecficFilters.isSelected()) {
				panel.reCalculateAdvancedFilter(errors.getSelectedItem().toString(), showOrHide);
			}
			
		});

		errors = new JComboBox<String>();
		errors.setPrototypeDisplayValue("java.lang.ArrayIndexOutOfBoundsException");
		errors.addActionListener((ActionEvent e) -> {
			if (errors.getItemCount() > 0) {
				panel.reCalculateAdvancedFilter(errors.getSelectedItem().toString(), showOrHide);
			}
		});

		enableSpecficFilters = new JCheckBox("Enable Other filters");
		enableSpecficFilters.setSelected(false);
		enableSpecficFilters.addActionListener((ActionEvent e) -> {
			errors.setEnabled(enableSpecficFilters.isSelected());
			showOrHideBtn.setEnabled(enableSpecficFilters.isSelected());
			if (errors.getItemCount() > 0) {
				panel.reCalculateAdvancedFilter(enableSpecficFilters.isSelected() ? errors.getSelectedItem().toString() : null, showOrHide);
			}
		});
		add(enableSpecficFilters);
		add(showOrHideBtn);
		add(errors);
		errors.setEnabled(enableSpecficFilters.isSelected());
		showOrHideBtn.setEnabled(enableSpecficFilters.isSelected());
	}
	
	/**
	 * Re initializes this panel by reading the new errors in the new OHS file and displaying them in the ComboBox
	 * @param file
	 */
	public void init(OHSFile file) {
		List<String> errorsList = new ArrayList<String>();
		for (SecurityTest t : file.getSecurityTests()) {
			String error = Utils.getErrorType(t.getOutputErr());
			if (error == null || error.isEmpty()) continue;
			if (!errorsList.contains(error)) errorsList.add(error);
		}
		ComboBoxModel<String> model = new DefaultComboBoxModel(errorsList.toArray());
		errors.setModel(model);
	}

}
