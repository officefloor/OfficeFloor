package net.officefloor.tutorial.loggerhttpserver;

import java.util.logging.Logger;

import net.officefloor.plugin.clazz.Dependency;

/**
 * Object with dependency injected {@link Logger}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class LogObject {

	@Dependency
	private Logger logger;

	public void log(LoggedRequest request) {
		this.logger.info("OBJECT: " + request.getMessage());
	}
}
// END SNIPPET: tutorial