/**
 * A panel keeping count of questions tested / tried / correct
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.RoundingMode;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class QuestionInfoPanel extends JPanel {
	private int testedCount;
	private int correctCount;
	private JLabel testedCountWidget = new JLabel();
	private JLabel correctCountWidget = new JLabel();
	private JLabel targetTestedPercentWidget = new JLabel();
	private JLabel testedCorrectPercentWidget = new JLabel();
	private JLabel targetCorrectPercentWidget = new JLabel();
	private JLabel correctCountLabel = new JLabel("Correct: ");
	private SpinnerNumberModel targetCountSpinnerModel = new SpinnerNumberModel(60, 1, 60, 1);
	private NumberFormat percentFormat = NumberFormat.getPercentInstance();
	private ChangeEvent changeEvent;
	QuestionInfoPanel(final int questionCount) {
		super(new GridBagLayout());
		setQuestionCount(questionCount);
		// the percentage of correct answers should be rounded down
		percentFormat.setRoundingMode(RoundingMode.FLOOR);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.NONE;
/// 1st row
		c.gridy = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		this.add(new JLabel("Target: "), c);

		// fillers to keep the column size from changing
		c.gridx = 1;
		c.anchor = GridBagConstraints.EAST;
		Dimension dim;
		JLabel filler = new JLabel("000");
		dim = filler.getPreferredSize();
		this.add(new Box.Filler(dim, dim, dim), c);

		c.gridx = 3;
		filler.setText("100%");
		dim = filler.getPreferredSize();
		this.add(new Box.Filler(dim, dim, dim), c);

		c.gridx = 5;
		this.add(new JSpinner(targetCountSpinnerModel), c);
/// 2nd row
		c.gridy = 1;
		c.gridx = 4;
		c.gridheight = 4;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.CENTER;
		this.add(new JSeparator(SwingConstants.VERTICAL), c);
		c.gridheight = 1;

		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(new JSeparator(SwingConstants.HORIZONTAL), c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
/// 3rd row
		c.gridy = 2;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		this.add(new JLabel("Tried: "), c);
		c.gridx = 3;
		c.anchor = GridBagConstraints.EAST;
		this.add(testedCountWidget, c);
		c.gridx = 5;
		this.add(targetTestedPercentWidget, c);
/// 4th row
		c.gridy = 3;
		c.gridx = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.VERTICAL;
		c.gridheight = 2;
		this.add(new JSeparator(SwingConstants.VERTICAL), c);		

		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 4;
		this.add(new JSeparator(SwingConstants.HORIZONTAL), c);

		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
/// 5th row
		c.gridy = 4;
		c.gridx = 0;
		this.add(correctCountLabel, c);

		c.gridx = 1;
		this.add(correctCountWidget, c);

		c.gridx = 3;
		this.add(testedCorrectPercentWidget, c);

		c.gridx = 5;
		this.add(targetCorrectPercentWidget, c);

		TitledBorder questionBorder = BorderFactory.createTitledBorder("Questions");
		this.setBorder(questionBorder);

		targetCountSpinnerModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				QuestionInfoPanel.this.fireStateChanged();
				targetTestedPercentWidget.setText(makePercent(getTargetCount(), testedCount));
				targetCorrectPercentWidget.setText(makePercent(getTargetCount(), correctCount));
			}
		});
		reset();
	}

	void setQuestionCount(int count) {
		targetCountSpinnerModel.setMaximum(count);		
	}

	int getTargetCount() {
		return targetCountSpinnerModel.getNumber().intValue();
	}

	boolean isAllTested() {
		return testedCount == getTargetCount();
	}

	void setTestedCount(int value) {
		testedCount = value;
		targetCountSpinnerModel.setMinimum(testedCount);
		testedCountWidget.setText(Integer.toString(testedCount));
		targetTestedPercentWidget.setText(makePercent(getTargetCount(), testedCount));
		testedCorrectPercentWidget.setText(makePercent(testedCount, correctCount));
	}

	void setCorrectCount(int value) {
		correctCount = value;
		correctCountWidget.setText(Integer.toString(correctCount));
		testedCorrectPercentWidget.setText(makePercent(testedCount, correctCount));
		targetCorrectPercentWidget.setText(makePercent(getTargetCount(), correctCount));
	}

	void reset() {
		setCorrectCount(0);
		setTestedCount(0);
	}

	void showResult(boolean state) {
		correctCountWidget.setVisible(state);
		targetCorrectPercentWidget.setVisible(state);
		testedCorrectPercentWidget.setVisible(state);
	}

	private String makePercent(int total, int value) {
		double ratio = 0.0;
		if (total != 0) {
			ratio = (double) value / total;
		}
		return percentFormat.format(ratio);
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

	public void fireStateChanged() {
		// Get the listener list
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first
		// List is in pairs, Class and instance
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				if (changeEvent == null) {
					changeEvent = new ChangeEvent(this);
				}
				((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
			}
		}
	}
}
