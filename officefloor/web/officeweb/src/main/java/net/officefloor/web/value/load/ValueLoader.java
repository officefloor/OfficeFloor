package net.officefloor.web.value.load;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpValueLocation;

/**
 * Loads a value onto the Object graph.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueLoader {

	/**
	 * Loads the value onto the object graph.
	 * 
	 * @param name
	 *            Property name.
	 * @param value
	 *            Property value.
	 * @param location
	 *            {@link HttpValueLocation}.
	 * @throws HttpException
	 *             If fails to load the value.
	 */
	void loadValue(String name, String value, HttpValueLocation location) throws HttpException;

}