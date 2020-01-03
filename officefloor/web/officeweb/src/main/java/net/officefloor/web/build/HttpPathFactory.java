package net.officefloor.web.build;

import net.officefloor.server.http.HttpException;

/**
 * Creates the HTTP path.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpPathFactory<T> {

	/**
	 * Obtains the expected type to retrieve values in constructing the path.
	 * 
	 * @return Expected type to retrieve values in constructing the path. May be
	 *         <code>null</code> if no values are required.
	 */
	Class<T> getValuesType();

	/**
	 * <p>
	 * Creates the client application path.
	 * <p>
	 * This is the path on the server to the {@link HttpInput} (i.e. includes
	 * the context path). It, however, does not include <code>protocol</code>,
	 * <code>domain</code> and <code>port</code>.
	 * 
	 * @param values
	 *            Optional object to obtain values to create the path.
	 * @return Application path.
	 * @throws HttpException
	 *             If fails to create the application path.
	 */
	String createApplicationClientPath(T values) throws HttpException;

}