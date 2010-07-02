/**
 * Class containing the data structure of a quiz.
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.data;

public class Quiz {
	private String prodId;
	private String title;
	private int questionCount;

	public Quiz(String newProdId, String newTitle, int newQuestionCount) {
		prodId = newProdId;
		title = newTitle;
		questionCount = newQuestionCount;
	}

	public String getTitle() {
		return title;
	}

	public String getProdId() {
		return prodId;
	}

	public int getQuestionCount() {
		return questionCount;
	}
}