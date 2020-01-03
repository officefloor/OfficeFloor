package net.officefloor.web.resource;

import java.io.IOException;

/**
 * Store of {@link HttpResource} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResourceStore {

	/**
	 * Obtains the {@link HttpResource}.
	 * 
	 * @param path Path to the {@link HttpResource}.
	 * @return {@link HttpResource}.
	 * @throws IOException If failure in finding the {@link HttpResource}.
	 */
	HttpResource getHttpResource(String path) throws IOException;

	/**
	 * Obtains the default {@link HttpFile} for the {@link HttpDirectory}.
	 * 
	 * @param directory {@link HttpDirectory}.
	 * @return {@link HttpFile} for the {@link HttpDirectory} or <code>null</code>
	 *         if no default {@link HttpFile}.
	 * @throws IOException If failure in obtaining default {@link HttpFile}.
	 */
	HttpFile getDefaultHttpFile(HttpDirectory directory) throws IOException;

}