package contest.winter2017.reader;

import java.awt.event.*;

import javax.swing.*;

public class BooleanButton extends JButton {
	
	private String trueText, falseText;
	private boolean state;

	public BooleanButton(String trueText, String falseText, boolean initalState) {
		super(initalState ? trueText : falseText);
		this.trueText = trueText;
		this.falseText = falseText;
		this.state = initalState;
		addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				flip();
			}
		});
	}
	
	public void flip() {
		state = !state;
		setText(state ? trueText : falseText);
	}

	public boolean getState() {
		return state;
	}

}
