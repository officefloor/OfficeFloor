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

import net.officefloor.plugin.web.http.application.HttpApplicationStateful;

/**
 * Quiz.
 * 
 * @author Daniel Sagenschneider
 */
@HttpApplicationStateful
public class Quiz {

	private final Question[] questions;

	public Quiz() {
		this.questions = new Question[] {
				new Question(0, "Inversion of Control is:",
						"Inversion of Control is all of the features", 5,
						"Context", "Dependency Injection", "Thread Injection",
						"Function Orchestration", "All of the above"),
				new Question(
						1,
						"OfficeFloor integrates with:",
						"OfficeFloor uses non-obtrusive web page instrumentation and has plug-ins for Maven and Eclipse",
						4, "WYSIWYG web page designers", "Maven", "Eclipse",
						"All of the above"),
				new Question(
						2,
						"OfficeFloor web applications can run:",
						"OfficeFloor through its inversion of control can run anywhere",
						4, "Stand alone", "Within a JEE Servlet Container",
						"By Cloud Computing Providers", "All of the above") };
	}

	public Question[] getQuestions() {
		return this.questions;
	}
}