/**
 * A panel that shows the question
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class QuestionPanel extends JScrollPane {
	private JTextArea textArea = new JTextArea();

	QuestionPanel(int fontSize) {
		setViewportView(textArea);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
		textArea.setMargin(new Insets(5, 5, 5, 5));
		textArea.setColumns(70);
		textArea.setFocusable(false);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		reset();
	}

	void setText(String text) {
		textArea.setText(text);
	}

	void reset() {
		setText("Select the number of questions to be tested and " +
				"the time limit, then start choosing questions " +
				"from the panel on the left.");
	}

	void setFontSize(int size) {
		textArea.setFont(textArea.getFont().deriveFont((float)size));
	}
// necessary for for the text area to be the right size for the text
	@Override
	public Dimension getMaximumSize() {
		return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
	}
}
