/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.woof.mock;

import java.io.IOException;
import java.io.Serializable;

import net.officefloor.frame.api.team.Team;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.ServerHttpConnection;
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
	@NextFunction("teamsDifferent")
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

}