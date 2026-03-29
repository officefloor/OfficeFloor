package net.officefloor.tutorial.loggerhttpserver;

import java.util.logging.Level;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.test.logger.LoggerExtension;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the Logger HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class LoggerHttpServerTest {

	// START SNIPPET: tutorial
	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	@RegisterExtension
	public LoggerExtension log = new LoggerExtension();

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