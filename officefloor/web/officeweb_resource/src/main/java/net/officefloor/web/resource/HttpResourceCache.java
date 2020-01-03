package net.officefloor.web.resource;

import java.io.IOException;

/**
 * Cache of the {@link HttpResource} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResourceCache {

	/**
	 * Obtains the cached {@link HttpResource}.
	 * 
	 * @param path
	 *            Path to the {@link HttpResource}.
	 * @return {@link HttpResource} or <code>null</code> if {@link HttpResource}
	 *         not cached.
	 * @throws IOException
	 *             If failure in finding the {@link HttpResource}.
	 */
	HttpResource getHttpResource(String path) throws IOException;

}