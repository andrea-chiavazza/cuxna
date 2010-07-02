/**
 * A JPanel displaying all questions as small clickable buttons
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import cuxna.data.Question;
import cuxna.data.QuestionData;
import cuxna.data.Quiz;


public class QuizPanel extends JPanel implements ActionListener {
	private static final int MAX_COLUMNS = 3;
	private NavigableMap<Question, JButton> buttons = new TreeMap<Question, JButton>();
	private EventListenerList actionListeners = new EventListenerList();

	QuizPanel(Quiz[] quizes, Map<Question, QuestionData> questions) {
		super(new GridBagLayout());
		/// makes questions be focused in the correct order
		this.setFocusTraversalPolicy(new FocusTraversalPolicy() {
				@Override
				public Component getComponentBefore(Container aContainer, Component aComponent) {
					for (Question q : buttons.keySet()) {
						if (buttons.get(q).equals(aComponent)) {
							Question prev = buttons.lowerKey(q);
							if (prev != null) {
								return buttons.get(buttons.lowerKey(q));
							} else {
								return null;
							}
						}
					}
					return null;
				}

				@Override
				public Component getComponentAfter(Container aContainer, Component aComponent) {
					for (Question q : buttons.keySet()) {
						if (buttons.get(q).equals(aComponent)) {
							Question next = buttons.lowerKey(q);
							if (next != null) {
								return buttons.get(buttons.higherKey(q));
							} else {
								return null;
							}
						}
					}
					return null;
				}

				@Override
				public Component getFirstComponent(Container aContainer) {
					return buttons.get(buttons.firstKey());
				}

				@Override
				public Component getLastComponent(Container aContainer) {
					return buttons.get(buttons.lastKey());
				}

				@Override
				public Component getDefaultComponent(Container aContainer) {
					return getFirstComponent(aContainer);
				}
		}
		);
		this.setFocusTraversalPolicyProvider(true);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagConstraints c = new GridBagConstraints();
		// add horizontal separator
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(3, 3, 3, 3);
		add(new JSeparator(), c);
		c.insets = new Insets(0, 0, 0, 0);
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		for (int quizNo = 0; quizNo < quizes.length; quizNo++) {
			// set prodid
			c.gridx = quizNo * (MAX_COLUMNS + 1);
			c.gridy = 0;
			c.gridwidth = 3;
			add(new JLabel(quizes[quizNo].getProdId()), c);
			c.gridwidth = 1;
			JButton button;
			for (int questionNo = 0; questionNo < quizes[quizNo].getQuestionCount();
					questionNo++) {
				button = createQuestionButton(new Question(quizNo, questionNo));
				c.gridx = questionNo % MAX_COLUMNS + quizNo * (MAX_COLUMNS + 1);
				c.gridy = questionNo / MAX_COLUMNS + 2;
				buttons.put(new Question(quizNo, questionNo), button);
				add(button, c);
			}
			// the last column doesn't need a separator on the right
			if (quizNo != quizes.length - 1) {
				// add a separator
				c.gridx = (quizNo + 1) * (MAX_COLUMNS + 1) - 1;
				c.gridy = 0;
				// size of the separator
				c.gridheight = quizes[quizNo].getQuestionCount() / MAX_COLUMNS
						+ 2;
				c.fill = GridBagConstraints.BOTH;
				add(new JSeparator(SwingConstants.VERTICAL), c);
				c.fill = GridBagConstraints.NONE;
				c.gridheight = 1;
			}
		}
		// add a Filler so that the content is not in the middle but on the top
		c.gridy = GridBagConstraints.RELATIVE;
		c.weighty = 0.5;
		add(Box.createVerticalGlue(), c);
	}

	void showState(Question question, GUI.QuestionState state) {
		Color color;
		switch (state) {
		case CORRECT:
			color = Color.GREEN;
			break;
		case WRONG:
			color = Color.RED;
			break;
		case UNANSWERED:
			color = Color.LIGHT_GRAY;
			break;
		case TESTED:
			color = Color.LIGHT_GRAY;
			break;
		case UNTESTED:
			color = null;
			break;
		default:
			color = null;
		}
		buttons.get(question).setBackground(color);
	}

	void setMarkedState(Question question, boolean isMarked) {
		if (isMarked) {
			buttons.get(question).setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
		} else {
			buttons.get(question).setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		}
	}

	void setCurrentState(Question question, boolean state) {
		if (state) {
			// make button represent the currently displayed question
			buttons.get(question).setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
		} else {
			// unmark the marked question
			buttons.get(question).setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		}
	}
	void reset() {
		for (Question q : buttons.keySet()) {
			showState(q, GUI.QuestionState.UNTESTED);
			setMarkedState(q, false);
			setCurrentState(q, false);
		}
	}
	private JButton createQuestionButton(Question question) {
		JButton button = new JButton(String.format("%02d", question
				.getQuestionNo() + 1));
		button.setActionCommand(Integer.toString(question.getQuizNo()) + " "
				+ Integer.toString(question.getQuestionNo()));
		button.addActionListener(this);
		button.setContentAreaFilled(false);
		button.setOpaque(true);
		button.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		button.setMaximumSize(button.getPreferredSize());
		return button;
	}

	// Listener notification support
	void addActionListener(ActionListener x) {
		actionListeners.add(ActionListener.class, x);
	}

	void removeActionListener(ActionListener x) {
		actionListeners.remove(ActionListener.class, x);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Get the listener list
		Object[] listeners = actionListeners.getListenerList();
		// Process the listeners last to first
		// List is in pairs, Class and instance
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ActionListener.class) {
				ActionListener cl = (ActionListener) listeners[i + 1];
				cl.actionPerformed(e);
			}
		}
	}
}