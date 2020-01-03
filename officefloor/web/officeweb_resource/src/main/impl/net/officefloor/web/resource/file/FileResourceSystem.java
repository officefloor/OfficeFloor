package net.officefloor.web.resource.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemContext;

/**
 * {@link ResourceSystem} for files.
 * 
 * @author Daniel Sagenschneider
 */
public class FileResourceSystem implements ResourceSystem {

	/**
	 * Root directory for this {@link ResourceSystem}.
	 */
	private final Path rootDirectory;

	/**
	 * Instantiate.
	 * 
	 * @param context
	 *            {@link ResourceSystemContext}.
	 * @throws IOException
	 *             If to setup file resources.
	 */
	public FileResourceSystem(ResourceSystemContext context) throws IOException {

		// Ensure have root directory for files
		this.rootDirectory = Paths.get(context.getLocation());
		if (!Files.isDirectory(this.rootDirectory)) {
			throw new FileNotFoundException("Can not find root directory for "
					+ FileResourceSystem.class.getSimpleName() + " at " + context.getLocation());
		}

		// TODO configure watching of directory for changes
	}

	/*
	 * =================== ResourceSystem =====================
	 */

	@Override
	public Path getResource(String path) throws IOException {

		// Need to strip off leading / to avoid absolute path resolution
		while (path.startsWith("/")) {
			path = path.substring("/".length());
		}

		// Return the path to potential file
		return this.rootDirectory.resolve(path);
	}

}