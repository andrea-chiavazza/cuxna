/**
 * Class used to identify a question object
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.data;

public class Question implements Comparable<Question> {
	private int quizNo;
	private int questionNo;

	public Question(int newQuizNo, int newQuestionNo) {
		set(newQuizNo, newQuestionNo);
	}

	public void set(int newQuizNo, int newQuestionNo) {
		this.quizNo = newQuizNo;
		this.questionNo = newQuestionNo;
	}

	public int getQuizNo() {
		return quizNo;
	}

	public int getQuestionNo() {
		return questionNo;
	}

	@Override
	public int hashCode() {
		return quizNo << 16 + (questionNo | 0x0000ffff);
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Question &&
				((Question) o).quizNo == this.quizNo &&
				((Question) o).questionNo == this.questionNo);
	}

	@Override
	public int compareTo(Question o) {
		if (o.quizNo == quizNo) {
			return questionNo - o.questionNo;
		} else {
			return quizNo - o.quizNo;
		}
	}
}
