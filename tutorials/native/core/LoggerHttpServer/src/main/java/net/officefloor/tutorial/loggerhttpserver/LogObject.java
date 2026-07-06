package net.officefloor.tutorial.loggerhttpserver;

import java.util.logging.Logger;

import net.officefloor.plugin.clazz.Dependency;

// START SNIPPET: tutorial
public class LogObject {

	private @Dependency Logger logger;

	public void log(LoggedRequest request) {
		this.logger.info("OBJECT: " + request.getMessage());
	}
}
// END SNIPPET: tutorial