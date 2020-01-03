package net.officefloor.web.resource.spi;

import java.io.IOException;

/**
 * Factory for the creation of a new {@link FileCache}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FileCacheFactory {

	/**
	 * Creates a new {@link FileCache}.
	 * 
	 * @param name
	 *            Name for the {@link FileCache}.
	 * @return New {@link FileCache}.
	 * @throws IOException
	 *             If fails to create a new {@link FileCache}.
	 */
	FileCache createFileCache(String name) throws IOException;

}