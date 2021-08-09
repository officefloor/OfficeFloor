/*-
 * #%L
 * Web on OfficeFloor Testing
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.woof;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Serializable;

import net.officefloor.frame.api.team.Team;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpObject;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.HttpSessionStateful;
import net.officefloor.web.ObjectResponse;

/**
 * Mock logic for the section.
 * 
 * @author Daniel Sagenschneider
 */
public class MockSection {

	@HttpSessionStateful
	public static class State implements Serializable {
		private static final long serialVersionUID = 1L;

		private String previous = "-1";
	}

	/**
	 * Services.
	 * 
	 * @param parameter  Parameter.
	 * @param state      {@link State}.
	 * @param connection {@link ServerHttpConnection}.
	 * @param object     {@link MockObject} injected from configuration.
	 */
	public void service(@HttpQueryParameter("param") String parameter, State state, ServerHttpConnection connection,
			MockObject object) throws IOException {
		connection.getResponse().getEntityWriter()
				.write("param=" + parameter + ", previous=" + state.previous + ", object=" + object.getMessage());
		state.previous = parameter;
	}

	/**
	 * Provides testing of objects.
	 * 
	 * @param object   {@link MockObject} injected from configuration.
	 * @param response Sends the {@link MockObject} as a response.
	 */
	public void objects(MockObject object, ObjectResponse<MockObject> response) {
		response.send(object);
	}

	/**
	 * Initial service method for testing {@link Team}
	 * 
	 * @param flows {@link TeamFlows}.
	 */
	@Next("teamsDifferent")
	public String teams() {
		return Thread.currentThread().getName();
	}

	/**
	 * Ensure different {@link Team}.
	 */
	public void teamsDifferent(@Parameter String threadName, MockObject object, ObjectResponse<String> response) {
		boolean isSameThread = Thread.currentThread().getName().equals(threadName);
		response.send(isSameThread ? "SAME THREAD" : "DIFFERENT THREAD");
	}

	@HttpObject
	public static class MockJsonObject {

		private String text = "TEST";

		public MockJsonObject() {
		}

		public MockJsonObject(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}

	public void json(ServerHttpConnection connection, MockJsonObject request, ObjectResponse<Object> response) {
		assertEquals("application/json", connection.getRequest().getHeaders().getHeader("content-type").getValue());
		response.send(request);
	}

	/**
	 * Failure to be thrown for testing.
	 */
	public static volatile Throwable failure = null;

	/**
	 * Services via throwing failure.
	 */
	public void failure() throws Throwable {
		throw failure;
	}

}
