/**
 * A panel that keeps track of time elapsed/remaining
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TimeInfoPanel extends JPanel {
	private Calendar remaining = Calendar.getInstance();
	private Calendar elapsed = Calendar.getInstance();
	private Timer timer;
	private JLabel remainingLabel = new JLabel();
	private JLabel elapsedLabel = new JLabel();
	private JButton pauseButton = new JButton();
	private String dateFormatString = "HH:mm:ss";
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatString);
	private SpinnerDateModel totalModel;

	TimeInfoPanel() {
		super(new GridBagLayout());
		totalModel = new SpinnerDateModel(
				makeDate(3, 0, 0), makeDate(0, 0, 1), makeDate(23, 59, 59), Calendar.SECOND);
		timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				elapsed.add(Calendar.SECOND, 1);
				showElapsedTime();
				updateRemaining(true);
			}
		});
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (timer.isRunning()) {
					timer.stop();
					pauseButton.setText("Resume");
					pauseButton.setBackground(Color.RED);
				} else {
					timer.start();
					pauseButton.setText("Pause");
					pauseButton.setBackground(null);
				}
			}
		});
		totalModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateRemaining(false);
			}
		});
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		this.add(new JLabel("Total:"), c);
		c.gridx = 1;
		JSpinner totalSpinner = new JSpinner(totalModel);
		totalSpinner.setEditor(new JSpinner.DateEditor(totalSpinner, dateFormatString));
		this.add(totalSpinner, c);
		c.gridx = 0;
		c.gridy = 1;
		this.add(new JLabel("Elapsed:"), c);
		c.gridx = 1;
		this.add(elapsedLabel, c);
		c.gridx = 0;
		c.gridy = 2;
		this.add(new JLabel("Remaining:"), c);
		c.gridx = 1;
		this.add(remainingLabel, c);
		c.gridy = 3;
		c.gridwidth = 2;
		c.gridx = 0;
		this.add(pauseButton, c);

		TitledBorder timeBorder = BorderFactory.createTitledBorder("Time");
		this.setBorder(timeBorder);
		reset();
	}

	/**
	 * Hour 0 is -3600000 millisec and hour 1 is 0 millisec
	 * so the difference of 2 dates has to be fixed before
	 * being displayed
	 * @param d
	 * @return
	 */
	private Date dateAsDifference(Date d) {
		return new Date(d.getTime() - 3600000);
	}
	
	private void updateRemaining(boolean checkEnd) {
		String label = "";
		long diff = totalModel.getDate().getTime() - elapsed.getTimeInMillis();
		if (diff >= 0) {
			remaining.setTimeInMillis(diff);
			if (checkEnd && diff == 0) {
				JOptionPane.showMessageDialog(this, "Time is over");
			}
		} else {
			label = "-";
			// if time limit has passed show difference
			remaining.setTimeInMillis(-diff);
		}
		label += simpleDateFormat.format(dateAsDifference(remaining.getTime()));
		remainingLabel.setText(label);
	}
	
	private void showElapsedTime() {
		elapsedLabel.setText(simpleDateFormat.format(elapsed.getTime()));
	}

	private void stop() {
		pauseButton.setText("Pause");
		pauseButton.setEnabled(false);
		timer.stop();		
	}

	void reset() {
		stop();
		elapsed.setTime(makeDate(0, 0, 0));
		showElapsedTime();
		updateRemaining(false);
	}

	void start() {
		timer.start();
		pauseButton.setEnabled(true);
	}

	private static Date makeDate(int hour, int minute, int second) {
		Calendar c = Calendar.getInstance();
//		c.clear();
		c.setTimeInMillis(0);
		c.set(Calendar.SECOND, second);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.HOUR_OF_DAY, hour);
		return c.getTime();
	}
}

