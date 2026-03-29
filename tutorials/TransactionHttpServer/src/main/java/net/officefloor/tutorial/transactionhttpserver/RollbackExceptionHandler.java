package net.officefloor.tutorial.transactionhttpserver;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Handles exception logic.
 * 
 * @author Daniel Sagenschneider
 */
public class RollbackExceptionHandler {

	public void handle(@Parameter IllegalArgumentException exception, ServerHttpConnection connection)
			throws Exception {
		HttpResponse response = connection.getResponse();
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		response.getEntityWriter().write(exception.getMessage());
	}

}