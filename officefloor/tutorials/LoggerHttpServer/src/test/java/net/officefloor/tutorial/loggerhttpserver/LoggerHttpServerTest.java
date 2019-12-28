/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.tutorial.loggerhttpserver;

import java.util.logging.Level;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.frame.test.LoggerRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Logger HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class LoggerHttpServerTest {

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public LoggerRule log = new LoggerRule();

	@Test
	public void ensureLogging() {

		// Send request to be logged
		MockHttpResponse response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, new LoggedRequest("TEST")));
		response.assertResponse(204, "");

		// Ensure log input message (from procedure)
		this.log.assertLog("log.procedure", Level.INFO, "PROCEDURE: TEST");

		// Ensure log from object
		this.log.assertLog("OFFICE.net_officefloor_tutorial_loggerhttpserver_LogObject", Level.INFO, "OBJECT: TEST");
	}
}
