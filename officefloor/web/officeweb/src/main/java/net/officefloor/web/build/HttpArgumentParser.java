package net.officefloor.web.build;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.value.load.ValueLoader;

/**
 * Parses arguments from the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpArgumentParser extends HttpContentParser {

	/**
	 * Parses the HTTP entity.
	 * 
	 * @param request     {@link HttpRequest}.
	 * @param valueLoader {@link ValueLoader}.
	 * @throws HttpException If fails to parse the {@link ServerHttpConnection}.
	 */
	void parse(HttpRequest request, ValueLoader valueLoader) throws HttpException;

}