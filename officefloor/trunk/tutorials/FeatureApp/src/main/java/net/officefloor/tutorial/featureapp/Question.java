/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.tutorial.featureapp;

/**
 * Question for the quiz.
 * 
 * @author Daniel Sagenschneider
 */
public class Question {

	private final int questionIndex;

	private final String text;

	private final String explanation;

	private final int correctAnswer;

	private final Answer[] answers;

	public Question(int questionIndex, String text, String explanation,
			int correctAnswer, String... answers) {
		this.questionIndex = questionIndex;
		this.text = text;
		this.explanation = explanation;
		this.correctAnswer = correctAnswer;

		// 0 index is unanswered
		this.answers = new Answer[answers.length];
		for (int i = 0; i < answers.length; i++) {
			this.answers[i] = new Answer(i + 1, answers[i]);
		}
	}

	public int getQuestionIndex() {
		return this.questionIndex;
	}

	public String getText() {
		return this.text;
	}

	public Answer[] getAnswers() {
		return this.answers;
	}

	public int getCorrectAnswer() {
		return this.correctAnswer;
	}

	public String getExplanation() {
		return this.explanation;
	}

}