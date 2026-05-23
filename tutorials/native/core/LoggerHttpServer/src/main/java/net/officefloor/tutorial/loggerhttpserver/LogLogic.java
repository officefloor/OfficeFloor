package net.officefloor.tutorial.loggerhttpserver;

import java.util.logging.Logger;

/**
 * Logic for logging {@link LoggedRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class LogLogic {

	// START SNIPPET: tutorial
	public void log(LoggedRequest request, Logger procedureLogger, LogObject object) {

		// Log the request via procedure logger
		procedureLogger.info("PROCEDURE: " + request.getMessage());

		// Have dependency injected object use it's logger
		object.log(request);
	}
	// END SNIPPET: tutorial
}