package net.officefloor.tutorial.exceptionhttpserver;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Handles the exception by logging it.
 *
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ExceptionHandler {

	public void handleSqlException(@Parameter SQLException ex, ServerHttpConnection connection) throws IOException {
		// Production code may take some action and would use a Logger
		System.err.println(ex.getMessage());
		HttpResponse response = connection.getResponse();
		response.setStatus(HttpStatus.SEE_OTHER);
		response.getHeaders().addHeader("location", "/Error.html");
	}

}
// END SNIPPET: tutorial