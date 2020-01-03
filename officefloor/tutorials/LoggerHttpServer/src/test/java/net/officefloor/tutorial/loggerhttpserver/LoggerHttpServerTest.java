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

	// START SNIPPET: tutorial
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
	// END SNIPPET: tutorial

}
