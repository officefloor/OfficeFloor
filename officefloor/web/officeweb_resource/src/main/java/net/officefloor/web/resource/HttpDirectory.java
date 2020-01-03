package net.officefloor.web.resource;

import java.io.IOException;

/**
 * HTTP Directory.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpDirectory extends HttpResource {

	/**
	 * Obtains the default {@link HttpFile} for the {@link HttpDirectory}.
	 * 
	 * @return Default {@link HttpFile}.
	 * @throws IOException
	 *             If failure in finding the {@link HttpFile}.
	 */
	HttpFile getDefaultHttpFile() throws IOException;

}