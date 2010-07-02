/**
 * The class that contains the main method and sets up the GUI.
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cuxna.data.Data;
import cuxna.data.Question;
import cuxna.data.QuestionData;
import cuxna.data.Quiz;
import cuxna.data.UserData;


public class GUI extends JFrame {
	enum QuestionState {
		TESTED, UNTESTED, CORRECT, WRONG, UNANSWERED
	}

	class ChangeFontSizeAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			int size = fontSpinnerModel.getNumber().intValue();
			questionPanel.setFontSize(size);
			answerPanel.setFontSize(size);
			Preferences.userNodeForPackage(GUI.class).putInt(PREFERENCE, size);
		}
	}
	private class UserCancelException extends Exception { }
	private Quiz[] quizes;
	private NavigableMap<Question, QuestionData> questions;
	private NavigableMap<Question, UserData> userData = new TreeMap<Question, UserData>();
	private CurrentProperty<Question> currentQuestion = new CurrentProperty<Question>();

	private TimeInfoPanel timeInfoPanel = new TimeInfoPanel();
	private QuestionInfoPanel questionInfoPanel;
	private QuizPanel quizPanel;
	private QuestionPanel questionPanel;
	private AnswerPanel answerPanel;

	private JButton prevButton = new JButton("Previous");
	private JButton nextButton = new JButton("Next");
	private JButton resetButton = new JButton("Reset");
	private JCheckBox markCheckBox = new JCheckBox("Mark Question");
	private JToggleButton showResultButton = new JToggleButton("Show Results");
	private JButton showHintButton = new JButton("Hint");
	private JButton showSolutionButton = new JButton("Solution");
	private JButton promptPathButton = new JButton("Load");
	private SpinnerNumberModel fontSpinnerModel = new SpinnerNumberModel(14, 10, 30, 1);

	private static String PREFERENCE = "preferredFontSize";

	/**
	 * Prompts the user for a directory path.
	 * 
	 * @return the path selected by the user or null if cancel button is clicked
	 *         or dialog is closed
	 * @throws UserCancelException 
	 */
	private File chooseDir() throws UserCancelException {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		switch (fileChooser.showOpenDialog(null)) {
		case JFileChooser.APPROVE_OPTION:
			return fileChooser.getSelectedFile();
		case JFileChooser.CANCEL_OPTION:
		case JFileChooser.ERROR_OPTION:
		default:
			throw new UserCancelException();
		}
	}

	private void loadDataPrompt() throws UserCancelException, IOException {
		JOptionPane.showMessageDialog(this,
				"Please select the folder containing the data for the questions.\n" +
				"It can be found:\n" +
				"- in the directory created by the Windows installer at: LearnKey\\MasterExam\\database\n" +
				"- in the CD included with the book at: /Programs/MasterExam/robo/database");
		Data data = new Data(chooseDir());
		quizes = data.getQuizes();
		questions = data.getQuestions();
	}

	private void loadData() {
		try {
			Data data = new Data();
			quizes = data.getQuizes();
			questions = data.getQuestions();
		} catch (IOException e) {
			for (;;) {
				try {
					loadDataPrompt();
					break;
				} catch (UserCancelException e2) {
					JOptionPane.showMessageDialog(this, "Exiting: this program can't work without a suitable database");
					System.exit(1);
				} catch (IOException e2) {
					// inform user database coudn't be found
					JOptionPane.showMessageDialog(this, "Database not found, plaese try again");
				}
			}
		}
	}
	
	// the constructor is used to initialise instance variables and create their
	// event handlers
	private GUI() {
		loadData();
		int fontSize = Preferences.userNodeForPackage(GUI.class).getInt(PREFERENCE, 0);
		if (fontSize == 0) {
			fontSize = 14;
		}
		fontSpinnerModel.setValue(fontSize);
		questionPanel = new QuestionPanel(fontSize);
		answerPanel = new AnswerPanel(fontSize);

		questionInfoPanel = new QuestionInfoPanel(questions.size());
		quizPanel = new QuizPanel(quizes, questions);
		// sets key short-cuts
		prevButton.setMnemonic(KeyEvent.VK_P);
		nextButton.setMnemonic(KeyEvent.VK_N);
		markCheckBox.setMnemonic(KeyEvent.VK_M);
		markCheckBox.setToolTipText("Sets a mark on the question as a convenience for the user");
		showHintButton.setMnemonic(KeyEvent.VK_H);
		showSolutionButton.setMnemonic(KeyEvent.VK_S);
		showResultButton.setMnemonic(KeyEvent.VK_R);
//		resetButton.setMnemonic(KeyEvent.VK_E);
		resetButton.setToolTipText("Clears all answers to restart the exam");
		promptPathButton.setToolTipText("Loads a new database");
		
		// adds all the listeners
		currentQuestion.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				Question oldValue = (Question) e.getOldValue();
				Question newValue = (Question) e.getNewValue();
				// there is not a current question
				if (newValue == null) {
					prevButton.setEnabled(false);
					nextButton.setEnabled(false);
					markCheckBox.setEnabled(false);
					showHintButton.setEnabled(false);
					showSolutionButton.setEnabled(false);
					showResultButton.setEnabled(false);
					resetButton.setEnabled(false);
					promptPathButton.setEnabled(true);
					answerPanel.clear();
					questionPanel.reset();
					return;
				}
				// no question was previously selected - starts the exam
				if (oldValue == null) {
					markCheckBox.setEnabled(true);
					showHintButton.setEnabled(true);
					showSolutionButton.setEnabled(true);
					resetButton.setEnabled(true);
					promptPathButton.setEnabled(false);
					showResultButton.setEnabled(true);
					timeInfoPanel.start();
				// a question was previously selected
				} else {
					// unmark the previously selected question in quizPanel
					showState(oldValue);
					quizPanel.setCurrentState(oldValue, false);
					quizPanel.setMarkedState(oldValue, userData.get(oldValue).isMarked());
					// updates the correct counter
					if (showResultButton.isSelected()) {
						questionInfoPanel.setCorrectCount(getCorrectCount());
					}
				}
				// the question selected is new
				if (!userData.containsKey(newValue)) {
					userData.put(newValue, new UserData(questions.get(newValue).getAnswers().length)); // questions.get(newValue).getCorrectAnswer()
					// update the tested question count
					questionInfoPanel.setTestedCount(userData.size());
					quizPanel.showState(newValue, QuestionState.TESTED);
				}
				// marks the currently selected question in quizPanel
				quizPanel.setCurrentState(newValue, true);
				// sets markCheckBox according to the question state
				markCheckBox.setSelected(userData.get(newValue).isMarked());
				// sets the question text
				questionPanel.setText(questions.get(newValue).getText());
				// sets the answers
				answerPanel.showAnswers(questions.get(newValue));
				// updates the check-boxes with the answer previously given
				answerPanel.setAnswer(userData.get(newValue).getAnswer());
				// enable/disable previous/next buttons
				checkPrevNextButton();
			}
		});
		quizPanel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String[] s = e.getActionCommand().split(" ");
				int quizNo = Integer.parseInt(s[0]);
				int questionNo = Integer.parseInt(s[1]);
				Question question = new Question(quizNo, questionNo);
				if (userData.containsKey(question) ||
						!questionInfoPanel.isAllTested()) {
					currentQuestion.setValue(question);
				}
			}
		});
		// Action classes should be used
		prevButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentQuestion.setValue(getPrevQuestion(currentQuestion.getValue()));
			}
		});
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentQuestion.setValue(getNextQuestion(currentQuestion.getValue()));
			}
		});
		markCheckBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (currentQuestion.getValue() != null &&
						userData.containsKey(currentQuestion.getValue())) {
					userData.get(currentQuestion.getValue()).setMarked(markCheckBox.isSelected());
				}
			}
		});
		// a change occurs when the user ticks an answer
		answerPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Choice choice = (Choice) e.getSource();
				userData.get(currentQuestion.getValue()).getAnswer()[choice.getChoiceNumber()] = choice.isSelected();
			}
		});
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(GUI.this, "Reset all data ?",
						"Warning", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					return;
				}
				// clear the user data
				userData.clear();
				resetButton.setEnabled(false);
				timeInfoPanel.reset();
				quizPanel.reset();
				questionInfoPanel.reset();
				currentQuestion.setValue(null);
				showResultButton.setSelected(false);
			}
		});
		showResultButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// updates the correct count
				if (showResultButton.isSelected()) {
					questionInfoPanel.setCorrectCount(getCorrectCount());
				}
				// updates the visibility of the correct count
				questionInfoPanel.showResult(showResultButton.isSelected());
				// updates the visibility of the result for all questions
				for (Question question : userData.keySet()) {
					if (userData.containsKey(question)) {
						showState(question);
					}
				}
			}
		});
		showHintButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(GUI.this,
						questions.get(currentQuestion.getValue()).getHint(),
						"Hint", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		showSolutionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JTextArea ta = new JTextArea(questions.get(currentQuestion.getValue()).getSolution());
				ta.setFont(ta.getFont().deriveFont(fontSpinnerModel.getNumber().floatValue()));
				ta.setEditable(false);
				ta.setLineWrap(true);
				ta.setWrapStyleWord(true);
				ta.setColumns(40);
				ta.setRows(20);
				JOptionPane.showMessageDialog(GUI.this, new JScrollPane(ta), "Solution",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		// a change occurs when the user changes the target count
		questionInfoPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (currentQuestion.getValue() != null) {
					checkPrevNextButton();
				}
			}
		});
		promptPathButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					loadDataPrompt();
					questionInfoPanel.setQuestionCount(questions.size());
					quizPanel = new QuizPanel(quizes, questions);
				} catch (UserCancelException e2) {					
				} catch (IOException e2) {
					// inform user database coudn't be read
					JOptionPane.showMessageDialog(GUI.this, "Database couldn't be read");
				}
			}			
		});
		fontSpinnerModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int size = fontSpinnerModel.getNumber().intValue();
				questionPanel.setFontSize(size);
				answerPanel.setFontSize(size);
				Preferences.userNodeForPackage(GUI.class).putInt(PREFERENCE, size);
			}
		});
		currentQuestion.setValue(null);
		questionInfoPanel.showResult(false);
	}

	/**
	 * if the button to show result is set, shows in the panel whether the
	 * answer is correct otherwise just shows if the question is being tested or
	 * not
	 */
	private void showState(Question question) {
		if (showResultButton.isSelected()) {
			if (Arrays.equals(userData.get(question).getAnswer(),
					new boolean[userData.get(question).getAnswer().length])) {
				quizPanel.showState(question, QuestionState.UNANSWERED);
			} else if (isCorrect(question)) {
				quizPanel.showState(question, QuestionState.CORRECT);
			} else {
				quizPanel.showState(question, QuestionState.WRONG);
			}
		} else {
			if (userData.containsKey(question)) {
				quizPanel.showState(question, QuestionState.TESTED);
			} else {
				quizPanel.showState(question, QuestionState.UNTESTED);
			}
		}
	}

	private boolean isCorrect(Question question) {
		return Arrays.equals(userData.get(question).getAnswer(),
				questions.get(question).getCorrectAnswer());
	}

	private int getCorrectCount() {
		int count = 0;
		for (Question q : userData.keySet()) {
			if (isCorrect(q)) {
				count++;
			}
		}
		return count;
	}

	private void checkPrevNextButton() {
		prevButton.setEnabled(getPrevQuestion(currentQuestion.getValue()) != null);
		nextButton.setEnabled(getNextQuestion(currentQuestion.getValue()) != null);
	}

	private Question getPrevQuestion(Question question) {
		// if the target number of question hasn't been reached simply returns
		// the previous question
		if (!questionInfoPanel.isAllTested()) {
			return questions.lowerKey(question);
		} else {
			// otherwise returns the previous already chosen question
			return userData.lowerKey(question);
		}
	}

	private Question getNextQuestion(Question question) {
		// if the target number of question hasn't been reached simply returns
		// the following question
		if (!questionInfoPanel.isAllTested()) {
			return questions.higherKey(question);
		} else {
			// otherwise returns the next already chosen question
			return userData.higherKey(question);
		}
	}

	/**
	 * Creates all the components for the GUI and shows it. Widgets created in
	 * this method don't need a reference of them to be kept
	 */
	private void createAndShowGUI() {
		add(quizPanel, BorderLayout.WEST);
		// center panel
		Box centerPanel = new Box(BoxLayout.Y_AXIS) {
			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = getMinimumSize().height;
				return d;
			}
		};

		centerPanel.add(questionPanel);
		centerPanel.add(answerPanel);
		add(centerPanel, BorderLayout.CENTER);
	// east panel
		JPanel eastPanel = new JPanel();
		GroupLayout layout = new GroupLayout(eastPanel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		eastPanel.setLayout(layout);
		JLabel fontSpinnerLabel = new JLabel("Font size");
		JSpinner fontSpinner = new JSpinner(fontSpinnerModel);
		fontSpinner.setMaximumSize(fontSpinner.getPreferredSize());
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
				.addComponent(timeInfoPanel,
						GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(questionInfoPanel,
						GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(fontSpinnerLabel)
						.addComponent(fontSpinner)
				)
				.addGroup(layout.createSequentialGroup()
						.addComponent(resetButton)
						.addComponent(promptPathButton)
				)
				.addComponent(showResultButton)
				.addComponent(showHintButton)
				.addComponent(showSolutionButton)
				.addComponent(markCheckBox)
				.addGroup(layout.createSequentialGroup()
						.addComponent(prevButton)
						.addComponent(nextButton)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(timeInfoPanel,
						GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(questionInfoPanel,
						GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
						.addComponent(fontSpinnerLabel)
						.addComponent(fontSpinner)
				)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
						.addComponent(resetButton)
						.addComponent(promptPathButton)
				)
				.addComponent(showResultButton)
				.addComponent(showHintButton)
				.addComponent(showSolutionButton)
				.addComponent(markCheckBox)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
								.addComponent(prevButton)
								.addComponent(nextButton)
				)
		);
		add(eastPanel, BorderLayout.EAST);
	// Display the window.
		setTitle("Cuxna");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (JOptionPane.showConfirmDialog(null, "Terminate session ?",
						"Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});

		getRootPane().registerKeyboardAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fontSpinnerModel.setValue(fontSpinnerModel.getNextValue());
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK),
		JComponent.WHEN_IN_FOCUSED_WINDOW);

		getRootPane().registerKeyboardAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fontSpinnerModel.setValue(fontSpinnerModel.getPreviousValue());
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK),
		JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
		setVisible(true);
	}

	/**
	 * The main method of the application.
	 * 
	 * @param args
	 *            currently unused
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new GUI().createAndShowGUI();
			}
		});
	}
}