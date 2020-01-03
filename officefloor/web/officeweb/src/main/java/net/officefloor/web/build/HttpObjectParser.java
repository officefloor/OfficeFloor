package net.officefloor.web.build;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Parses an object from the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpObjectParser<T> extends HttpContentParser {

	/**
	 * Obtains the type of object parsed from the {@link ServerHttpConnection}.
	 * 
	 * @return Object type.
	 */
	Class<T> getObjectType();

	/**
	 * Parses the object from the {@link ServerHttpConnection}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @return Parsed object.
	 * @throws HttpException
	 *             If fails to parse the object from the
	 *             {@link ServerHttpConnection}.
	 */
	T parse(ServerHttpConnection connection) throws HttpException;

}