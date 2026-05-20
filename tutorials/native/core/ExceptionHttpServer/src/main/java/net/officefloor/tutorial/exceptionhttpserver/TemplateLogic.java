package net.officefloor.tutorial.exceptionhttpserver;

import java.sql.SQLException;

/**
 * Logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class TemplateLogic {

	public void submit() throws Exception {
		throw new SQLException("Test");
	}

}
// END SNIPPET: tutorial