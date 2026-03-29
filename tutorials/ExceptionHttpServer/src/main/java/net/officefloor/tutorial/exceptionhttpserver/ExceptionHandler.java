package net.officefloor.tutorial.exceptionhttpserver;

import java.sql.SQLException;

import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Handles the exception by logging it.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ExceptionHandler {

	public void handleSqlException(@Parameter SQLException ex) {
		// Production code may take some action and would use a Logger
		System.err.println(ex.getMessage());
	}

}
// END SNIPPET: tutorial