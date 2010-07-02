/**
 * Class containing the data structure of a question.
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.data;

public class QuestionData {
	private String text, hint, solution;
	private boolean[] correctAnswer;
	private String[] choices;

	public QuestionData(String newText, boolean[] newCorrectAnswer, String[] answers,
			String newHint, String newSolution) {
		text = newText;
		hint = newHint;
		solution = newSolution;
		correctAnswer = newCorrectAnswer;
		choices = answers;
	}

	public String getText() {
		return text;
	}

	public String getHint() {
		return hint;
	}

	public String getSolution() {
		return solution;
	}

	public String[] getAnswers() {
		return choices;
	}

	public boolean[] getCorrectAnswer() {
		return correctAnswer;
	}
}