/**
 * Class containing a collection of data
 * needed as a result of the user interaction with the quiz.
 * For example the answer given by the user, and whether a question is marked.
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.data;

public class UserData {
	private boolean[] userAnswer;
	private boolean isMarked = false;

	public UserData(int answersCount) {
		userAnswer = new boolean[answersCount];
	}

	public void setMarked(boolean state) {
		isMarked = state;
	}

	public boolean isMarked() {
		return isMarked;
	}

	public boolean[] getAnswer() {
		return userAnswer;
	}
}