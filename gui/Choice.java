/**
 * Creates the widgets to represent a choice entry.
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

public class Choice extends JCheckBox {
	private JTextArea textArea = new JTextArea();
	private int choiceNumber;

	/**
	 * Creates an instance.
	 * 
	 * @param text
	 *            the text of the choice
	 * @param newchoiceNumber
	 *            its ordinal number
	 */
	Choice(String text, int newchoiceNumber, int fontSize) {
		super(new Character((char) ('A' + newchoiceNumber)).toString());
		// answers can be selected by typing their letters with no modifier
		this.registerKeyboardAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Choice.this.setSelected(!Choice.this.isSelected());
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_A + newchoiceNumber, 0),
		JComponent.WHEN_IN_FOCUSED_WINDOW);
		choiceNumber = newchoiceNumber;
		// create answer check-box
		setAlignmentY(Component.TOP_ALIGNMENT);

		// create answer text-area
		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textArea.setMargin(new Insets(5, 5, 5, 5));
		textArea.setFocusable(false);
		// makes it possible to select the answer by just clicking the text rather
		// than the check-box
		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Choice.this.setSelected(!Choice.this.isSelected());
			}
		});
		setAnswer(text);
	}

	int getChoiceNumber() {
		return choiceNumber;
	}

	void addTo(Container container) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = choiceNumber;
		c.gridx = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1.0;
		container.add(this, c);
		c.gridx = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		container.add(textArea, c);
	}

	void setAnswer(String text) {
		textArea.setText(text);
		textArea.revalidate();
	}

	void setFontSize(int size) {
		textArea.setFont(textArea.getFont().deriveFont((float)size));
	}

	@Override
	public void setVisible(boolean visibility) {
		super.setVisible(visibility);
		textArea.setVisible(visibility);
	}
}