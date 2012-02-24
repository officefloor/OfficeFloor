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

import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Logic for <code>question.html</code>.
 * 
 * @author Daniel Sagenschneider
 */
public class QuestionLogic {

	@FlowInterface
	public static interface QuizFlows {
		void complete();
	}

	private Question question;

	public Question getQuestion(UserAnswers answers, Quiz quiz) {

		// Ensure quiz started
		if (answers.getAnswers() == null) {
			answers.startQuiz(quiz.getQuestions().length);
		}

		// Obtain the next unanswered question
		int[] values = answers.getAnswers();
		for (int i = 0; i < values.length; i++) {
			if (values[i] == 0) {
				this.question = quiz.getQuestions()[i];
				return this.question;
			}
		}

		// No further questions
		return null;
	}

	public Answer[] getAnswers() {
		return (this.question == null ? null : this.question.getAnswers());
	}

	public void answer(UserAnswer answer, UserAnswers answers, Quiz quiz,
			QuizFlows flows) {

		// Specify the answer
		answers.getAnswers()[answer.getQuestion()] = answer.getAnswer();

		// Determine if quiz complete
		Question nextQuestion = this.getQuestion(answers, quiz);
		if (nextQuestion == null) {
			flows.complete();
		}
	}

}