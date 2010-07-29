/**
 * This class loads an array of Quiz objects and a Map of Question objects
 * given the path of a file in DBF format.
 * The file GTEST.DBF is looked for in the path.
 * 
 * @author Andrea Chiavazza
 * Copyright 2010 licensed under GPL version 3 obtainable from http://www.gnu.org/licenses/gpl.html
 */
package cuxna.data;


import java.io.File;
import java.io.IOException;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import cuxna.dbf.DBF;
import cuxna.gui.GUI;


public class Data {
	final String PATH = "path";
	// The name of file to be looked for in the given path.
	private static final String indexName = "GTEST";

	private Quiz[] quizes;
	private NavigableMap<Question, QuestionData> questions = new TreeMap<Question, QuestionData>();

	public Quiz[] getQuizes() {
		return quizes;
	}

	public NavigableMap<Question, QuestionData> getQuestions() {
		return questions;
	}

	/**
	 * Reads a DBF file and stores the data in an array of Quiz objects and a Map of Question objects
	 * 
	 * @param path
	 *            the path where to look for the database file
	 */
	private void load(String path) throws IOException {
		String dbPath = path + File.separatorChar;
		DBF indexDBF = new DBF(dbPath + indexName);
		// load Quiz structures
		quizes = new Quiz[indexDBF.getNumRecords()];
		for (int i = 0; i < quizes.length; i++) {
			String fileBase = dbPath
			+ indexDBF.readString(i, "PATH").replace('\\',
					File.separatorChar)
					+ indexDBF.readString(i, "TABLENAME");
			int numQuestions = readQuestions(fileBase, i);
			quizes[i] = new Quiz(indexDBF.readString(i, "PRODID"), indexDBF.readString(i, "TITLE"),
					numQuestions);
			// applies erratas
			if (quizes[i].getProdId().equals("075708f")) {
				boolean[] a = questions.get(new Question(i, 72)).getCorrectAnswer();
				System.arraycopy(
						new boolean[] {false, false, true, true, false, true},
						0, a, 0, a.length);
			}
			if (quizes[i].getProdId().equals("075708b")) {
				String[] answers = questions.get(new Question(i, 67)).getAnswers();
				answers[6] = answers[6].replaceAll("cp\\.jar", "mp.jar");
				answers[7] = answers[7].replaceAll("cp\\.jar", "mp.jar");
			}
		}
	}

	/**
	 * Converts an answer stored as a bitmap int to an array of boolean
	 * 
	 * @param bitmap
	 * @param length
	 * @return
	 */
	private static boolean[] bitmapToAnswer(int bitmap, int length) {
		boolean[] result = new boolean[length];
		for (int i = 0; i < length; i++) {
			switch (bitmap & 1) {
			case 0:
				result[i] = false;
				break;
			case 1:
				result[i] = true;
			}
			bitmap >>= 1;
		}
		return result;
	}

	/**
	 * Creates an array of boolean of size length with element in position i - 1 set to true
	 * and all the others set to false
	 * 
	 * @param i
	 * @param length
	 * @return the newly created array
	 */
	private static boolean[] iToAnswer(int i, int length) {
		boolean[] result = new boolean[length];
		result[i - 1] = true;
		return result;
	}

	/**
	 * Reads questions from the given DBF file and its associated answers from a
	 * DBF file with the same name with an 'X' appended to it.
	 * 
	 * @param fileBase
	 *            name of file without extension
	 * @param quizNo
	 * @return an array of Question objects
	 */
	private int readQuestions(String fileBase, int quizNo) {
		int numQuestions = 0;
		try {
			DBF questionsDBF = new DBF(fileBase);
			DBF answersDBF = new DBF(fileBase + "X");
			numQuestions = questionsDBF.getNumRecords();
			int i = 0;
			for (int q = 0; q < numQuestions; q++) {
				// read answers. - relies on NUMANSWER field being correct
				String[] answers = new String[questionsDBF.readInt(q, "NUMANSWER")];
				for (int l = 0; l < answers.length; l++) {
					// Workaround: fix the format of the answer text
					// 2 or more spaces are converted to a newline
					answers[l] = answersDBF.readString(i, "ANSWER").replaceAll(" {2,}", "\n");
					i++;
				}
				boolean[] correctAnswer;
				int answerValue = Integer.parseInt(questionsDBF.readString(q, "ANSWER").trim());
				/* The user is not hinted as to whether the question can have more than one correct
				 * answer by using radio or check-box buttons.
				 * This makes the test a bit tougher but the book mentions it as a good idea
				 * at the top of page xxx (30 in roman's)
				 * */
				switch(questionsDBF.readInt(q, "TYPE")) {
				// only one answer is correct
				case 1:
					correctAnswer = iToAnswer(answerValue, answers.length);
					break;
				// more than one answer can be correct
				case 5:
				default:
					correctAnswer = bitmapToAnswer(answerValue, answers.length);
				}
				questions.put(new Question(quizNo, q),
						new QuestionData(
								questionsDBF.readString(q, "QUESTION"),
								correctAnswer,
								answers,
								questionsDBF.readString(q, "HINT"),
								questionsDBF.readString(q, "REF")
						)
				);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return numQuestions;
	}

	/**
	 * Constructor that gets the path from the Preferences
	 * 
	 * @throws IOException
	 */
	public Data() throws IOException {
		// get path from preferences
		Preferences prefs = Preferences.userNodeForPackage(GUI.class);
		String path = prefs.get(PATH, null);

		if (path == null) {
			// the key is not set in the preferences
			throw new IOException();
		}
		// try to load the data
		try {
			load(path);
		} catch (IOException e) {
			// the path is wrong: remove it from the preferences
			prefs.remove(PATH);
			throw new IOException();
		}
	}

	/**
	 * Constructor that gets the path as a parameter, and saves it to the
	 * Preferences if the loading was successful
	 * 
	 * @param filePath
	 * 
	 * @throws IOException
	 */
	public Data(File filePath) throws IOException {
		// try to load the data
		load(filePath.getPath());
		// save path to preferences
		Preferences.userNodeForPackage(GUI.class).put(PATH, filePath.getPath());
	}	
}