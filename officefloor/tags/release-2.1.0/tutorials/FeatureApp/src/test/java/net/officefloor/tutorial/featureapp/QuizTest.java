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

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import junit.framework.TestCase;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests the {@link Quiz}.
 * 
 * @author Daniel Sagenschneider
 */
public class QuizTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		WoofOfficeFloorSource.main();
	}

	@Override
	protected void tearDown() throws Exception {
		AutoWireManagement.closeAllOfficeFloors();
	}

	public void testCorrectlyAnswerQuiz() throws Exception {
		HttpClient client = new DefaultHttpClient();
		System.out.println(this.doRequest(client, "question"));
		String response = null;
		for (Question question : new Quiz().getQuestions()) {
			String uri = "question.links-answer.task?question="
					+ question.getQuestionIndex() + "&answer="
					+ question.getCorrectAnswer();
			response = this.doRequest(client, uri);
			System.out.println("URI " + uri);
			System.out.println(response);
		}
		assertTrue("Should have a correct answer", response.contains("Correct"));
		assertFalse("Should have no wrong answers",
				response.contains("Incorrect"));
	}

	private String doRequest(HttpClient client, String uri) throws Exception {
		HttpResponse response = client.execute(new HttpGet(
				"http://localhost:7878/" + uri));
		StringWriter buffer = new StringWriter();
		Reader reader = new InputStreamReader(response.getEntity().getContent());
		for (int character = reader.read(); character != -1; character = reader
				.read()) {
			buffer.write(character);
		}
		return buffer.toString();
	}

}