package net.officefloor.web.value.load;

import java.util.Map;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpValueLocation;

/**
 * Loads a value onto the Object graph.
 * 
 * @author Daniel Sagenschneider
 */
public interface StatelessValueLoader {

	/**
	 * Loads the value onto the object graph.
	 * 
	 * @param object
	 *            Root object of the graph to have the value loaded.
	 * @param name
	 *            Full property name.
	 * @param nameIndex
	 *            Index into property name to identify particular property name
	 *            for next stringed property to load.
	 * @param value
	 *            Property value.
	 * @param location
	 *            {@link HttpValueLocation}.
	 * @param state
	 *            State of loading values to the Object graph.
	 * @throws HttpException
	 *             If fails to load the value.
	 */
	void loadValue(Object object, String name, int nameIndex, String value, HttpValueLocation location,
			Map<PropertyKey, Object> state) throws HttpException;

}