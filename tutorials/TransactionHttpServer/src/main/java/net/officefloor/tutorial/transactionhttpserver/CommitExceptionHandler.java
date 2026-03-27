package net.officefloor.tutorial.transactionhttpserver;

import java.io.EOFException;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Handles exception logic.
 * 
 * @author Daniel Sagenschneider
 */
public class CommitExceptionHandler {

	public void handle(@Parameter EOFException exception, ServerHttpConnection connection) throws Exception {
		HttpResponse response = connection.getResponse();
		response.setStatus(HttpStatus.CREATED);
		response.getEntityWriter().write(exception.getMessage());
	}

}