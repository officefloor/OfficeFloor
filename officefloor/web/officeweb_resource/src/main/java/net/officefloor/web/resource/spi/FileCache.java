package net.officefloor.web.resource.spi;

import java.nio.file.Path;
import java.io.Closeable;
import java.io.IOException;

/**
 * Cache of files.
 * 
 * @author Daniel Sagenschneider
 */
public interface FileCache extends Closeable {

	/**
	 * Creates a new file.
	 * 
	 * @param name
	 *            Name to aid in identifying the file for debugging.
	 * @return {@link Path} to the new file.
	 * @throws IOException
	 *             If fails to create the file.
	 */
	Path createFile(String name) throws IOException;

	/**
	 * Creates a new directory.
	 *
	 * @param name
	 *            Name to aid in identifying the file for debugging.
	 * @return {@link Path} to the new directory.
	 * @throws IOException
	 *             If fails to create the directory.
	 */
	Path createDirectory(String name) throws IOException;

}