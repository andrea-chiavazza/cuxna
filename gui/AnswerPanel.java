/**
 * This class creates a widget that shows the multiple choices for the answer.
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cuxna.data.QuestionData;

public class AnswerPanel extends JScrollPane implements ChangeListener {
	class ScrollablePanel extends JPanel implements Scrollable {
		ScrollablePanel() {
			super(new GridBagLayout());
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect,
				int orientation, int direction) {
			return 10;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect,
				int orientation, int direction) {
			return 10;
		}
	}
	private ScrollablePanel panel = new ScrollablePanel();
	private JComponent filler = new Box.Filler(
			new Dimension(), new Dimension(), new Dimension());
	private List<Choice> answerWidgets = new ArrayList<Choice>();
	private int fontSize;

	AnswerPanel(int size) {
		fontSize = size;
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		setViewportView(panel);
	}

	/**
	 * Updates all the answers check-boxes
	 * 
	 * @param answer
	 *            the answer used to update the checkboxes
	 */
	void setAnswer(boolean[] answer) {
		for (int i = 0; i < answer.length; i++) {
			answerWidgets.get(i).setSelected(answer[i]);			
		}
	}

	/**
	 * Shows a Question object through the widgets.
	 * 
	 * @param question
	 *            the Question object
	 */
	void showAnswers(QuestionData question) {
		String[] answers = question.getAnswers();
		for (int choiceNumber = 0; choiceNumber < answers.length; choiceNumber++) {
			// there are more answers than widgets
			if (choiceNumber >= answerWidgets.size()) {
				// creates a new Choice object
				Choice answer = new Choice(answers[choiceNumber], choiceNumber, fontSize);
				answer.addChangeListener(this);
				answer.addTo(panel);
				// add the entry to answerWidgets
				answerWidgets.add(answer);
				// set filler grid-y
				GridBagConstraints c = ((GridBagLayout)(panel.getLayout())).getConstraints(filler);
				c.gridy = choiceNumber + 1;
				((GridBagLayout)(panel.getLayout())).setConstraints(filler, c);
			} else {
				answerWidgets.get(choiceNumber).setVisible(true);
			}
			// update the text of the answer
			answerWidgets.get(choiceNumber).setAnswer(answers[choiceNumber]);
		}
		// if there are more widgets than answers make them invisible
		for (int answerNumber = answers.length; answerNumber < answerWidgets.size();
				answerNumber++) {
			answerWidgets.get(answerNumber).setVisible(false);
		}
	}

// makes the panel occupy the right size for the number of answers
	@Override
	public Dimension getMaximumSize() {
		Dimension d = super.getMaximumSize();
		d.height = getPreferredSize().height;
		return d;
	}

	void clear() {
		for (Choice c : answerWidgets) {
			c.setVisible(false);
		}
	}

	void setFontSize(int size) {
		fontSize = size;
		for (Choice c : answerWidgets) {
			c.setFontSize(fontSize);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		fireStateChanged(e);
	}

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	public ChangeListener[] getChangeListeners() {
		return (listenerList.getListeners(ChangeListener.class));
	}

	protected void fireStateChanged(ChangeEvent changeEvent) {
		// Get the listener list
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first
		// List is in pairs, Class and instance
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
			}
		}
	}
}
