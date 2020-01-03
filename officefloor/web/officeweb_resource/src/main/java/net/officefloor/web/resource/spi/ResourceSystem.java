package net.officefloor.web.resource.spi;

import java.io.IOException;
import java.nio.file.Path;

import net.officefloor.web.resource.HttpResourceStore;

/**
 * Underlying system to the {@link HttpResourceStore}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceSystem {

	/**
	 * Obtains the {@link Path} to the resource.
	 * 
	 * @param path
	 *            Path for the resource.
	 * @return {@link Path} if resource found, otherwise <code>null</code>.
	 * @throws IOException
	 *             If failure in obtaining {@link Path}.
	 */
	Path getResource(String path) throws IOException;

}