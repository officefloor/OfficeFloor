package net.officefloor.tutorial.loggerhttpserver;

import java.util.logging.Level;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.test.logger.LoggerRule;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Logger HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class LoggerHttpServerJUnit4Test {

	// START SNIPPET: tutorial
	@Rule
	public final MockWoofServerRule server = new MockWoofServerRule(this);

	@Rule
	public final LoggerRule log = new LoggerRule();

	@Test
	public void ensureLogging() {

		// Send request to be logged
		MockHttpResponse response = this.server.send(MockWoofServer.mockJsonRequest(new LoggedRequest("TEST")));
		response.assertResponse(204, "");

		// Ensure log input message (from procedure)
		this.log.assertLog("log.procedure", Level.INFO, "PROCEDURE: TEST");

		// Ensure log from object
		this.log.assertLog("OFFICE.net_officefloor_tutorial_loggerhttpserver_LogObject", Level.INFO, "OBJECT: TEST");
	}
	// END SNIPPET: tutorial

}